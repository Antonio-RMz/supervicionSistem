package com.team.gestorproyectos.usuario.domain.model;

import java.util.List;
import java.util.Set;

import com.team.gestorproyectos.shared.domain.exception.ReglaNegocioException;

/** Lo que puede hacer una persona: sus roles (los permisos se suman) y sus permisos extra. */
public record Accesos(Set<String> roles, List<PermisoExtra> permisosExtra) {

    public Accesos {
        roles = Set.copyOf(roles);
        permisosExtra = List.copyOf(permisosExtra);
        if (roles.isEmpty()) {
            throw new ReglaNegocioException("Elige al menos un rol");
        }
        for (int i = 0; i < permisosExtra.size(); i++) {
            for (int j = i + 1; j < permisosExtra.size(); j++) {
                if (permisosExtra.get(i).mismoAjusteQue(permisosExtra.get(j))) {
                    throw new ReglaNegocioException("Un permiso extra está repetido en el mismo proyecto");
                }
            }
        }
    }

    public boolean esAdmin() {
        return roles.contains(Rol.ADMIN);
    }
}
