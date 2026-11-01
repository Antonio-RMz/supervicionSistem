package com.team.gestorproyectos.proyecto.infrastructure.persistence.repository;

/** Una fila del conteo de tickets agrupado por fase, módulo y estado. */
public record ConteoTicketsFila(Long faseId, Long moduloId, String estado, Long cantidad) {
}
