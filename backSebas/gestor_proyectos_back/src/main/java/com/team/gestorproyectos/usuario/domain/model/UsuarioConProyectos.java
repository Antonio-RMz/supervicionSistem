package com.team.gestorproyectos.usuario.domain.model;

/** Una fila de la tabla de Usuarios: la persona y en cuántos proyectos está. */
public record UsuarioConProyectos(Usuario usuario, long proyectos) {
}
