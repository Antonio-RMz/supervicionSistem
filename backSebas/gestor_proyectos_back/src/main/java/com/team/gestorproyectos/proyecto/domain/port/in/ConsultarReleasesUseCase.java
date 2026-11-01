package com.team.gestorproyectos.proyecto.domain.port.in;

import com.team.gestorproyectos.proyecto.domain.model.ReleasesProyecto;

/** Pestaña Releases del proyecto (PDF pág. 36). La ve todo el equipo del proyecto. */
public interface ConsultarReleasesUseCase {

    ReleasesProyecto releases(Long proyectoId, Long usuarioId, boolean esAdmin);
}
