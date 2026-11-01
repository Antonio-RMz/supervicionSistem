package com.team.gestorproyectos.ticket.application;

import java.util.Set;
import java.util.stream.Collectors;

import com.team.gestorproyectos.shared.application.ServicioAplicacion;
import com.team.gestorproyectos.shared.application.Transaccional;
import com.team.gestorproyectos.shared.domain.exception.AccesoDenegadoException;
import com.team.gestorproyectos.shared.domain.exception.NoEncontradoException;
import com.team.gestorproyectos.shared.domain.exception.ReglaNegocioException;
import com.team.gestorproyectos.shared.domain.port.PermisosPort;
import com.team.gestorproyectos.ticket.domain.model.NuevoTicketMantenimiento;
import com.team.gestorproyectos.ticket.domain.model.PreparacionMantenimiento;
import com.team.gestorproyectos.ticket.domain.model.PreparacionMantenimiento.HistoriaDeOrigen;
import com.team.gestorproyectos.ticket.domain.model.PreparacionTicket.ModuloDeTicket;
import com.team.gestorproyectos.ticket.domain.model.PreparacionTicket.PersonaDeEquipo;
import com.team.gestorproyectos.ticket.domain.model.ReporteFalla;
import com.team.gestorproyectos.ticket.domain.model.TipoTicket;
import com.team.gestorproyectos.ticket.domain.port.in.CrearEnMantenimientoUseCase;
import com.team.gestorproyectos.ticket.domain.port.out.TicketEscrituraPort;
import com.team.gestorproyectos.ticket.domain.port.out.TicketEscrituraPort.ProyectoDeTicket;
import com.team.gestorproyectos.ticket.domain.port.out.TicketMantenimientoPort;
import com.team.gestorproyectos.ticket.domain.port.out.TicketParchePort;

import lombok.RequiredArgsConstructor;

@ServicioAplicacion
@RequiredArgsConstructor
public class CrearEnMantenimientoService implements CrearEnMantenimientoUseCase {

    private final TicketEscrituraPort tickets;
    private final TicketMantenimientoPort mantenimiento;
    private final TicketParchePort parches;
    private final PermisosPort permisos;

    @Override
    @Transaccional(soloLectura = true)
    public PreparacionMantenimiento preparar(Long proyectoId, Long usuarioId, boolean esAdmin) {
        ProyectoDeTicket proyecto = proyecto(proyectoId, usuarioId, esAdmin);
        return new PreparacionMantenimiento(proyecto.id(), faseMantenimiento(proyectoId), proyecto.liderId(),
                parches.versionEnProduccion(proyectoId).orElse(null),
                mantenimiento.proximaFaseNumero(proyectoId).orElse(null), mantenimiento.modulos(proyectoId),
                tickets.equipo(proyectoId), tickets.siguientesNumeros(proyectoId), mantenimiento.historias(proyectoId),
                permisos.puede(usuarioId, PermisosPort.CREAR_TICKETS),
                permisos.puede(usuarioId, PermisosPort.REPORTAR_BUGS));
    }

    @Override
    @Transaccional
    public Long crearTicket(Long proyectoId, Long usuarioId, boolean esAdmin, NuevoTicketMantenimiento nuevo) {
        proyecto(proyectoId, usuarioId, esAdmin);
        if (!permisos.puede(usuarioId, PermisosPort.CREAR_TICKETS)) {
            throw new AccesoDenegadoException("Los tickets de mantenimiento los crea el Líder");
        }
        Long faseId = faseMantenimiento(proyectoId);
        verificarModulo(proyectoId, nuevo.moduloId());
        if (nuevo.responsableId() != null && tickets.equipo(proyectoId).stream()
                .noneMatch(p -> p.id().equals(nuevo.responsableId()) && p.esDesarrollador())) {
            throw new ReglaNegocioException("Asígnalo a un Desarrollador del equipo del proyecto");
        }
        return mantenimiento.crear(proyectoId, faseId, siguienteNumero(proyectoId, nuevo.tipo()), nuevo, usuarioId);
    }

    @Override
    @Transaccional
    public Long reportarFalla(Long proyectoId, Long usuarioId, boolean esAdmin, ReporteFalla reporte) {
        proyecto(proyectoId, usuarioId, esAdmin);
        if (!permisos.puede(usuarioId, PermisosPort.REPORTAR_BUGS)) {
            throw new AccesoDenegadoException("No tienes permiso para reportar fallas");
        }
        Long faseId = faseMantenimiento(proyectoId);
        verificarModulo(proyectoId, reporte.moduloId());
        if (reporte.historiaOrigenId() != null && mantenimiento.historias(proyectoId).stream()
                .map(HistoriaDeOrigen::id).noneMatch(reporte.historiaOrigenId()::equals)) {
            throw new ReglaNegocioException("La historia de la que viene debe ser de este proyecto");
        }
        // Se guarda con qué versión en producción se vio la falla, para saber qué parche la corrige.
        return mantenimiento.reportar(proyectoId, faseId, siguienteNumero(proyectoId, TipoTicket.BUGS), reporte,
                parches.versionEnProduccion(proyectoId).orElse(null), usuarioId);
    }

    private ProyectoDeTicket proyecto(Long proyectoId, Long usuarioId, boolean esAdmin) {
        return tickets.proyectoVisible(proyectoId, usuarioId, esAdmin)
                .orElseThrow(() -> new NoEncontradoException("No encontramos ese proyecto"));
    }

    private Long faseMantenimiento(Long proyectoId) {
        return mantenimiento.faseMantenimiento(proyectoId)
                .orElseThrow(() -> new NoEncontradoException("Este proyecto no tiene fase de Mantenimiento"));
    }

    private void verificarModulo(Long proyectoId, Long moduloId) {
        Set<Long> modulos = mantenimiento.modulos(proyectoId).stream().map(ModuloDeTicket::id)
                .collect(Collectors.toSet());
        if (!modulos.contains(moduloId)) {
            throw new ReglaNegocioException("El repositorio debe ser de este proyecto");
        }
    }

    private int siguienteNumero(Long proyectoId, TipoTicket tipo) {
        return tickets.siguientesNumeros(proyectoId).getOrDefault(tipo, 1);
    }
}
