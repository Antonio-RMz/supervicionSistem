package com.team.gestorproyectos.ticket.application;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.team.gestorproyectos.shared.application.ServicioAplicacion;
import com.team.gestorproyectos.shared.application.Transaccional;
import com.team.gestorproyectos.shared.domain.exception.AccesoDenegadoException;
import com.team.gestorproyectos.shared.domain.exception.NoEncontradoException;
import com.team.gestorproyectos.shared.domain.exception.ReglaNegocioException;
import com.team.gestorproyectos.shared.domain.port.PermisosPort;
import com.team.gestorproyectos.ticket.domain.model.EdicionTicket;
import com.team.gestorproyectos.ticket.domain.model.NuevoTicket;
import com.team.gestorproyectos.ticket.domain.model.PreparacionTicket;
import com.team.gestorproyectos.ticket.domain.model.PreparacionTicket.CriterioDeHistoria;
import com.team.gestorproyectos.ticket.domain.model.PreparacionTicket.FaseDeTicket;
import com.team.gestorproyectos.ticket.domain.model.PreparacionTicket.HistoriaDeTicket;
import com.team.gestorproyectos.ticket.domain.model.PreparacionTicket.ModuloDeTicket;
import com.team.gestorproyectos.ticket.domain.model.PreparacionTicket.PersonaDeEquipo;
import com.team.gestorproyectos.ticket.domain.model.PreparacionTicket.TicketAbierto;
import com.team.gestorproyectos.ticket.domain.model.TicketEditable;
import com.team.gestorproyectos.ticket.domain.model.TipoTicket;
import com.team.gestorproyectos.ticket.domain.port.in.CrearTicketUseCase;
import com.team.gestorproyectos.ticket.domain.port.out.TicketEscrituraPort;
import com.team.gestorproyectos.ticket.domain.port.out.TicketEscrituraPort.ProyectoDeTicket;
import com.team.gestorproyectos.shared.domain.port.AvisosPort;

import lombok.RequiredArgsConstructor;

@ServicioAplicacion
@RequiredArgsConstructor
public class CrearTicketService implements CrearTicketUseCase {

    /** Ya no hay Actividades: la historia le llega directo a sus Desarrolladores y anotan lo que hacen en ella. */
    private static final String SIN_ACTIVIDADES = "Ya no se crean Actividades: la historia le llega directo a sus"
            + " Desarrolladores y lo que hacen se anota en \"Lo que hice\" de la historia";

    private final TicketEscrituraPort tickets;
    private final PermisosPort permisos;
    private final AvisosPort avisos;

    @Override
    @Transaccional(soloLectura = true)
    public PreparacionTicket preparar(Long proyectoId, Long usuarioId, boolean esAdmin) {
        ProyectoDeTicket proyecto = verificar(proyectoId, usuarioId, esAdmin);
        return new PreparacionTicket(proyecto.id(), proyecto.nombre(), proyecto.liderId(),
                tickets.fasesConHistorias(proyectoId), tickets.equipo(proyectoId),
                tickets.siguientesNumeros(proyectoId), tickets.abiertos(proyectoId));
    }

    @Override
    @Transaccional
    public Long crear(Long proyectoId, Long usuarioId, boolean esAdmin, NuevoTicket nuevo) {
        verificar(proyectoId, usuarioId, esAdmin);
        validar(proyectoId, nuevo, null);
        int numero = tickets.siguientesNumeros(proyectoId).getOrDefault(nuevo.tipo(), 1);
        Long ticketId = tickets.crear(proyectoId, numero, nuevo, usuarioId);
        avisos.avisar(AvisosPort.Tipo.ASIGNACION, ticketId, usuarioId, List.of(nuevo.responsableId()));
        return ticketId;
    }

    @Override
    @Transaccional(soloLectura = true)
    public EdicionTicket edicion(Long proyectoId, Long ticketId, Long usuarioId, boolean esAdmin) {
        ProyectoDeTicket proyecto = tickets.proyectoVisible(proyectoId, usuarioId, esAdmin)
                .orElseThrow(() -> new NoEncontradoException("No encontramos ese proyecto"));
        TicketEditable ticket = editable(proyectoId, ticketId, usuarioId, esAdmin);
        PreparacionTicket preparacion = new PreparacionTicket(proyecto.id(), proyecto.nombre(), proyecto.liderId(),
                tickets.fasesConHistorias(proyectoId), tickets.equipo(proyectoId),
                tickets.siguientesNumeros(proyectoId),
                tickets.abiertos(proyectoId).stream().filter(t -> !t.id().equals(ticketId)).toList());
        return new EdicionTicket(preparacion, ticket);
    }

