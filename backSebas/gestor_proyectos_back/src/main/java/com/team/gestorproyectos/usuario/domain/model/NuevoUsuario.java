package com.team.gestorproyectos.usuario.domain.model;

import java.util.Set;

import com.team.gestorproyectos.shared.domain.exception.ReglaNegocioException;

/**
 * Alta de una persona del equipo. La cuenta queda lista para entrar; no se envían invitaciones.
 *
 * @param password       contraseña temporal, en texto plano (se cifra antes de guardar)
 * @param cambiarPassword pedir que la cambie al primer inicio
 * @param proyectoIds    proyectos donde queda como miembro
 */
public record NuevoUsuario(
        String nombre,
        String apellidos,
        String correo,
        String password,
        boolean cambiarPassword,
        boolean activo,
        Set<String> roles,
        Set<Long> proyectoIds) {

    public static final int LARGO_MINIMO_PASSWORD = 8;

    public NuevoUsuario {
        nombre = nombre.trim();
        apellidos = apellidos == null || apellidos.isBlank() ? null : apellidos.trim();
        correo = correo.trim().toLowerCase();
        roles = Set.copyOf(roles);
        proyectoIds = proyectoIds == null ? Set.of() : Set.copyOf(proyectoIds);
        if (password.length() < LARGO_MINIMO_PASSWORD) {
            throw new ReglaNegocioException(
                    "La contraseña temporal debe tener al menos " + LARGO_MINIMO_PASSWORD + " caracteres");
        }
        if (roles.isEmpty()) {
            throw new ReglaNegocioException("Elige al menos un rol");
        }
    }
}
