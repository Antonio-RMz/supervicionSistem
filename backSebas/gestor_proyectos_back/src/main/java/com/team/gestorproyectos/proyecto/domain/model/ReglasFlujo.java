package com.team.gestorproyectos.proyecto.domain.model;

import com.team.gestorproyectos.shared.domain.model.FormaTrabajo;

/**
 * Las reglas del flujo que se eligen al crear el proyecto (PDF pág. 16). La forma de trabajo y si la Actividad
 * requiere PR ya no cambian después; las otras cuatro sí.
 *
 * @param requierePr en Normal, si la Actividad termina con PR aprobado o con "Terminar"; en Gitflow siempre
 */
public record ReglasFlujo(boolean permitirAutovalidacion, boolean comentarioAlRechazar, boolean ramaAlIniciar,
                          boolean notificarClienteFase, FormaTrabajo formaTrabajo, boolean requierePr) {

    public ReglasFlujo {
        formaTrabajo = formaTrabajo == null ? FormaTrabajo.GITFLOW : formaTrabajo;
        requierePr = formaTrabajo == FormaTrabajo.GITFLOW || requierePr;
    }

    /** Las mismas reglas con la forma de trabajo de otras (al editar, la forma no cambia). */
    public ReglasFlujo conFormaDe(ReglasFlujo otras) {
        return new ReglasFlujo(permitirAutovalidacion, comentarioAlRechazar, ramaAlIniciar, notificarClienteFase,
                otras.formaTrabajo(), otras.requierePr());
    }

    public boolean normal() {
        return formaTrabajo == FormaTrabajo.NORMAL;
    }
}
