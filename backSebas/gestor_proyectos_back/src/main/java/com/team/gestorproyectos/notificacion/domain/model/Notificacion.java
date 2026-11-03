package com.team.gestorproyectos.notificacion.domain.model;

import java.time.LocalDateTime;

/**
 * Un aviso para una persona.
 *
 * @param tipo     ASIGNACION, BUG_REPORTADO o COMENTARIO
 * @param ticketId   a dónde lleva al abrirla, si es de un ticket
 * @param historiaId a dónde lleva al abrirla, si es de una historia
 */
public record Notificacion(Long id, String tipo, String texto, Long proyectoId, Long ticketId, Long historiaId,
                           boolean leida, LocalDateTime creadoEn) {
}
