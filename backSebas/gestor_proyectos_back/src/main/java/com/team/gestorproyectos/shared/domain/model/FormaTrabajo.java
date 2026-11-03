package com.team.gestorproyectos.shared.domain.model;

/**
 * Cómo se trabaja el repositorio del proyecto. Se elige al crearlo y ya no cambia.
 * Las dos trabajan en develop y pasan a main al terminar la fase (Subir a main y crear tag).
 * GITFLOW: la Actividad siempre pasa por PR y el Líder integra cada historia aceptada a develop.
 * NORMAL: más simple; todo sube directo a develop (con o sin PR, según el proyecto) y la historia queda
 * integrada en cuanto el Analista la acepta.
 */
public enum FormaTrabajo {
    GITFLOW,
    NORMAL
}
