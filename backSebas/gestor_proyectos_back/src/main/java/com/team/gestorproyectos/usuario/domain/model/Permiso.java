package com.team.gestorproyectos.usuario.domain.model;

/**
 * Una acción del sistema, ej. "release.publicar".
 *
 * @param grupo ADMINISTRACION, PROYECTO, FASE, HISTORIA, TICKET o RELEASE
 */
public record Permiso(String codigo, String nombre, String grupo) {
}
