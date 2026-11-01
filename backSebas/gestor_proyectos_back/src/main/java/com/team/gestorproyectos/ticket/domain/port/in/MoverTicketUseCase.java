package com.team.gestorproyectos.ticket.domain.port.in;

import com.team.gestorproyectos.ticket.domain.model.DatosMovimiento;
import com.team.gestorproyectos.ticket.domain.model.Transicion;

/**
 * Ejecuta una acción del ticket: la usan el Tablero (arrastrar), Mis tickets, el Dashboard (botón)
 * y el detalle del ticket (todas, con su ventana de confirmación).
 */
public interface MoverTicketUseCase {

    Transicion mover(Long ticketId, Long usuarioId, DatosMovimiento datos);
}
