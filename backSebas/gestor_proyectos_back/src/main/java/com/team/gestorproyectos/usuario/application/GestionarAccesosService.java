package com.team.gestorproyectos.usuario.application;

import com.team.gestorproyectos.shared.application.ServicioAplicacion;
import com.team.gestorproyectos.shared.application.Transaccional;
import com.team.gestorproyectos.shared.domain.exception.NoEncontradoException;
import com.team.gestorproyectos.shared.domain.exception.ReglaNegocioException;
import com.team.gestorproyectos.usuario.domain.model.Accesos;
import com.team.gestorproyectos.usuario.domain.port.in.GestionarAccesosUseCase;
import com.team.gestorproyectos.usuario.domain.port.out.UsuarioRepositoryPort;

import lombok.RequiredArgsConstructor;

@ServicioAplicacion
@RequiredArgsConstructor
public class GestionarAccesosService implements GestionarAccesosUseCase {

    private final UsuarioRepositoryPort usuarios;
    private final ValidadorAccesos validador;

    @Override
    @Transaccional(soloLectura = true)
    public Accesos consultar(Long usuarioId) {
        existe(usuarioId);
        return usuarios.accesos(usuarioId);
    }

    @Override
    @Transaccional
    public Accesos actualizar(Long adminId, Long usuarioId, Accesos accesos) {
        existe(usuarioId);
        // Si se lo quitara, nadie podría devolvérselo desde aquí.
        if (adminId.equals(usuarioId) && !accesos.esAdmin()) {
            throw new ReglaNegocioException("No puedes quitarte a ti mismo el rol de Administrador");
        }
        validador.validarRoles(accesos.roles());
        validador.validarPermisos(accesos.permisosExtra());

        usuarios.guardarAccesos(usuarioId, accesos);
        return usuarios.accesos(usuarioId);
    }

    private void existe(Long usuarioId) {
        usuarios.buscarDelEquipo(usuarioId).orElseThrow(() -> new NoEncontradoException("No encontramos a esa persona"));
    }
}
