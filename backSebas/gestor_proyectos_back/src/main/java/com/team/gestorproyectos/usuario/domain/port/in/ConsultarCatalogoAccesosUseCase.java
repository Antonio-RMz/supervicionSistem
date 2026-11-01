package com.team.gestorproyectos.usuario.domain.port.in;

import java.util.List;

import com.team.gestorproyectos.usuario.domain.model.Permiso;
import com.team.gestorproyectos.usuario.domain.model.Rol;

/** Los roles con sus permisos y la lista de permisos (pestañas Roles y Matriz de permisos). */
public interface ConsultarCatalogoAccesosUseCase {

    List<Rol> roles();

    List<Permiso> permisos();
}
