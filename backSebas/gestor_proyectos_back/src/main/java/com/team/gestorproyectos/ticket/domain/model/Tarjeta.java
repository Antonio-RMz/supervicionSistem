package com.team.gestorproyectos.ticket.domain.model;

import com.team.gestorproyectos.shared.domain.model.Persona;

/**
 * Una tarjeta del Tablero.
 *
 * @param avance      lo que yo puedo hacer para avanzarla (null si no me toca)
 * @param rechazo     regresarla, si soy quien la revisa (null si no)
 * @param bloqueadaPor "Analista", "Líder"... cuando la siguiente acción es de alguien más: candado en la tarjeta
 * @param mia         participo en ella ("Solo los míos")
 */
public record Tarjeta(
        FilaTicket fila,
        ColumnaTablero columna,
        Persona responsable,
        Movimiento avance,
        Movimiento rechazo,
        String bloqueadaPor,
        boolean mia) {

    /** Una acción que puedo hacer y a qué columna manda la tarjeta. */
    public record Movimiento(TipoMovimiento tipo, String accion, ColumnaTablero columnaDestino) {
    }
}
