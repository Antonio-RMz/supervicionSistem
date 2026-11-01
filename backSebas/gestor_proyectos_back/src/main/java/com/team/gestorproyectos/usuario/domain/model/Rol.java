package com.team.gestorproyectos.usuario.domain.model;

import java.util.Set;

/**
 * Administrador, Líder de Proyecto, Analista Comercial o Desarrollador.
 *
 * @param permisos códigos de los permisos que da el rol, ej. ticket.aprobar_qa
 */
public record Rol(String codigo, String nombre, String descripcion, Set<String> permisos) {

    public static final String ADMIN = "ADMIN";
}
