package com.team.gestorproyectos.shared.domain.model;

/** Quién tiene la siguiente acción de un ticket o historia. */
public enum Actor {
    /** El responsable del ticket (Desarrollador) o, en una historia, su Analista. */
    RESPONSABLE,
    /** Quien prueba el hotfix y lo verifica en producción: el Analista asignado al ticket. */
    TESTER,
    /** El Líder del proyecto. */
    LIDER
}
