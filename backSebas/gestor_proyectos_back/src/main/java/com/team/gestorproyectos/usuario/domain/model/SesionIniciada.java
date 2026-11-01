package com.team.gestorproyectos.usuario.domain.model;

/** Resultado de iniciar sesión: el token para las siguientes peticiones y quién entró. */
public record SesionIniciada(String token, Usuario usuario) {
}
