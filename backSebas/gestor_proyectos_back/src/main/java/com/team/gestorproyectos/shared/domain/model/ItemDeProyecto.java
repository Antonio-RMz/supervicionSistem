package com.team.gestorproyectos.shared.domain.model;

import java.util.Set;

/**
 * Un ticket o historia abierto, con lo que hace falta para saber quién actúa cuando no hay un
 * solo proyecto a la mano (detalle de una persona) o cuando importa si QA lo rechazó (Mis tickets).
 *
 * @param liderId              Líder del proyecto del item
 * @param ultimoEstadoAnterior de dónde venía en su último cambio de estado (EN_QA = lo rechazaron)
 */
public record ItemDeProyecto(ItemTrabajo item, Long liderId, String ultimoEstadoAnterior) {

    /** Estados en los que alguien está trabajando el item con las manos. */
    private static final Set<String> EN_TRABAJO = Set.of(
            "EN_PROGRESO", "EN_CORRECCION", "INVESTIGANDO", "REDACTANDO", "EN_DESARROLLO");

    public boolean enTrabajo() {
        return EN_TRABAJO.contains(item.estado());
    }

    /** QA o el Líder (pidiendo cambios a su PR) lo regresó y sigue sin corregirse. */
    public boolean rechazadoEnQa() {
        return enTrabajo() && ("EN_QA".equals(ultimoEstadoAnterior) || "EN_REVISION".equals(ultimoEstadoAnterior));
    }
}
