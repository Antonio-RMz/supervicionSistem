package com.team.gestorproyectos.proyecto.domain.model;

import java.math.BigDecimal;

/**
 * "Salud del proyecto" (PDF pág. 14).
 *
 * @param diasPromedioEnQa cuánto tardan los tickets en salir de QA (últimos 30 días); null si no hubo
 * @param historiasSinTickets historias ya listas o en desarrollo a las que nadie les ha creado tickets
 */
public record Salud(long rechazosQaMes, Double diasPromedioEnQa, BigDecimal horasEstimadas, BigDecimal horasReales,
                    long historiasSinTickets) {
}
