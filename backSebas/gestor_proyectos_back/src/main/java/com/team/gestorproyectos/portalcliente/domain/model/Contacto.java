package com.team.gestorproyectos.portalcliente.domain.model;

/** A quién le escribe el cliente si tiene dudas (el Analista del proyecto). */
public record Contacto(String nombre, String rol, String correo) {
}
