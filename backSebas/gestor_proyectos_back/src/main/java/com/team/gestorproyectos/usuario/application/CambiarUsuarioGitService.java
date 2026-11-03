package com.team.gestorproyectos.usuario.application;

import com.team.gestorproyectos.shared.application.ServicioAplicacion;
import com.team.gestorproyectos.shared.application.Transaccional;
import com.team.gestorproyectos.shared.domain.exception.NoEncontradoException;
import com.team.gestorproyectos.usuario.domain.model.UsuarioGit;
import com.team.gestorproyectos.usuario.domain.port.in.CambiarUsuarioGitUseCase;
import com.team.gestorproyectos.usuario.domain.port.out.UsuarioRepositoryPort;

import lombok.RequiredArgsConstructor;

@ServicioAplicacion
@RequiredArgsConstructor
public class CambiarUsuarioGitService implements CambiarUsuarioGitUseCase {

    private final UsuarioRepositoryPort usuarios;

    @Override
    @Transaccional
    public void cambiar(Long usuarioId, String usuarioGit) {
        usuarios.buscarDelEquipo(usuarioId).orElseThrow(() -> new NoEncontradoException("No encontramos a esa persona"));
        usuarios.cambiarUsuarioGit(usuarioId, UsuarioGit.normalizar(usuarioGit));
    }
}