    @Override
    @Transaccional
    public void editar(Long proyectoId, Long ticketId, Long usuarioId, boolean esAdmin, NuevoTicket pedido) {
        tickets.proyectoVisible(proyectoId, usuarioId, esAdmin)
                .orElseThrow(() -> new NoEncontradoException("No encontramos ese proyecto"));
        NuevoTicket actual = editable(proyectoId, ticketId, usuarioId, esAdmin).datos();
        // Tipo, fase e historia se quedan: su código y su rama ya salieron de ahí.
        NuevoTicket datos = new NuevoTicket(actual.faseId(), actual.historiaId(), actual.tipo(), pedido.titulo(),
                pedido.descripcion(), pedido.moduloId(), pedido.capas(), pedido.criterioIds(), List.of(),
                pedido.dependeDe(), pedido.responsableId(), pedido.testerId(), pedido.prioridad(),
                pedido.estimacionHoras(), pedido.fechaLimite());
        validar(proyectoId, datos, ticketId);
        tickets.actualizar(ticketId, datos);
        // Solo si cambió de responsable: al nuevo le llega el aviso.
        if (!Objects.equals(actual.responsableId(), datos.responsableId())) {
            avisos.avisar(AvisosPort.Tipo.ASIGNACION, ticketId, usuarioId, List.of(datos.responsableId()));
        }
    }

    private TicketEditable editable(Long proyectoId, Long ticketId, Long usuarioId, boolean esAdmin) {
        TicketEditable ticket = tickets.editable(proyectoId, ticketId)
                .orElseThrow(() -> new NoEncontradoException("No encontramos ese ticket"));
        if (!ticket.puedeEditar(usuarioId, esAdmin)) {
            throw new AccesoDenegadoException(ticket.motivoSinEdicion());
        }
        return ticket;
    }

    /** @param ticketId el que se edita (no puede depender de sí mismo); null si es nuevo */
    private void validar(Long proyectoId, NuevoTicket nuevo, Long ticketId) {
        FaseDeTicket fase = tickets.fasesConHistorias(proyectoId).stream()
                .filter(f -> f.id().equals(nuevo.faseId()))
                .findFirst()
                .orElseThrow(() -> new ReglaNegocioException("Esa fase no es de este proyecto o ya está cerrada"));
        HistoriaDeTicket historia = fase.historias().stream()
                .filter(h -> h.id().equals(nuevo.historiaId()))
                .findFirst()
                .orElseThrow(() -> new ReglaNegocioException(
                        "La historia debe ser de la Fase " + fase.numero() + " y estar Lista, Lista para desarrollo o En desarrollo"));
        if (nuevo.tipo() == TipoTicket.ACTV) {
            throw new ReglaNegocioException(SIN_ACTIVIDADES);
        }

        // Un ticket pertenece a un solo módulo, de los que trabaja su historia (o su fase, si la historia no dice).
        Set<Long> modulosPermitidos = historia.moduloIds().isEmpty()
                ? fase.modulos().stream().map(ModuloDeTicket::id).collect(Collectors.toSet())
                : historia.moduloIds();
        if (!modulosPermitidos.contains(nuevo.moduloId())) {
            throw new ReglaNegocioException("El repositorio debe ser uno de los de la historia HIST-"
                    + String.format("%02d", historia.numero()));
        }
        Set<Long> criterios = historia.criterios().stream().map(CriterioDeHistoria::id).collect(Collectors.toSet());
        if (!criterios.containsAll(nuevo.criterioIds())) {
            throw new ReglaNegocioException("Uno de los criterios no es de la historia");
        }
        Set<Long> abiertos = tickets.abiertos(proyectoId).stream().map(TicketAbierto::id)
                .filter(id -> !id.equals(ticketId)).collect(Collectors.toSet());
        if (!abiertos.containsAll(nuevo.dependeDe())) {
            throw new ReglaNegocioException("Solo puede depender de tickets abiertos de este proyecto");
        }
        List<PersonaDeEquipo> personas = tickets.equipo(proyectoId);
        Set<Long> equipo = personas.stream().map(PersonaDeEquipo::id).collect(Collectors.toSet());
        if (personas.stream().noneMatch(p -> p.id().equals(nuevo.responsableId()) && p.esDesarrollador())) {
            throw new ReglaNegocioException("El responsable debe ser un Desarrollador del equipo del proyecto");
        }
        if (nuevo.testerId() != null && !equipo.contains(nuevo.testerId())) {
            throw new ReglaNegocioException("Quien lo prueba debe ser del equipo del proyecto");
        }
    }

    private ProyectoDeTicket verificar(Long proyectoId, Long usuarioId, boolean esAdmin) {
        ProyectoDeTicket proyecto = tickets.proyectoVisible(proyectoId, usuarioId, esAdmin)
                .orElseThrow(() -> new NoEncontradoException("No encontramos ese proyecto"));
        // El Desarrollador no crea tickets: agrega subtareas a los suyos desde el detalle.
        if (!permisos.puede(usuarioId, PermisosPort.CREAR_TICKETS)) {
            throw new AccesoDenegadoException("Los tickets de trabajo los crea el Líder");
        }
        return proyecto;
    }

}
