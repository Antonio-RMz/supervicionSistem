package com.team.gestorproyectos.usuario.domain.port.out;

import java.util.List;

import com.team.gestorproyectos.usuario.domain.model.Permiso;
import com.team.gestorproyectos.usuario.domain.model.Rol;

public interface CatalogoAccesosPort {

    List<Rol> roles();

    List<Permiso> permisos();
}
