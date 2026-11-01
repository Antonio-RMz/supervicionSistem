package com.team.gestorproyectos.ticket.domain.port.in;

import com.team.gestorproyectos.ticket.domain.model.DetalleTicket;

/** Detalle del ticket (PDF pág. 29): lo ve cualquier miembro del proyecto. */
public interface DetalleTicketUseCase {

    DetalleTicket detalle(Long proyectoId, Long ticketId, Long usuarioId, boolean esAdmin);

    /** Las subtareas son solo aviso: no cambian el estado del ticket. Las marca quien lo trabaja. */
    void marcarSubtarea(Long proyectoId, Long ticketId, Long subtareaId, boolean hecha, Long usuarioId,
                        boolean esAdmin);

    /** Las agregan y quitan quien lo trabaja o el Líder, hasta que se termina. */
    void agregarSubtarea(Long proyectoId, Long ticketId, String descripcion, Long usuarioId, boolean esAdmin);

    void quitarSubtarea(Long proyectoId, Long ticketId, Long subtareaId, Long usuarioId, boolean esAdmin);

    void comentar(Long proyectoId, Long ticketId, String texto, Long usuarioId, boolean esAdmin);
}
