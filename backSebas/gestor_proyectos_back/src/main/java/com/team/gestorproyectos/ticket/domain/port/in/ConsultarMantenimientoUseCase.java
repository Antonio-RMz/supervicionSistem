package com.team.gestorproyectos.ticket.domain.port.in;

import com.team.gestorproyectos.ticket.domain.model.Mantenimiento;

/** Vista de Mantenimiento del proyecto (PDF pág. 37). La ve todo el equipo del proyecto. */
public interface ConsultarMantenimientoUseCase {

    Mantenimiento mantenimiento(Long proyectoId, Long usuarioId, boolean esAdmin);
}
