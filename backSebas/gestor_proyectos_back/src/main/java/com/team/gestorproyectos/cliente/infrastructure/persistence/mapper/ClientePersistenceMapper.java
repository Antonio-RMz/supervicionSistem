package com.team.gestorproyectos.cliente.infrastructure.persistence.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.team.gestorproyectos.cliente.domain.model.Cliente;
import com.team.gestorproyectos.cliente.domain.model.DatosCliente;
import com.team.gestorproyectos.cliente.domain.model.ProyectoDeCliente;
import com.team.gestorproyectos.cliente.infrastructure.persistence.entity.ClienteEntity;
import com.team.gestorproyectos.cliente.infrastructure.persistence.repository.ClienteJpaRepository.ProyectoFila;

/** Entidad JPA <-> modelo de dominio de Clientes. */
@Mapper(componentModel = "spring")
public interface ClientePersistenceMapper {

    Cliente aDominio(ClienteEntity entity);

    ProyectoDeCliente aProyecto(ProyectoFila fila);

    /** Copia lo capturado sobre la entidad (nueva o existente). El id no se toca. */
    @Mapping(target = "id", ignore = true)
    void copiar(DatosCliente datos, @MappingTarget ClienteEntity entity);
}
