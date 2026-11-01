package com.team.gestorproyectos.usuario.infrastructure.security;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.team.gestorproyectos.usuario.domain.port.out.PasswordPort;

import lombok.RequiredArgsConstructor;

/** Cifra y compara contraseñas con BCrypt (el PasswordEncoder de SecurityConfig). */
@Component
@RequiredArgsConstructor
public class PasswordAdapter implements PasswordPort {

    private final PasswordEncoder passwordEncoder;

    @Override
    public boolean coincide(String passwordPlano, String passwordHash) {
        return passwordEncoder.matches(passwordPlano, passwordHash);
    }

    @Override
    public String cifrar(String passwordPlano) {
        return passwordEncoder.encode(passwordPlano);
    }
}
