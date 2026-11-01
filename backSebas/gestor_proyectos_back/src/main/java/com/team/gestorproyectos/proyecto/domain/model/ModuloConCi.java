package com.team.gestorproyectos.proyecto.domain.model;

import java.time.LocalDateTime;

/**
 * Un módulo con el resultado de su último CI.
 *
 * @param ciEstado PASO, FALLO o PENDIENTE del último pull request; null si nunca ha corrido
 */
public record ModuloConCi(ModuloProyecto modulo, String ciEstado, LocalDateTime ciFecha) {
}
