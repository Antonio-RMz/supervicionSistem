package com.team.gestorproyectos.historia.domain.model;

/**
 * Un Desarrollador de la historia: a quien le llega directo, sin Actividades como ticket.
 *
 * @param moduloId  el repositorio en que trabaja; null = en todos los de la historia
 * @param terminado ya marcó que terminó su parte
 */
public record DesarrolladorDeHistoria(Long usuarioId, String nombre, Long moduloId, String moduloNombre,
                                      boolean terminado) {
}
