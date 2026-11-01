package com.team.gestorproyectos.usuario.domain.model;

import java.time.LocalDateTime;

/**
 * Una línea de "Lo que hizo": un cambio de estado o un comentario.
 *
 * @param clase INICIO, ENVIO, RECHAZO, CIERRE, COMENTARIO u OTRO; el front le pone color
 */
public record Hecho(String descripcion, String clase, LocalDateTime fecha) {
}
