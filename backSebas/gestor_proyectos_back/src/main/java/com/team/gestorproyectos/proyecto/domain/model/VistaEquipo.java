package com.team.gestorproyectos.proyecto.domain.model;

import java.util.List;

/**
 * La pestaña Equipo (PDF pág. 34) para quien la ve.
 *
 * @param permisos       los ajustes de permisos que aplican solo en este proyecto
 * @param puedeGestionar el Líder del proyecto o un Admin: agrega, quita y reasigna
 */
public record VistaEquipo(Long proyectoId, EquipoProyecto equipo, List<PermisoDelProyecto> permisos,
                          ReglasFlujo reglas, boolean puedeGestionar) {

    /** @param otorgado true = se le da aunque su rol no lo tenga; false = se le quita */
    public record PermisoDelProyecto(String permiso, Long usuarioId, String usuario, boolean otorgado) {
    }

    /** Alguien del equipo de la empresa que aún no está en el proyecto. */
    public record Candidato(Long id, String nombreCompleto, List<String> roles) {
    }
}
