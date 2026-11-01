package com.team.gestorproyectos.historia.domain.model;

/**
 * Lo que llega con la acción (PDF pág. 33).
 *
 * @param estadoEsperado el estado que vio quien presionó; si ya cambió, no se aplica
 * @param motivo         Descartar y Devolver
 * @param ticketId       Devolver reabriendo un ticket existente
 * @param crearBug       Devolver creando un bug dentro de la historia (para algo que no cubría ningún ticket)
 * @param faseDestinoId  Mover a otra fase
 */
public record DatosAccionHistoria(String estadoEsperado, String motivo, Long ticketId, boolean crearBug,
                                  Long faseDestinoId) {
}
