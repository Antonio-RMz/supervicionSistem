package com.team.gestorproyectos.usuario.infrastructure.security;

import org.springframework.stereotype.Component;

import com.team.gestorproyectos.shared.infrastructure.security.JwtService;
import com.team.gestorproyectos.shared.infrastructure.security.UsuarioSesion;
import com.team.gestorproyectos.usuario.domain.model.Usuario;
import com.team.gestorproyectos.usuario.domain.port.out.TokenPort;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TokenAdapter implements TokenPort {

    private final JwtService jwtService;

    @Override
    public String generar(Usuario usuario) {
        return jwtService.generar(new UsuarioSesion(
                usuario.id(),
                usuario.correo(),
                usuario.tipo().name(),
                usuario.clienteId(),
                usuario.roles().stream().sorted().toList()));
    }
}
