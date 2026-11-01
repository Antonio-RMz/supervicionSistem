package com.team.gestorproyectos.usuario.domain.port.in;

import java.util.List;

import com.team.gestorproyectos.usuario.domain.model.UsuarioConProyectos;

/** Las personas del equipo (no los contactos de clientes), con cuántos proyectos tiene cada una. */
public interface ListarUsuariosUseCase {

    List<UsuarioConProyectos> listar();
}
