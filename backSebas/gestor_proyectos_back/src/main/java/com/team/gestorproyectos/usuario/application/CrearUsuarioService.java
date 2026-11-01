package com.team.gestorproyectos.usuario.application;

import com.team.gestorproyectos.shared.application.ServicioAplicacion;
import com.team.gestorproyectos.shared.application.Transaccional;
import com.team.gestorproyectos.shared.domain.exception.ReglaNegocioException;
import com.team.gestorproyectos.usuario.domain.model.NuevoUsuario;
import com.team.gestorproyectos.usuario.domain.model.UsuarioConProyectos;
import com.team.gestorproyectos.usuario.domain.port.in.CrearUsuarioUseCase;
import com.team.gestorproyectos.usuario.domain.port.out.PasswordPort;
import com.team.gestorproyectos.usuario.domain.port.out.UsuarioRepositoryPort;

import lombok.RequiredArgsConstructor;

@ServicioAplicacion
@RequiredArgsConstructor
public class CrearUsuarioService implements CrearUsuarioUseCase {

    private final UsuarioRepositoryPort usuarios;
    private final PasswordPort passwords;
    private final ValidadorAccesos validador;

    @Override
    @Transaccional
    public UsuarioConProyectos crear(NuevoUsuario nuevo) {
        if (usuarios.existeCorreo(nuevo.correo())) {
            throw new ReglaNegocioException("Ya hay un usuario con el correo " + nuevo.correo());
        }
        validador.validarRoles(nuevo.roles());

        Long id = usuarios.crear(nuevo, passwords.cifrar(nuevo.password()));
        return new UsuarioConProyectos(usuarios.buscarDelEquipo(id).orElseThrow(), nuevo.proyectoIds().size());
    }
}
