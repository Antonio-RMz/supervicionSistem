package com.team.gestorproyectos.dashboard.infrastructure.persistence.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import com.team.gestorproyectos.dashboard.domain.model.ConteoCierres;
import com.team.gestorproyectos.shared.domain.model.ItemTrabajo;
import com.team.gestorproyectos.shared.domain.model.Movimiento;
import com.team.gestorproyectos.shared.domain.model.Persona;
import com.team.gestorproyectos.dashboard.domain.model.ProyectoDashboard;
import com.team.gestorproyectos.dashboard.domain.port.out.DashboardConsultaPort.FaseBasica;
import com.team.gestorproyectos.dashboard.infrastructure.persistence.repository.DashboardJpaRepository.CierresFila;
import com.team.gestorproyectos.dashboard.infrastructure.persistence.repository.DashboardJpaRepository.ItemFila;
import com.team.gestorproyectos.dashboard.infrastructure.persistence.repository.DashboardJpaRepository.MovimientoFila;
import com.team.gestorproyectos.dashboard.infrastructure.persistence.repository.DashboardJpaRepository.PersonaFila;
import com.team.gestorproyectos.dashboard.infrastructure.persistence.repository.DashboardJpaRepository.ProyectoFila;
import com.team.gestorproyectos.proyecto.infrastructure.persistence.entity.FaseEntity;

/** Filas de las consultas -> modelos del dominio del Dashboard. */
@Mapper(componentModel = "spring")
public interface DashboardPersistenceMapper {

    ProyectoDashboard aProyecto(ProyectoFila fila);

    ItemTrabajo aItem(ItemFila fila);

    List<ItemTrabajo> aItems(List<ItemFila> filas);

    Persona aPersona(PersonaFila fila);

    List<Persona> aPersonas(List<PersonaFila> filas);

    ConteoCierres aCierres(CierresFila fila);

    Movimiento aMovimiento(MovimientoFila fila);

    List<Movimiento> aMovimientos(List<MovimientoFila> filas);

    FaseBasica aFase(FaseEntity entity);

    List<FaseBasica> aFases(List<FaseEntity> entities);
}
