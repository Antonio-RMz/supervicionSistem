package com.team.gestorproyectos.portalcliente.domain.model;

import java.time.LocalDate;

public record Fase(
        Long id,
        int numero,
        String nombre,
        String objetivo,
        EstadoFase estado,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        String version) {
}
