package com.team.gestorproyectos.ticket.application;

import java.time.LocalDateTime;

import com.team.gestorproyectos.shared.application.ServicioAplicacion;
import com.team.gestorproyectos.shared.application.Transaccional;
import com.team.gestorproyectos.shared.domain.exception.NoEncontradoException;
import com.team.gestorproyectos.shared.domain.port.PermisosPort;
import com.team.gestorproyectos.ticket.domain.model.Mantenimiento;
import com.team.gestorproyectos.ticket.domain.port.in.ConsultarMantenimientoUseCase;
import com.team.gestorproyectos.ticket.domain.port.out.TicketEscrituraPort;
import com.team.gestorproyectos.ticket.domain.port.out.TicketEscrituraPort.ProyectoDeTicket;
import com.team.gestorproyectos.ticket.domain.port.out.TicketMantenimientoPort;
import com.team.gestorproyectos.ticket.domain.port.out.TicketParchePort;

import lombok.RequiredArgsConstructor;

@ServicioAplicacion
@RequiredArgsConstructor
public class ConsultarMantenimientoService implements ConsultarMantenimientoUseCase {

    /** El tiempo medio de hotfix se mide sobre este periodo. */
    private static final int DIAS_TIEMPO_HOTFIX = 30;

    private final TicketEscrituraPort proyectos;
    private final TicketMantenimientoPort mantenimiento;
    private final TicketParchePort parches;
    private final PermisosPort permisos;

    @Override
    @Transaccional(soloLectura = true)
    public Mantenimiento mantenimiento(Long proyectoId, Long usuarioId, boolean esAdmin) {
        ProyectoDeTicket proyecto = proyectos.proyectoVisible(proyectoId, usuarioId, esAdmin)
                .orElseThrow(() -> new NoEncontradoException("No encontramos ese proyecto"));
        Long faseId = mantenimiento.faseMantenimiento(proyectoId)
                .orElseThrow(() -> new NoEncontradoException("Este proyecto no tiene fase de Mantenimiento"));
        // Igual que en el detalle del bug: el parche lo publica el Líder del proyecto.
        boolean puedePublicar = usuarioId.equals(proyecto.liderId())
                && permisos.puede(usuarioId, PermisosPort.PUBLICAR_PARCHES);
        return Mantenimiento.armar(faseId, mantenimiento.tickets(faseId),
                parches.versionEnProduccion(proyectoId).orElse(null),
                mantenimiento.proximaFaseNumero(proyectoId).orElse(null),
                mantenimiento.minutosHotfix(proyectoId, LocalDateTime.now().minusDays(DIAS_TIEMPO_HOTFIX)),
                mantenimiento.parches(proyectoId), puedePublicar);
    }
}
