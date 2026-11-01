package com.team.gestorproyectos.usuario.application;

import com.team.gestorproyectos.shared.application.ServicioAplicacion;
import com.team.gestorproyectos.shared.application.Transaccional;
import com.team.gestorproyectos.shared.domain.exception.NoEncontradoException;
import com.team.gestorproyectos.shared.domain.exception.ReglaNegocioException;
import com.team.gestorproyectos.usuario.domain.port.in.CambiarEstadoUsuarioUseCase;
import com.team.gestorproyectos.usuario.domain.port.out.UsuarioRepositoryPort;

import lombok.RequiredArgsConstructor;

@ServicioAplicacion
@RequiredArgsConstructor
public class CambiarEstadoUsuarioService implements CambiarEstadoUsuarioUseCase {

    private final UsuarioRepositoryPort usuarios;

    @Override
    @Transaccional
    public void cambiar(Long adminId, Long usuarioId, boolean activo) {
        usuarios.buscarDelEquipo(usuarioId).orElseThrow(() -> new NoEncontradoException("No encontramos a esa persona"));
        if (!activo && adminId.equals(usuarioId)) {
            throw new ReglaNegocioException("No puedes desactivar tu propia cuenta");
        }
        usuarios.cambiarActivo(usuarioId, activo);
    }
}
