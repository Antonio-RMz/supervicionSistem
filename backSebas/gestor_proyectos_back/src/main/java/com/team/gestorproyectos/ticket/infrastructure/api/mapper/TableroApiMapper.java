package com.team.gestorproyectos.ticket.infrastructure.api.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.team.gestorproyectos.ticket.domain.model.Tarjeta;
import com.team.gestorproyectos.ticket.domain.model.Transicion;
import com.team.gestorproyectos.ticket.domain.model.VistaTablero;
import com.team.gestorproyectos.ticket.infrastructure.api.dto.TableroDtos.MoverResponse;
import com.team.gestorproyectos.ticket.infrastructure.api.dto.TableroDtos.TableroResponse;
import com.team.gestorproyectos.ticket.infrastructure.api.dto.TableroDtos.TarjetaResponse;
import com.team.gestorproyectos.shared.domain.model.CodigoItem;

@Mapper(componentModel = "spring")
public interface TableroApiMapper {

    TableroResponse aResponse(VistaTablero vista);

    @Mapping(target = "id", source = "fila.item.item.id")
    @Mapping(target = "tipo", source = "fila.item.item.tipo")
    @Mapping(target = "codigo", expression = "java(tarjeta.fila().item().item().codigo())")
    @Mapping(target = "titulo", source = "fila.item.item.titulo")
    @Mapping(target = "estado", source = "fila.item.item.estado")
    @Mapping(target = "prioridad", source = "fila.item.item.prioridad")
    @Mapping(target = "historiaId", source = "fila.historiaId")
    @Mapping(target = "historiaCodigo", expression = "java(historiaCodigo(tarjeta))")
    @Mapping(target = "responsableId", source = "responsable.id")
    @Mapping(target = "responsableNombre", source = "responsable.nombre")
    @Mapping(target = "responsableIniciales", source = "fila.responsableIniciales")
    TarjetaResponse aTarjeta(Tarjeta tarjeta);

    @Mapping(target = "estado", source = "estadoNuevo")
    @Mapping(target = "rama", source = "ramaNueva")
    MoverResponse aMoverResponse(Transicion transicion);

    default String historiaCodigo(Tarjeta tarjeta) {
        Integer numero = tarjeta.fila().historiaNumero();
        return numero == null ? null : CodigoItem.historia(numero);
    }
}
