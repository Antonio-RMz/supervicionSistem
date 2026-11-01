package com.team.gestorproyectos.historia.domain.model;

import java.util.Set;

/** Quien quiere mover la historia, con los permisos de historia que tiene. */
public record QuienGestiona(Long id, boolean esAdmin, Set<String> permisos) {

    public QuienGestiona {
        permisos = Set.copyOf(permisos);
    }

    public boolean tiene(String permiso) {
        return permisos.contains(permiso);
    }
}
