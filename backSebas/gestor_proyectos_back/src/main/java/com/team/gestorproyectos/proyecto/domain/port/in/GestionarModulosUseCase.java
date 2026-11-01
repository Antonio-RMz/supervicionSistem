package com.team.gestorproyectos.proyecto.domain.port.in;

import com.team.gestorproyectos.proyecto.domain.model.ModuloProyecto;
import com.team.gestorproyectos.proyecto.domain.model.ModulosProyecto;

/**
 * Pestaña Módulos del proyecto (PDF pág. 35). La ve todo el equipo del proyecto; agregar, editar y quitar
 * módulos lo hacen su Líder o un Admin con el permiso proyecto.editar.
 */
public interface GestionarModulosUseCase {

    ModulosProyecto modulos(Long proyectoId, Long usuarioId, boolean esAdmin);

    /** @param modulo con id null para crearlo; @param responsableId responsable técnico, opcional */
    Long guardar(Long proyectoId, ModuloProyecto modulo, Long responsableId, Long usuarioId, boolean esAdmin);

    void quitar(Long proyectoId, Long moduloId, Long usuarioId, boolean esAdmin);
}
