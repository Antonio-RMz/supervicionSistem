package com.team.gestorproyectos.portalcliente.domain.model;

import java.util.List;

/** Datos fijos del proyecto, tal como vienen de la base de datos, antes de calcular el avance. */
public record ProyectoBase(Long id, String codigo, String nombre, EstadoProyecto estado, List<Modulo> modulos) {
}
