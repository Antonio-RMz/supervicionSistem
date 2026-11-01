package com.team.gestorproyectos.usuario.domain.model;

/**
 * Si una persona tiene un permiso que no depende de un proyecto (ej. cliente.gestionar).
 *
 * @param porRol       alguno de sus roles lo da
 * @param ajusteGlobal permiso extra en "Todos los proyectos": true otorgado, false quitado, null sin ajuste
 */
public record PermisoEfectivo(boolean porRol, Boolean ajusteGlobal) {

    /** El ajuste puntual gana sobre lo que dan los roles. */
    public boolean concedido() {
        return ajusteGlobal != null ? ajusteGlobal : porRol;
    }
}
