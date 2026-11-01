package com.team.gestorproyectos.proyecto.domain.model;

import java.time.LocalDateTime;

/**
 * Un ticket que no avanza: bloqueado, o en QA hace más de 3 días.
 *
 * @param desde  cuándo llegó a ese estado
 * @param motivo por qué se bloqueó (null si está en QA)
 */
public record Detenido(Long ticketId, String codigo, String titulo, String estado, LocalDateTime desde,
                       String motivo) {
}
