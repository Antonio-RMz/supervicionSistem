package com.team.gestorproyectos.usuario.domain.model;

/** @param ticketsAbiertos tickets abiertos donde la persona es responsable */
public record ProyectoDeUsuario(Long id, String nombre, long ticketsAbiertos) {
}
