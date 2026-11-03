package com.team.gestorproyectos.ticket.domain.port.out;

import java.util.List;
import java.util.Optional;

import com.team.gestorproyectos.shared.domain.model.AvanceHistoria.TicketEstado;
import com.team.gestorproyectos.ticket.domain.model.TicketParaMover;
import com.team.gestorproyectos.ticket.domain.model.Transicion;

public interface TicketRepositoryPort {

    Optional<TicketParaMover> buscarParaMover(Long ticketId);

    /** Cambia el estado (y la rama, si se creó) y deja el paso en el historial con quién y por qué. */
    void guardar(Transicion transicion, Long usuarioId);

    /** Estado de un ticket del proyecto; vacío si no existe o es de otro proyecto. */
    Optional<String> estadoDelTicket(Long proyectoId, Long ticketId);

    /** "Jorge Sebastián"; vacío si no existe. */
    Optional<String> nombreDe(Long usuarioId);

    /** Si la persona es Desarrollador, está activa y es del equipo del proyecto (o su Líder). */
    boolean esDesarrolladorDelEquipo(Long proyectoId, Long usuarioId);

    /** Los tickets bloqueados por este vuelven a trabajarse solos ("Desbloqueado: ACTV-07 se terminó"). */
    void desbloquearLosQueEspera(Long ticketId, String codigo);

    /** La historia del ticket con el estado de todos sus tickets; vacío si el ticket va suelto. */
    Optional<HistoriaDelTicket> historiaDe(Long ticketId);

    /** El paso automático de la historia según sus tickets; queda en el historial como automático. */
    void avanzarHistoria(Long historiaId, String anterior, String nuevo, String motivo);

    /**
     * Una historia Lista con su primera Actividad Lista queda Lista para desarrollo; si su fase ya está en curso,
     * arranca de una vez. En otro estado no cambia.
     */
    void historiaConActividadLista(Long historiaId, Long usuarioId);

    /** @param conDesarrolladores se trabaja sin Actividades: sus Desarrolladores la terminan, no sus tickets */
    record HistoriaDelTicket(Long id, String estado, List<TicketEstado> tickets, boolean conDesarrolladores) {
    }
}
