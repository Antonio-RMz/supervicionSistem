package com.team.gestorproyectos.proyecto.infrastructure.api.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import com.team.gestorproyectos.proyecto.domain.model.UsuarioDelSistema;
import com.team.gestorproyectos.proyecto.domain.model.UsuariosDelSistema;
import com.team.gestorproyectos.proyecto.infrastructure.api.dto.UsuariosDelSistemaDtos.UsuarioDelSistemaDto;
import com.team.gestorproyectos.proyecto.infrastructure.api.dto.UsuariosDelSistemaDtos.UsuariosDelSistemaResponse;

@Mapper(componentModel = "spring")
public interface UsuariosDelSistemaApiMapper {

    UsuariosDelSistemaResponse aRespuesta(UsuariosDelSistema usuarios);

    List<UsuarioDelSistema> aUsuarios(List<UsuarioDelSistemaDto> usuarios);
}
