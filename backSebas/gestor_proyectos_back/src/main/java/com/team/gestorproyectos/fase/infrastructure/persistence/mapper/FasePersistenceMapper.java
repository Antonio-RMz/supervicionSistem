package com.team.gestorproyectos.fase.infrastructure.persistence.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import com.team.gestorproyectos.fase.domain.model.InfoFase.ModuloDeFase;
import com.team.gestorproyectos.fase.infrastructure.persistence.repository.FaseGestionJpaRepository.ModuloFila;
import com.team.gestorproyectos.fase.infrastructure.persistence.repository.FaseGestionJpaRepository.MovimientoFila;
import com.team.gestorproyectos.shared.domain.model.Movimiento;

/** Filas de las consultas del detalle de la fase -> modelos del dominio. */
@Mapper(componentModel = "spring")
public interface FasePersistenceMapper {

    List<ModuloDeFase> aModulos(List<ModuloFila> filas);

    List<Movimiento> aMovimientos(List<MovimientoFila> filas);
}
