package com.team.gestorproyectos.usuario.domain.port.in;

import com.team.gestorproyectos.usuario.domain.model.NuevoUsuario;
import com.team.gestorproyectos.usuario.domain.model.UsuarioConProyectos;

public interface CrearUsuarioUseCase {

    UsuarioConProyectos crear(NuevoUsuario nuevo);
}
