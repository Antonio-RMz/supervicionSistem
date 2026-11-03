package com.team.gestorproyectos.proyecto.domain.port.out;

import java.util.Optional;

public interface IconoProyectoPort {

    /** El proyecto con su Líder y su ícono, si el usuario puede verlo (Admin, Líder o miembro). */
    Optional<ProyectoConIcono> visible(Long proyectoId, Long usuarioId, boolean esAdmin);

    /** ruta y tipo null = quitar el ícono. */
    void guardar(Long proyectoId, String ruta, String tipoMime);

    /** @param ruta null si no tiene ícono */
    record ProyectoConIcono(Long liderId, String ruta, String tipoMime) {
    }
}
