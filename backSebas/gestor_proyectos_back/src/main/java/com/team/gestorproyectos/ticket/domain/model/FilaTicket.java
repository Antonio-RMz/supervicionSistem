package com.team.gestorproyectos.ticket.domain.model;

import com.team.gestorproyectos.shared.domain.model.ItemDeProyecto;

/**
 * Un ticket o historia abierto del proyecto, con la fase y la historia a las que pertenece.
 *
 * @param historiaNumero        null si el ticket no cuelga de una historia (o si el item es la historia)
 * @param responsableIniciales "PR" para el avatar; null si nadie lo tiene
 */
public record FilaTicket(
        ItemDeProyecto item,
        Long faseId,
        Integer faseNumero,
        Long historiaId,
        Integer historiaNumero,
        String responsableIniciales) {
}
