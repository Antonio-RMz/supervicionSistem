package com.team.gestorproyectos.proyecto.infrastructure.api.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import com.team.gestorproyectos.proyecto.domain.model.ProyectoResumen;
import com.team.gestorproyectos.proyecto.infrastructure.api.dto.ProyectoResumenResponse;

@Mapper(componentModel = "spring")
public interface ProyectoApiMapper {

    ProyectoResumenResponse aResponse(ProyectoResumen proyecto);

    List<ProyectoResumenResponse> aResponses(List<ProyectoResumen> proyectos);
}
