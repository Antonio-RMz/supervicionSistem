package com.team.gestorproyectos.fase.domain.model;

/** Criterios de cierre que el sistema revisa solo. Toda fase los tiene; los manuales se agregan aparte. */
public enum CriterioAutomatico {
    HISTORIAS_ACEPTADAS("Todas las historias aceptadas por el Analista"),
    SIN_BUGS_ALTA("Sin bugs de prioridad alta abiertos"),
    MODULOS_EN_STAGING("Repositorios involucrados desplegados en staging");

    private final String descripcion;

    CriterioAutomatico(String descripcion) {
        this.descripcion = descripcion;
    }

    public String descripcion() {
        return descripcion;
    }
}
