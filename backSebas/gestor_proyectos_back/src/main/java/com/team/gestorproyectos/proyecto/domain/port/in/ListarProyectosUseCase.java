package com.team.gestorproyectos.proyecto.domain.port.in;

import com.team.gestorproyectos.proyecto.domain.model.ListadoProyectos;

/** Tarjetas de la pantalla Proyectos: solo los proyectos donde soy miembro (todos si soy Admin). */
public interface ListarProyectosUseCase {

    ListadoProyectos listar(Long usuarioId, boolean esAdmin);
}
