package com.team.gestorproyectos.cliente.domain.model;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Empresa para la que trabaja el equipo. Cada proyecto pertenece a un cliente.
 *
 * @param nombreCorto como aparece en listas y proyectos, ej. "Grupo Ferretero León"
 * @param giro        null si no se capturó
 */
public record Cliente(
        Long id,
        String razonSocial,
        String nombreCorto,
        String rfc,
        Giro giro,
        String ciudad,
        String contactoNombre,
        String contactoCorreo,
        String contactoTelefono,
        String notas,
        boolean activo) {

    /** "GF" para Grupo Ferretero León; "IN" para Interno (una sola palabra). */
    public String iniciales() {
        String[] palabras = nombreCorto.trim().split("\\s+");
        String iniciales = palabras.length == 1
                ? palabras[0].substring(0, Math.min(2, palabras[0].length()))
                : Arrays.stream(palabras).limit(2).map(p -> p.substring(0, 1)).collect(Collectors.joining());
        return iniciales.toUpperCase();
    }
}
