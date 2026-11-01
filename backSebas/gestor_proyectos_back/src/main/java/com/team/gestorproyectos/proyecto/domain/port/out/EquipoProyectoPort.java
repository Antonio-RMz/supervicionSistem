package com.team.gestorproyectos.proyecto.domain.port.out;

import java.util.List;
import java.util.Optional;

import com.team.gestorproyectos.proyecto.domain.model.EquipoProyecto.Persona;
import com.team.gestorproyectos.proyecto.domain.model.EquipoProyecto.Reasignacion;
import com.team.gestorproyectos.proyecto.domain.model.ReglasFlujo;
import com.team.gestorproyectos.proyecto.domain.model.VistaEquipo.Candidato;
import com.team.gestorproyectos.proyecto.domain.model.VistaEquipo.PermisoDelProyecto;

/** El equipo de un proyecto: quién está, qué permisos extra tiene y quién más puede entrar. */
public interface EquipoProyectoPort {

    Optional<ProyectoDelEquipo> proyecto(Long proyectoId);

    /** Miembros y Líder, con sus roles y su último acceso. */
    List<Persona> personas(Long proyectoId);

    List<PermisoDelProyecto> permisosExtra(Long proyectoId);

    /** Personas del equipo con cuenta activa que no están en el proyecto. */
    List<Candidato> candidatos(Long proyectoId);

    void agregar(Long proyectoId, Long usuarioId);

    void quitar(Long proyectoId, Long usuarioId);

    /** Cambia de manos los tickets y deja un comentario en cada uno con quién lo hizo. */
    void reasignar(Reasignacion reasignacion, Long usuarioId);

    record ProyectoDelEquipo(Long id, Long liderId, ReglasFlujo reglas) {
    }
}
