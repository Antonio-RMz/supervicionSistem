package com.team.gestorproyectos.proyecto.domain.model;

/**
 * Un ticket dentro del árbol de Fases e historias.
 *
 * @param responsableNombre "Pedro Ramírez", para la tarjeta del ticket
 * @param accion botón si la siguiente acción es mía y se puede hacer desde la lista; si no, null
 * @param quien  "Ana · Analista" si la siguiente acción es de alguien más; si no, null
 */
public record TicketArbol(
        Long id,
        String tipo,
        String codigo,
        String titulo,
        String estado,
        TipoModulo modulo,
        Long responsableId,
        String responsableIniciales,
        String responsableNombre,
        String accion,
        String quien) {
}
