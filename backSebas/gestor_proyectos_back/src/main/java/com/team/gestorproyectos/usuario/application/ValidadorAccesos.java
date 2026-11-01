package com.team.gestorproyectos.usuario.application;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import com.team.gestorproyectos.shared.application.ServicioAplicacion;
import com.team.gestorproyectos.shared.domain.exception.ReglaNegocioException;
import com.team.gestorproyectos.usuario.domain.model.Permiso;
import com.team.gestorproyectos.usuario.domain.model.PermisoExtra;
import com.team.gestorproyectos.usuario.domain.model.Rol;
import com.team.gestorproyectos.usuario.domain.port.out.CatalogoAccesosPort;

import lombok.RequiredArgsConstructor;

/** Revisa que los roles y permisos que llegan existan en el catálogo. */
@ServicioAplicacion
@RequiredArgsConstructor
class ValidadorAccesos {

    private final CatalogoAccesosPort catalogo;

    void validarRoles(Collection<String> roles) {
        Set<String> existentes = catalogo.roles().stream().map(Rol::codigo).collect(Collectors.toSet());
        roles.stream().filter(rol -> !existentes.contains(rol)).findFirst().ifPresent(rol -> {
            throw new ReglaNegocioException("El rol " + rol + " no existe");
        });
    }

    void validarPermisos(Collection<PermisoExtra> permisosExtra) {
        Set<String> existentes = catalogo.permisos().stream().map(Permiso::codigo).collect(Collectors.toSet());
        permisosExtra.stream().map(PermisoExtra::permisoCodigo).filter(p -> !existentes.contains(p)).findFirst()
                .ifPresent(permiso -> {
                    throw new ReglaNegocioException("El permiso " + permiso + " no existe");
                });
    }
}
