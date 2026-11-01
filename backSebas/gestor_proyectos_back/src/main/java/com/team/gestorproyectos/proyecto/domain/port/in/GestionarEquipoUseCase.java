package com.team.gestorproyectos.proyecto.domain.port.in;

import java.util.List;

import com.team.gestorproyectos.proyecto.domain.model.VistaEquipo;
import com.team.gestorproyectos.proyecto.domain.model.VistaEquipo.Candidato;

/**
 * Pestaña Equipo del proyecto (PDF pág. 34). La ve todo el equipo del proyecto; agregar, quitar y
 * reasignar lo hace el Administrador (permiso proyecto.equipo).
 */
public interface GestionarEquipoUseCase {

    VistaEquipo equipo(Long proyectoId, Long usuarioId, boolean esAdmin);

    List<Candidato> candidatos(Long proyectoId, Long usuarioId, boolean esAdmin);

    void agregar(Long proyectoId, Long nuevoId, Long usuarioId, boolean esAdmin);

    void quitar(Long proyectoId, Long miembroId, Long usuarioId, boolean esAdmin);

    /** @return cuántos tickets cambiaron de manos */
    int reasignar(Long proyectoId, Long deId, Long responsableNuevoId, Long testerNuevoId, Long usuarioId,
                  boolean esAdmin);
}
