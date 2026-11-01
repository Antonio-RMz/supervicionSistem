package com.team.gestorproyectos.proyecto.infrastructure.persistence.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import com.team.gestorproyectos.proyecto.domain.model.ProyectoResumen;
import com.team.gestorproyectos.proyecto.infrastructure.persistence.repository.ProyectoJpaRepository.ProyectoResumenFila;

@Mapper(componentModel = "spring")
public interface ProyectoPersistenceMapper {

    ProyectoResumen aResumen(ProyectoResumenFila fila);

    List<ProyectoResumen> aResumenes(List<ProyectoResumenFila> filas);
}
