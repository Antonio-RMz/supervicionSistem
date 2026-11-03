package com.team.gestorproyectos.usuario.domain.port.in;

/** El Admin captura o cambia el usuario de git de una persona del equipo. */
public interface CambiarUsuarioGitUseCase {

    /** @param usuarioGit vacío o null lo quita */
    void cambiar(Long usuarioId, String usuarioGit);
}
