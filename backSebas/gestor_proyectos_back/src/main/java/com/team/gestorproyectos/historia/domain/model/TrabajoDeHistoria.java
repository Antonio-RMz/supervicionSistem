package com.team.gestorproyectos.historia.domain.model;

import java.time.LocalDateTime;

/**
 * Un punto de "Lo que hice" de la historia: lo que el Desarrollador fue haciendo, o la guía que dejó la Analista
 * o el Líder para que lo marque.
 *
 * @param moduloNombre el repositorio en que se hizo; null si no se dijo
 */
public record TrabajoDeHistoria(Long id, String descripcion, Long moduloId, String moduloNombre, boolean hecho,
                                String creadoPor, LocalDateTime creadoEn) {
}
