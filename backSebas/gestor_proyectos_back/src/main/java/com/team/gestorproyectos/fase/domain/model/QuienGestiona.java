package com.team.gestorproyectos.fase.domain.model;

import java.util.Set;

/** Quien quiere mover la fase, con los permisos de fase y release que tiene. */
public record QuienGestiona(Long id, boolean esAdmin, Set<String> permisos) {

    public QuienGestiona {
        permisos = Set.copyOf(permisos);
    }

    public boolean tiene(String permiso) {
        return permisos.contains(permiso);
    }
}
