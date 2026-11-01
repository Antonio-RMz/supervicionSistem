package com.team.gestorproyectos.fase.domain.model;

/**
 * Lo que impide eliminar una fase.
 *
 * @param historiasIniciadas historias que ya pasaron de Lista (las que trabajan o ya terminaron)
 * @param tickets            tickets de la fase, de sus historias o que vienen de ellas
 * @param otros              solicitudes, demos o releases ligados a la fase
 */
public record BloqueosEliminarFase(long historiasIniciadas, long tickets, long otros) {
}
