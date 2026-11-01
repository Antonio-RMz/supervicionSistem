package com.team.gestorproyectos.proyecto.domain.port.in;

import java.util.List;

import com.team.gestorproyectos.proyecto.domain.model.FaseArbol;
import com.team.gestorproyectos.proyecto.domain.model.ResumenProyecto;

/** Detalle de un proyecto: pestañas Resumen y Fases e historias. */
public interface ConsultarProyectoUseCase {

    ResumenProyecto resumen(Long proyectoId, Long usuarioId, boolean esAdmin);

    List<FaseArbol> arbol(Long proyectoId, Long usuarioId, boolean esAdmin);
}
