package com.team.gestorproyectos.usuario.infrastructure.persistence.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import com.team.gestorproyectos.shared.domain.model.ItemTrabajo;
import com.team.gestorproyectos.shared.domain.model.Movimiento;
import com.team.gestorproyectos.shared.domain.model.Persona;
import com.team.gestorproyectos.usuario.domain.model.ComentarioHecho;
import com.team.gestorproyectos.shared.domain.model.ItemDeProyecto;
import com.team.gestorproyectos.usuario.domain.model.ProyectoDeUsuario;
import com.team.gestorproyectos.usuario.infrastructure.persistence.repository.PerfilUsuarioJpaRepository.ComentarioFila;
import com.team.gestorproyectos.usuario.infrastructure.persistence.repository.PerfilUsuarioJpaRepository.ItemFila;
import com.team.gestorproyectos.usuario.infrastructure.persistence.repository.PerfilUsuarioJpaRepository.MovimientoFila;
import com.team.gestorproyectos.usuario.infrastructure.persistence.repository.PerfilUsuarioJpaRepository.PersonaFila;
import com.team.gestorproyectos.usuario.infrastructure.persistence.repository.PerfilUsuarioJpaRepository.ProyectoFila;

/** Filas de las consultas del detalle de una persona -> modelos del dominio. */
@Mapper(componentModel = "spring")
public interface PerfilUsuarioPersistenceMapper {

    ItemTrabajo aItem(ItemFila fila);

    default ItemDeProyecto aItemDeProyecto(ItemFila fila) {
        return new ItemDeProyecto(aItem(fila), fila.getLiderId(), fila.getUltimoEstadoAnterior());
    }

    default List<ItemDeProyecto> aItemsDeProyecto(List<ItemFila> filas) {
        return filas.stream().map(this::aItemDeProyecto).toList();
    }

    List<Persona> aPersonas(List<PersonaFila> filas);

    List<Movimiento> aMovimientos(List<MovimientoFila> filas);

    List<ComentarioHecho> aComentarios(List<ComentarioFila> filas);

    List<ProyectoDeUsuario> aProyectos(List<ProyectoFila> filas);
}
