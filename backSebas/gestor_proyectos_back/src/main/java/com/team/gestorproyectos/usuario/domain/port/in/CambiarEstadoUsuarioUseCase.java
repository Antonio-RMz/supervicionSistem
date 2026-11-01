package com.team.gestorproyectos.usuario.domain.port.in;

/** Activar o desactivar una cuenta. Desactivada, la persona ya no puede entrar. */
public interface CambiarEstadoUsuarioUseCase {

    /** @param adminId quien hace el cambio: no puede desactivarse a sí mismo */
    void cambiar(Long adminId, Long usuarioId, boolean activo);
}
