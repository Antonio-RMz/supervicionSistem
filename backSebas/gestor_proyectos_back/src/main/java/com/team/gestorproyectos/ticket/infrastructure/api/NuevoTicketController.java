package com.team.gestorproyectos.ticket.infrastructure.api;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.team.gestorproyectos.shared.infrastructure.security.UsuarioSesion;
import com.team.gestorproyectos.ticket.domain.port.in.CrearTicketUseCase;
import com.team.gestorproyectos.ticket.infrastructure.api.dto.NuevoTicketDtos.EdicionResponse;
import com.team.gestorproyectos.ticket.infrastructure.api.dto.NuevoTicketDtos.NuevoTicketRequest;
import com.team.gestorproyectos.ticket.infrastructure.api.dto.NuevoTicketDtos.PreparacionResponse;
import com.team.gestorproyectos.ticket.infrastructure.api.dto.NuevoTicketDtos.TicketCreadoResponse;
import com.team.gestorproyectos.ticket.infrastructure.api.mapper.NuevoTicketApiMapper;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/** Nuevo ticket de trabajo (PDF pág. 19) y editarlo mientras no empiece. */
@RestController
@RequestMapping("/api/equipo/proyectos/{proyectoId}/tickets")
@RequiredArgsConstructor
public class NuevoTicketController {

    private final CrearTicketUseCase crearTicket;
    private final NuevoTicketApiMapper mapper;

    /** Fases con sus historias listas, módulos, equipo, números siguientes y tickets de los que puede depender. */
    @GetMapping("/nuevo")
    public PreparacionResponse preparar(@PathVariable("proyectoId") Long proyectoId,
                                        @AuthenticationPrincipal UsuarioSesion sesion) {
        return mapper.aPreparacion(crearTicket.preparar(proyectoId, sesion.id(), sesion.esAdmin()));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TicketCreadoResponse crear(@PathVariable("proyectoId") Long proyectoId,
                                      @Valid @RequestBody NuevoTicketRequest request,
                                      @AuthenticationPrincipal UsuarioSesion sesion) {
        return new TicketCreadoResponse(crearTicket.crear(proyectoId, sesion.id(), sesion.esAdmin(),
                mapper.aNuevoTicket(request)));
    }

    @GetMapping("/{ticketId}/edicion")
    public EdicionResponse edicion(@PathVariable("proyectoId") Long proyectoId,
                                   @PathVariable("ticketId") Long ticketId,
                                   @AuthenticationPrincipal UsuarioSesion sesion) {
        return mapper.aEdicion(crearTicket.edicion(proyectoId, ticketId, sesion.id(), sesion.esAdmin()));
    }

    /** Tipo, fase, historia y subtareas no cambian aquí. */
    @PutMapping("/{ticketId}")
    public void editar(@PathVariable("proyectoId") Long proyectoId,
                       @PathVariable("ticketId") Long ticketId,
                       @Valid @RequestBody NuevoTicketRequest request,
                       @AuthenticationPrincipal UsuarioSesion sesion) {
        crearTicket.editar(proyectoId, ticketId, sesion.id(), sesion.esAdmin(), mapper.aNuevoTicket(request));
    }
}
