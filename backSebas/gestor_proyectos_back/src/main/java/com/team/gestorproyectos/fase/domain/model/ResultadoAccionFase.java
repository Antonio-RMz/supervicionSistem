package com.team.gestorproyectos.fase.domain.model;

/** @param bugCreado "BUGS-50" si la acción fue revertir */
public record ResultadoAccionFase(String accion, String estadoNuevo, String bugCreado) {
}
