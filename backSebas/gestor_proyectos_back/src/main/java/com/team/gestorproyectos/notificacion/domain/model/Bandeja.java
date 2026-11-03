package com.team.gestorproyectos.notificacion.domain.model;

import java.util.List;

/**
 * Lo que ve una persona al abrir la campana.
 *
 * @param noLeidas       cuántas no ha leído (todas, no solo las de la lista)
 * @param notificaciones las más recientes, de la más nueva a la más vieja
 */
public record Bandeja(long noLeidas, List<Notificacion> notificaciones) {
}
