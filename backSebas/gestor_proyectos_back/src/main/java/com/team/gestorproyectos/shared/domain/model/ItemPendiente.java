package com.team.gestorproyectos.shared.domain.model;

/**
 * Un item con su acción pendiente.
 *
 * @param actor    quién debe actuar (null si nadie está asignado)
 * @param rolActor "Analista", "Líder"... como se muestra en "Esperando a otros"
 */
public record ItemPendiente(ItemTrabajo item, String accion, Persona actor, String rolActor) {
}
