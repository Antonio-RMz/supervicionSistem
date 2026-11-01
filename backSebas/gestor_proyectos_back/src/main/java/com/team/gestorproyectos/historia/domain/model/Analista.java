package com.team.gestorproyectos.historia.domain.model;

import java.util.List;

/** Alguien del equipo del proyecto que puede ser responsable de la historia: un Analista o un Líder. */
public record Analista(Long id, String nombreCompleto, List<String> roles) {

    public boolean esAnalista() {
        return roles.contains("ANALISTA");
    }

    public boolean puedeSerResponsable() {
        return esAnalista() || roles.contains("LIDER");
    }
}
