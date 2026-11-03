package com.team.gestorproyectos.ticket.application;

import java.util.List;

import com.team.gestorproyectos.shared.application.ServicioAplicacion;
import com.team.gestorproyectos.shared.application.Transaccional;
import com.team.gestorproyectos.shared.domain.exception.NoEncontradoException;
import com.team.gestorproyectos.shared.domain.exception.ReglaNegocioException;
import com.team.gestorproyectos.shared.domain.model.AvanceHistoria;
import com.team.gestorproyectos.shared.domain.model.ConteoTickets;
import com.team.gestorproyectos.shared.domain.model.FlujoTrabajo;
import com.team.gestorproyectos.ticket.domain.model.DatosMovimiento;
import com.team.gestorproyectos.ticket.domain.model.TicketParaMover;
import com.team.gestorproyectos.ticket.domain.model.TipoMovimiento;
import com.team.gestorproyectos.ticket.domain.model.Transicion;
import com.team.gestorproyectos.ticket.domain.port.in.MoverTicketUseCase;
import com.team.gestorproyectos.ticket.domain.port.out.TicketRepositoryPort;
import com.team.gestorproyectos.shared.domain.port.AvisosPort;

import lombok.RequiredArgsConstructor;

@ServicioAplicacion
@RequiredArgsConstructor
public class MoverTicketService implements MoverTicketUseCase {

    private final TicketRepositoryPort tickets;
    private final AvisosPort avisos;

    @Override
    @Transaccional
    public Transicion mover(Long ticketId, Long usuarioId, DatosMovimiento datos) {
        TicketParaMover ticket = tickets.buscarParaMover(ticketId)
                .orElseThrow(() -> new NoEncontradoException("No encontramos ese ticket"));
        if (datos.bloqueadoPorId() != null) {
            String estado = tickets.estadoDelTicket(ticket.proyectoId(), datos.bloqueadoPorId())
                    .orElseThrow(() -> new ReglaNegocioException("El ticket que lo bloquea no es de este proyecto"));
            if (FlujoTrabajo.terminado(estado) || new ConteoTickets(null, null, estado, 1).terminado()) {
                throw new ReglaNegocioException("Ese ticket ya está terminado: no puede bloquear a otro");
            }
        }
        // Confirmar un bug o aprobar una mejora lo asigna: siempre a un Desarrollador del equipo.
        if (datos.responsableId() != null && !tickets.esDesarrolladorDelEquipo(ticket.proyectoId(), datos.responsableId())) {
            throw new ReglaNegocioException("Asígnalo a un Desarrollador del equipo del proyecto");
        }
        Transicion transicion = ticket.mover(usuarioId, datos);
        if (datos.tipo() == TipoMovimiento.BLOQUEAR && datos.destrabadorId() != null) {
            String quien = tickets.nombreDe(datos.destrabadorId())
                    .orElseThrow(() -> new ReglaNegocioException("No encontramos a quien puede destrabarlo"));
            transicion = transicion.conMotivo(transicion.motivo() + " · Puede destrabarlo: " + quien);
        }
        tickets.guardar(transicion, usuarioId);
        if (datos.responsableId() != null && !datos.responsableId().equals(ticket.item().responsableId())) {
            avisos.avisar(AvisosPort.Tipo.ASIGNACION, ticketId, usuarioId, List.of(datos.responsableId()));
        }
        // Con su primera Actividad Lista, la historia Lista queda lista para desarrollo.
        if ("ACTV".equals(ticket.item().tipo()) && "BORRADOR".equals(transicion.estadoAnterior())
                && "POR_HACER".equals(transicion.estadoNuevo())) {
            tickets.historiaDe(ticketId).ifPresent(h -> tickets.historiaConActividadLista(h.id(), usuarioId));
        }
        avanzarHistoria(ticketId);
        if (new ConteoTickets(null, null, transicion.estadoNuevo(), 1).terminado()) {
            tickets.desbloquearLosQueEspera(ticketId, ticket.item().codigo());
        }
        return transicion;
    }

    /** La historia pasa sola a En validación al aprobarse su último ticket, y regresa si uno vuelve a trabajarse. */
    private void avanzarHistoria(Long ticketId) {
        // Una historia con Desarrolladores la terminan ellos: sus bugs no la mueven.
        tickets.historiaDe(ticketId).filter(h -> !h.conDesarrolladores()).ifPresent(historia -> AvanceHistoria
                .siguienteEstado(historia.estado(), historia.tickets())
                .ifPresent(nuevo -> tickets.avanzarHistoria(historia.id(), historia.estado(), nuevo,
                        "EN_VALIDACION".equals(nuevo) ? "Se aprobó el último PR: pasa a validación"
                                : "Uno de sus tickets volvió a trabajarse")));
    }
}
