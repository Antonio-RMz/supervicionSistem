package com.team.gestorproyectos.usuario.infrastructure.api.dto;

import java.util.Set;

/** Lo que el front necesita del usuario. Nunca incluye el hash de la contraseña. */
public record UsuarioResponse(
        Long id,
        String nombre,
        String nombreCompleto,
        String iniciales,
        String correo,
        String tipo,
        Long clienteId,
        String clienteNombre,
        boolean cambiarPassword,
        Set<String> roles) {
}
