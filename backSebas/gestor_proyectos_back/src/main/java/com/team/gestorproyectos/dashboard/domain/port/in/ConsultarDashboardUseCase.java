package com.team.gestorproyectos.dashboard.domain.port.in;

import com.team.gestorproyectos.dashboard.domain.model.Dashboard;

/** El Dashboard de una persona del equipo en un proyecto donde es miembro (o cualquiera si es Admin). */
public interface ConsultarDashboardUseCase {

    Dashboard consultar(Long proyectoId, Long usuarioId, boolean esAdmin);
}
