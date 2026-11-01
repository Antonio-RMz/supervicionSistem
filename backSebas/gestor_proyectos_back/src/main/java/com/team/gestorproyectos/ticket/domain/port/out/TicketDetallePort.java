package com.team.gestorproyectos.ticket.domain.port.out;

import java.time.LocalDateTime;
import java.util.Optional;

import com.team.gestorproyectos.ticket.domain.model.InfoTicket;

public interface TicketDetallePort {

    /** Vacío si el ticket no existe o no es de ese proyecto. */
    Optional<InfoTicket> info(Long proyectoId, Long ticketId);

    /** @return false si la subtarea no es de ese ticket */
    boolean marcarSubtarea(Long ticketId, Long subtareaId, boolean hecha);

    void agregarSubtarea(Long ticketId, String descripcion);

    /** @return false si la subtarea no es de ese ticket */
    boolean quitarSubtarea(Long ticketId, Long subtareaId);

    void comentar(Long ticketId, Long usuarioId, String texto, LocalDateTime ahora);
}
