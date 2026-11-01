package com.team.gestorproyectos.ticket.domain.port.in;

import com.team.gestorproyectos.ticket.domain.model.NuevoTicketMantenimiento;
import com.team.gestorproyectos.ticket.domain.model.PreparacionMantenimiento;
import com.team.gestorproyectos.ticket.domain.model.ReporteFalla;

/** Los dos botones de la vista de Mantenimiento: Nuevo ticket (PDF pág. 38) y Reportar falla (pág. 39). */
public interface CrearEnMantenimientoUseCase {

    PreparacionMantenimiento preparar(Long proyectoId, Long usuarioId, boolean esAdmin);

    /** Pide "ticket.crear". Devuelve el id del ticket nuevo. */
    Long crearTicket(Long proyectoId, Long usuarioId, boolean esAdmin, NuevoTicketMantenimiento nuevo);

    /** Pide "bug.reportar". Devuelve el id del bug nuevo. */
    Long reportarFalla(Long proyectoId, Long usuarioId, boolean esAdmin, ReporteFalla reporte);
}
