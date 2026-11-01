package com.team.gestorproyectos.usuario.domain.model;

/**
 * Ajuste puntual sobre lo que dan los roles de una persona.
 *
 * @param proyectoId     null = aplica en todos los proyectos
 * @param otorgado       true = se le da aunque sus roles no lo tengan; false = se le quita
 * @param permisoNombre  y proyectoNombre solo vienen al leer; al guardar basta con códigos e ids
 */
public record PermisoExtra(
        String permisoCodigo,
        String permisoNombre,
        Long proyectoId,
        String proyectoNombre,
        boolean otorgado) {

    /** Mismo permiso en el mismo alcance: no puede haber dos. */
    public boolean mismoAjusteQue(PermisoExtra otro) {
        return permisoCodigo.equals(otro.permisoCodigo)
                && (proyectoId == null ? otro.proyectoId == null : proyectoId.equals(otro.proyectoId));
    }
}
