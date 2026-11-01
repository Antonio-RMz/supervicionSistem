package com.team.gestorproyectos.ticket.domain.model;

/** Una opción del selector de fase del Tablero. */
public record FaseTablero(Long id, int numero, String nombre, String estado, boolean mantenimiento) {
}
