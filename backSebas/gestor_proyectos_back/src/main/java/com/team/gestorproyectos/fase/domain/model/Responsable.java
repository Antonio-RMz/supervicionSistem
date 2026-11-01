package com.team.gestorproyectos.fase.domain.model;

import java.util.List;

/** Alguien del equipo que puede ser responsable de la fase. */
public record Responsable(Long id, String nombreCompleto, String iniciales, List<String> roles) {
}
