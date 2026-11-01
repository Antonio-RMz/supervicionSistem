package com.team.gestorproyectos.proyecto.domain.port.in;

import java.util.List;

import com.team.gestorproyectos.proyecto.domain.model.ProyectoResumen;

/** "Solo ves los proyectos donde eres miembro." El Administrador ve todos. */
public interface ListarProyectosDelUsuarioUseCase {

    List<ProyectoResumen> listar(Long usuarioId, boolean esAdmin);
}
