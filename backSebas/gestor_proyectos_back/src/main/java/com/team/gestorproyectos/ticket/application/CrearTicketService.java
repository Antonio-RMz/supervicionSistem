package com.team.gestorproyectos.ticket.application;

import java.util.ArrayList;
import java.util.List;
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
import com.team.gestorproyectos.ticket.domain.model.TicketImportado;
import com.team.gestorproyectos.ticket.domain.model.TipoTicket;
import com.team.gestorproyectos.ticket.domain.port.in.CrearTicketUseCase;
import com.team.gestorproyectos.ticket.domain.port.out.TicketEscrituraPort;
import com.team.gestorproyectos.ticket.domain.port.out.TicketEscrituraPort.ProyectoDeTicket;

import lombok.RequiredArgsConstructor;

@ServicioAplicacion
@RequiredArgsConstructor
public class CrearTicketService implements CrearTicketUseCase {

    private static final int MAXIMO_IMPORTADOS = 50;

    private final TicketEscrituraPort tickets;
    private final PermisosPort permisos;

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
        return tickets.crear(proyectoId, numero, nuevo, usuarioId);
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
    }

    @Override
    @Transaccional
    public int importar(Long proyectoId, Long usuarioId, boolean esAdmin, Long historiaId, Long responsableId,
                        List<TicketImportado> importados) {
        verificar(proyectoId, usuarioId, esAdmin);
        if (importados == null || importados.isEmpty()) {
            throw new ReglaNegocioException("El JSON no trae tickets");
        }
        if (importados.size() > MAXIMO_IMPORTADOS) {
            throw new ReglaNegocioException("Se pueden importar hasta " + MAXIMO_IMPORTADOS + " tickets a la vez");
        }
        FaseDeTicket fase = tickets.fasesConHistorias(proyectoId).stream()
                .filter(f -> f.historias().stream().anyMatch(h -> h.id().equals(historiaId)))
                .findFirst()
                .orElseThrow(() -> new ReglaNegocioException(
                        "La historia debe estar Lista, Lista para desarrollo o En desarrollo para recibir tickets"));
        HistoriaDeTicket historia = fase.historias().stream().filter(h -> h.id().equals(historiaId)).findFirst()
                .orElseThrow();
        List<ModuloDeTicket> repositorios = fase.modulos().stream()
                .filter(m -> historia.moduloIds().isEmpty() || historia.moduloIds().contains(m.id()))
                .toList();

        // Primero se validan todos: si uno falla, no se crea ninguno.
        List<NuevoTicket> nuevos = new ArrayList<>();
        for (int i = 0; i < importados.size(); i++) {
            String cual = "Ticket " + (i + 1) + ": ";
            TicketImportado t = importados.get(i);
            if (t == null) {
                throw new ReglaNegocioException(cual + "viene vacío");
            }
            try {
                NuevoTicket nuevo = new NuevoTicket(fase.id(), historiaId, TipoTicket.ACTV, t.titulo(), t.descripcion(),
                        repositorio(t.repositorio(), repositorios), Set.of(), Set.of(), t.subtareas(), Set.of(),
                        responsableId, null, null, null, null);
                validar(proyectoId, nuevo, null);
                nuevos.add(nuevo);
            } catch (ReglaNegocioException e) {
                throw new ReglaNegocioException(cual + e.getMessage());
            }
        }

        int numero = tickets.siguientesNumeros(proyectoId).getOrDefault(TipoTicket.ACTV, 1);
        for (NuevoTicket nuevo : nuevos) {
            tickets.crear(proyectoId, numero++, nuevo, usuarioId);
        }
        return nuevos.size();
    }

    /** Por tipo ("API", "Web") o por nombre; si la historia trabaja en uno solo, puede faltar. */
    private static Long repositorio(String texto, List<ModuloDeTicket> repositorios) {
        if (texto == null || texto.isBlank()) {
            if (repositorios.size() == 1) {
                return repositorios.get(0).id();
            }
            throw new ReglaNegocioException("falta \"repositorio\" (" + nombres(repositorios) + ")");
        }
        String buscado = texto.trim();
        return repositorios.stream()
                .filter(m -> buscado.equalsIgnoreCase(m.tipo()) || buscado.equalsIgnoreCase(m.nombre()))
                .map(ModuloDeTicket::id)
                .findFirst()
                .orElseThrow(() -> new ReglaNegocioException(
                        "el repositorio \"" + buscado + "\" no es de la historia (" + nombres(repositorios) + ")"));
    }

    private static String nombres(List<ModuloDeTicket> repositorios) {
        return repositorios.stream().map(ModuloDeTicket::tipo).collect(Collectors.joining(", "));
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
