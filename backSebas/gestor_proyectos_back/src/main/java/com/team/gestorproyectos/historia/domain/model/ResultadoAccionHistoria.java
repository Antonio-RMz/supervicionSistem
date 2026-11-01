package com.team.gestorproyectos.historia.domain.model;

/** @param bugCreado "BUGS-33" si se devolvió creando un bug */
public record ResultadoAccionHistoria(String accion, String estadoNuevo, String bugCreado) {
}
