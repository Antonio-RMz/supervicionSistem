package com.team.gestorproyectos.proyecto.infrastructure.api.mapper;

import org.mapstruct.Mapper;

import com.team.gestorproyectos.proyecto.domain.model.ReleasesProyecto;
import com.team.gestorproyectos.proyecto.infrastructure.api.dto.ReleasesDtos.ReleasesResponse;

@Mapper(componentModel = "spring")
public interface ReleasesApiMapper {

    ReleasesResponse aReleases(ReleasesProyecto releases);
}
