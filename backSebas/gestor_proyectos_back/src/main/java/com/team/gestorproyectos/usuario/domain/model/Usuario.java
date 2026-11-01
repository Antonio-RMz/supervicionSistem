package com.team.gestorproyectos.usuario.domain.model;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Usuario del sistema. Java puro: no sabe nada de JPA ni de Spring.
 *
 * @param clienteId     solo para usuarios CLIENTE
 * @param clienteNombre nombre corto de su empresa, ej. "Grupo Ferretero León"
 * @param roles         códigos de rol (ADMIN, LIDER...). Vacío para clientes.
 * @param ultimoAcceso  null si nunca ha entrado
 */
public record Usuario(
        Long id,
        String nombre,
        String apellidos,
        String correo,
        String passwordHash,
        TipoUsuario tipo,
        Long clienteId,
        String clienteNombre,
        boolean activo,
        boolean cambiarPassword,
        Set<String> roles,
        LocalDateTime ultimoAcceso) {

    /** "Mariana Torres" */
    public String nombreCompleto() {
        return apellidos == null || apellidos.isBlank() ? nombre : nombre + " " + apellidos;
    }

    /** "MT": inicial del nombre y del primer apellido. */
    public String iniciales() {
        String inicialApellido = apellidos == null || apellidos.isBlank() ? "" : apellidos.substring(0, 1);
        return (nombre.substring(0, 1) + inicialApellido).toUpperCase();
    }
}
