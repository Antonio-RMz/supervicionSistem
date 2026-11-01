package com.team.gestorproyectos.ticket.domain.port.in;

import java.util.List;

import com.team.gestorproyectos.ticket.domain.model.TicketMio;

/** Mis tickets en un proyecto donde soy miembro (o cualquiera si soy Admin). */
public interface ListarMisTicketsUseCase {

    List<TicketMio> listar(Long proyectoId, Long usuarioId, boolean esAdmin);
}
