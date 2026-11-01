package com.team.gestorproyectos.usuario.domain.port.in;

import com.team.gestorproyectos.usuario.domain.model.Accesos;

/** Roles y permisos extra de una persona (panel lateral de Usuarios y roles). */
public interface GestionarAccesosUseCase {

    Accesos consultar(Long usuarioId);

    /** @param adminId quien hace el cambio: no puede quitarse a sí mismo el rol de Administrador */
    Accesos actualizar(Long adminId, Long usuarioId, Accesos accesos);
}
