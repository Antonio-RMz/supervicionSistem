package com.team.gestorproyectos.usuario.infrastructure.api.dto;

public record LoginResponse(String token, UsuarioResponse usuario) {
}
