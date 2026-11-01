package com.team.gestorproyectos.proyecto.domain.port.out;

import java.util.List;

import com.team.gestorproyectos.proyecto.domain.model.UsuarioDelSistema;

public interface UsuariosDelSistemaPort {

    /** En su orden. */
    List<UsuarioDelSistema> usuarios(Long proyectoId);

    void reemplazar(Long proyectoId, List<UsuarioDelSistema> usuarios);
}
