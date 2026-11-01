package com.team.gestorproyectos.ticket.infrastructure.api.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.team.gestorproyectos.ticket.domain.model.TicketMio;
import com.team.gestorproyectos.ticket.infrastructure.api.dto.MisTicketsDtos.TicketMioResponse;
import com.team.gestorproyectos.shared.domain.model.CodigoItem;

@Mapper(componentModel = "spring")
public interface MisTicketsApiMapper {

    @Mapping(target = "id", source = "fila.item.item.id")
    @Mapping(target = "tipo", source = "fila.item.item.tipo")
    @Mapping(target = "codigo", expression = "java(ticket.fila().item().item().codigo())")
    @Mapping(target = "titulo", source = "fila.item.item.titulo")
    @Mapping(target = "estado", source = "fila.item.item.estado")
    @Mapping(target = "prioridad", source = "fila.item.item.prioridad")
    @Mapping(target = "faseId", source = "fila.faseId")
    @Mapping(target = "faseNumero", source = "fila.faseNumero")
    @Mapping(target = "historiaId", source = "fila.historiaId")
    @Mapping(target = "historiaCodigo", expression = "java(historiaCodigo(ticket))")
    @Mapping(target = "responsableId", source = "responsable.id")
    @Mapping(target = "responsableNombre", source = "responsable.nombre")
    @Mapping(target = "responsableIniciales", source = "fila.responsableIniciales")
    @Mapping(target = "actualizadoEn", source = "fila.item.item.actualizadoEn")
    TicketMioResponse aResponse(TicketMio ticket);

    List<TicketMioResponse> aResponses(List<TicketMio> tickets);

    default String historiaCodigo(TicketMio ticket) {
        Integer numero = ticket.fila().historiaNumero();
        return numero == null ? null : CodigoItem.historia(numero);
    }
}
