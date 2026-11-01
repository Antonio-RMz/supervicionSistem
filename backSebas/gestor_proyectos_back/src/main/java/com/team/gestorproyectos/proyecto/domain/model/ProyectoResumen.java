package com.team.gestorproyectos.proyecto.domain.model;

/** Lo mínimo de un proyecto para listarlo (selector de proyecto, listas). */
public record ProyectoResumen(Long id, String codigo, String nombre, String estado, String clienteNombre) {
}
