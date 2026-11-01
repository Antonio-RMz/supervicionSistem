package com.team.gestorproyectos.ticket.domain.model;

import com.team.gestorproyectos.shared.domain.model.Persona;

/**
 * Una fila de Mis tickets.
 *
 * @param responsable quien lo trabaja (en una historia, su Analista); null si nadie
 * @param accion      el botón ("Confirmar", "Aprobar QA"...) solo si la siguiente acción es mía; si no, null
 * @param asignado    lo trabajo, lo pruebo o soy el Analista de la historia
 * @param ejecutable  el botón hace la acción desde la lista. "Corregir" y las acciones con su propio
 *                    proceso (publicar parche, integrar historia) solo indican qué sigue.
 */
public record TicketMio(
        FilaTicket fila,
        Persona responsable,
        String accion,
        boolean rechazadaEnQa,
        boolean meToca,
        boolean asignado,
        boolean creadoPorMi,
        boolean ejecutable) {
}
