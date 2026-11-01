package com.team.gestorproyectos.ticket.infrastructure.api;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.team.gestorproyectos.shared.infrastructure.security.UsuarioSesion;
import com.team.gestorproyectos.ticket.domain.port.in.ListarMisTicketsUseCase;
import com.team.gestorproyectos.ticket.infrastructure.api.dto.MisTicketsDtos.TicketMioResponse;
import com.team.gestorproyectos.ticket.infrastructure.api.mapper.MisTicketsApiMapper;

import lombok.RequiredArgsConstructor;

/** Mis tickets (PDF pág. 11), siempre dentro del proyecto elegido en el selector. */
@RestController
@RequestMapping("/api/equipo/proyectos/{proyectoId}/mis-tickets")
@RequiredArgsConstructor
public class MisTicketsController {

    private final ListarMisTicketsUseCase listarMisTickets;
    private final MisTicketsApiMapper mapper;

    @GetMapping
    public List<TicketMioResponse> listar(@PathVariable("proyectoId") Long proyectoId,
                                          @AuthenticationPrincipal UsuarioSesion sesion) {
        return mapper.aResponses(listarMisTickets.listar(proyectoId, sesion.id(), sesion.esAdmin()));
    }
}
