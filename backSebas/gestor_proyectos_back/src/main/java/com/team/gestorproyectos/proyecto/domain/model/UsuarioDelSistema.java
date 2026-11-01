package com.team.gestorproyectos.proyecto.domain.model;

import com.team.gestorproyectos.shared.domain.exception.ReglaNegocioException;

/**
 * Alguien que usa el software que se construye (Administrador, Escuela, Alumno...), no alguien del equipo.
 * Es el "Como" de las historias.
 *
 * @param descripcion qué hace en el sistema; ayuda a la IA a proponer historias
 */
public record UsuarioDelSistema(String nombre, String descripcion) {

    public UsuarioDelSistema {
        nombre = nombre == null ? "" : nombre.trim();
        if (nombre.isEmpty()) {
            throw new ReglaNegocioException("Escribe el nombre de cada usuario del sistema");
        }
        if (nombre.length() > 80) {
            throw new ReglaNegocioException("El nombre \"" + nombre + "\" es muy largo (máximo 80 letras)");
        }
        descripcion = descripcion == null || descripcion.isBlank() ? null : descripcion.trim();
        if (descripcion != null && descripcion.length() > 300) {
            throw new ReglaNegocioException("La descripción de " + nombre + " es muy larga (máximo 300 letras)");
        }
    }
}
