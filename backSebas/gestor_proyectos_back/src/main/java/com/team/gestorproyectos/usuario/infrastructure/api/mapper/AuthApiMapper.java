package com.team.gestorproyectos.usuario.infrastructure.api.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.team.gestorproyectos.usuario.domain.model.SesionIniciada;
import com.team.gestorproyectos.usuario.domain.model.Usuario;
import com.team.gestorproyectos.usuario.infrastructure.api.dto.LoginResponse;
import com.team.gestorproyectos.usuario.infrastructure.api.dto.UsuarioResponse;

/** Modelo de dominio -> DTO de respuesta. */
@Mapper(componentModel = "spring")
public interface AuthApiMapper {

    LoginResponse aRespuesta(SesionIniciada sesion);

    @Mapping(target = "nombreCompleto", expression = "java(usuario.nombreCompleto())")
    @Mapping(target = "iniciales", expression = "java(usuario.iniciales())")
    UsuarioResponse aUsuarioResponse(Usuario usuario);
}
