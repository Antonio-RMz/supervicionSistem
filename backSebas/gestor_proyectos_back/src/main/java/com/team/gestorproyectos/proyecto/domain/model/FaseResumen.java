package com.team.gestorproyectos.proyecto.domain.model;

import java.time.LocalDate;
import java.util.List;

/** Una fila de "Fases" con su avance y los módulos que trabaja. */
public record FaseResumen(
        Long id,
        int numero,
        String nombre,
        String estado,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        String versionRelease,
        boolean mantenimiento,
        List<TipoModulo> modulos,
        long historias,
        long tickets,
        int avance) {
}
