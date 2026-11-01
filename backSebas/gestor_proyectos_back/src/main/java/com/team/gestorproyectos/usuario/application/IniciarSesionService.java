package com.team.gestorproyectos.usuario.application;

import com.team.gestorproyectos.shared.application.ServicioAplicacion;
import com.team.gestorproyectos.shared.application.Transaccional;
import com.team.gestorproyectos.usuario.domain.exception.CredencialesInvalidasException;
import com.team.gestorproyectos.usuario.domain.model.SesionIniciada;
import com.team.gestorproyectos.usuario.domain.model.Usuario;
import com.team.gestorproyectos.usuario.domain.port.in.IniciarSesionUseCase;
import com.team.gestorproyectos.usuario.domain.port.out.PasswordPort;
import com.team.gestorproyectos.usuario.domain.port.out.TokenPort;
import com.team.gestorproyectos.usuario.domain.port.out.UsuarioRepositoryPort;

import lombok.RequiredArgsConstructor;

@ServicioAplicacion
@RequiredArgsConstructor
public class IniciarSesionService implements IniciarSesionUseCase {

    private final UsuarioRepositoryPort usuarios;
    private final PasswordPort passwords;
    private final TokenPort tokens;

    @Override
    @Transaccional
    public SesionIniciada iniciarSesion(String correo, String password) {
        Usuario usuario = usuarios.buscarPorCorreo(correo.trim().toLowerCase())
                .filter(Usuario::activo)
                .filter(u -> passwords.coincide(password, u.passwordHash()))
                .orElseThrow(CredencialesInvalidasException::new);

        usuarios.registrarAcceso(usuario.id());
        return new SesionIniciada(tokens.generar(usuario), usuario);
    }
}
