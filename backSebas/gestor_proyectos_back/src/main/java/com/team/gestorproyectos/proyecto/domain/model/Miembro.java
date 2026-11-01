package com.team.gestorproyectos.proyecto.domain.model;

import java.util.List;

/** Alguien del equipo del proyecto. @param roles códigos: ADMIN, LIDER, ANALISTA... */
public record Miembro(Long id, String nombreCompleto, String iniciales, List<String> roles) {
}
