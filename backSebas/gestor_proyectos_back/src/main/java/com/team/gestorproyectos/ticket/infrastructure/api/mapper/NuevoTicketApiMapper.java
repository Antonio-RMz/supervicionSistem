package com.team.gestorproyectos.ticket.infrastructure.api.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.team.gestorproyectos.ticket.domain.model.EdicionTicket;
import com.team.gestorproyectos.ticket.domain.model.NuevoTicket;
import com.team.gestorproyectos.ticket.domain.model.PreparacionTicket;
import com.team.gestorproyectos.ticket.infrastructure.api.dto.NuevoTicketDtos.EdicionResponse;
import com.team.gestorproyectos.ticket.infrastructure.api.dto.NuevoTicketDtos.NuevoTicketRequest;
import com.team.gestorproyectos.ticket.infrastructure.api.dto.NuevoTicketDtos.PreparacionResponse;
import com.team.gestorproyectos.ticket.infrastructure.api.dto.NuevoTicketDtos.TicketEditableResponse;

@Mapper(componentModel = "spring")
public interface NuevoTicketApiMapper {

    @Mapping(target = "capas", expression = "java(com.team.gestorproyectos.ticket.domain.model.NuevoTicket.CAPAS)")
    PreparacionResponse aPreparacion(PreparacionTicket preparacion);

    NuevoTicket aNuevoTicket(NuevoTicketRequest request);

    NuevoTicketRequest aRequest(NuevoTicket datos);


    default EdicionResponse aEdicion(EdicionTicket edicion) {
        return new EdicionResponse(aPreparacion(edicion.preparacion()), new TicketEditableResponse(
                edicion.ticket().id(), edicion.ticket().codigo(), aRequest(edicion.ticket().datos())));
    }
}
