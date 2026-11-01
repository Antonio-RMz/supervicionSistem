package com.team.gestorproyectos.proyecto.domain.model;

import java.time.LocalDate;

/** Los datos generales del proyecto (encabezado y tarjeta "Información"). clienteId null = interno. */
public record InfoProyecto(
        Long id,
        String codigo,
        String nombre,
        String descripcion,
        String estado,
        Long clienteId,
        String clienteNombre,
        String prioridad,
        LocalDate fechaInicio,
        LocalDate fechaEntrega,
        Long liderId,
        String liderNombre,
        ReglasFlujo reglas) {
}
