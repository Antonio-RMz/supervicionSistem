package com.team.gestorproyectos.ticket.infrastructure.api.mapper;

import org.mapstruct.Mapper;

import com.team.gestorproyectos.ticket.domain.model.DetalleTicket;
import com.team.gestorproyectos.ticket.domain.model.Parche;
import com.team.gestorproyectos.ticket.infrastructure.api.dto.DetalleTicketDtos.DetalleTicketResponse;
import com.team.gestorproyectos.ticket.infrastructure.api.dto.DetalleTicketDtos.ParcheResponse;

@Mapper(componentModel = "spring")
public interface DetalleTicketApiMapper {

    DetalleTicketResponse aDetalle(DetalleTicket detalle);

    ParcheResponse aParche(Parche parche);
}
