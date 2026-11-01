package com.team.gestorproyectos.proyecto.domain.model;

import java.util.List;

/** @param puedeCrear si quien consulta tiene el permiso "proyecto.crear" */
public record ListadoProyectos(List<ProyectoTarjeta> proyectos, boolean puedeCrear) {
}
