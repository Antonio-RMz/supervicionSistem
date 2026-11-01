package com.team.gestorproyectos.historia.domain.model;

/**
 * Una imagen ya subida a la historia.
 *
 * @param ruta dónde quedó el archivo, para leerlo o borrarlo
 */
public record ImagenGuardada(Long id, String nombre, String tipoMime, String ruta) {
}
