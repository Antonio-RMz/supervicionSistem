package com.team.gestorproyectos.usuario.infrastructure.api;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.team.gestorproyectos.usuario.domain.port.in.IniciarSesionUseCase;
import com.team.gestorproyectos.usuario.infrastructure.api.dto.LoginRequest;
import com.team.gestorproyectos.usuario.infrastructure.api.dto.LoginResponse;
import com.team.gestorproyectos.usuario.infrastructure.api.mapper.AuthApiMapper;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/** Adaptador de entrada (REST). Solo traduce HTTP <-> caso de uso; no tiene lógica. */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final IniciarSesionUseCase iniciarSesion;
    private final AuthApiMapper mapper;

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return mapper.aRespuesta(iniciarSesion.iniciarSesion(request.correo(), request.password()));
    }
}
