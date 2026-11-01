package com.team.gestorproyectos.ticket.infrastructure.api;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.team.gestorproyectos.shared.infrastructure.security.UsuarioSesion;
import com.team.gestorproyectos.ticket.domain.port.in.DetalleTicketUseCase;
import com.team.gestorproyectos.ticket.domain.port.in.PublicarParcheUseCase;
import com.team.gestorproyectos.ticket.infrastructure.api.dto.DetalleTicketDtos.ComentarioRequest;
import com.team.gestorproyectos.ticket.infrastructure.api.dto.DetalleTicketDtos.DetalleTicketResponse;
import com.team.gestorproyectos.ticket.infrastructure.api.dto.DetalleTicketDtos.NuevaSubtareaRequest;
import com.team.gestorproyectos.ticket.infrastructure.api.dto.DetalleTicketDtos.ParcheRequest;
import com.team.gestorproyectos.ticket.infrastructure.api.dto.DetalleTicketDtos.ParcheResponse;
import com.team.gestorproyectos.ticket.infrastructure.api.dto.DetalleTicketDtos.SubtareaRequest;
import com.team.gestorproyectos.ticket.infrastructure.api.mapper.DetalleTicketApiMapper;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Detalle del ticket (PDF pág. 29). Sus acciones van por {@code POST /api/equipo/tickets/{id}/movimientos}
 * (TableroController), igual que en las listas.
 */
@RestController
@RequestMapping("/api/equipo/proyectos/{proyectoId}/tickets/{ticketId}")
@RequiredArgsConstructor
public class DetalleTicketController {

    private final DetalleTicketUseCase detalleTicket;
    private final PublicarParcheUseCase publicarParche;
    private final DetalleTicketApiMapper mapper;

    @GetMapping
    public DetalleTicketResponse detalle(@PathVariable("proyectoId") Long proyectoId,
                                         @PathVariable("ticketId") Long ticketId,
                                         @AuthenticationPrincipal UsuarioSesion sesion) {
        return mapper.aDetalle(detalleTicket.detalle(proyectoId, ticketId, sesion.id(), sesion.esAdmin()));
    }

    /** Publicar parche (PDF pág. 23): salen todos los hotfix aprobados del proyecto en una versión nueva. */
    @PostMapping("/parche")
    public ParcheResponse publicarParche(@PathVariable("proyectoId") Long proyectoId,
                                         @PathVariable("ticketId") Long ticketId,
                                         @Valid @RequestBody ParcheRequest request,
                                         @AuthenticationPrincipal UsuarioSesion sesion) {
        return mapper.aParche(publicarParche.publicar(proyectoId, ticketId, request.version(), sesion.id(),
                sesion.esAdmin()));
    }

    @PutMapping("/subtareas/{subtareaId}")
    public void marcarSubtarea(@PathVariable("proyectoId") Long proyectoId,
                               @PathVariable("ticketId") Long ticketId,
                               @PathVariable("subtareaId") Long subtareaId,
                               @RequestBody SubtareaRequest request,
                               @AuthenticationPrincipal UsuarioSesion sesion) {
        detalleTicket.marcarSubtarea(proyectoId, ticketId, subtareaId, request.hecha(), sesion.id(), sesion.esAdmin());
    }

    @PostMapping("/subtareas")
    @ResponseStatus(HttpStatus.CREATED)
    public void agregarSubtarea(@PathVariable("proyectoId") Long proyectoId,
                                @PathVariable("ticketId") Long ticketId,
                                @Valid @RequestBody NuevaSubtareaRequest request,
                                @AuthenticationPrincipal UsuarioSesion sesion) {
        detalleTicket.agregarSubtarea(proyectoId, ticketId, request.descripcion(), sesion.id(), sesion.esAdmin());
    }

    @DeleteMapping("/subtareas/{subtareaId}")
    public void quitarSubtarea(@PathVariable("proyectoId") Long proyectoId,
                               @PathVariable("ticketId") Long ticketId,
                               @PathVariable("subtareaId") Long subtareaId,
                               @AuthenticationPrincipal UsuarioSesion sesion) {
        detalleTicket.quitarSubtarea(proyectoId, ticketId, subtareaId, sesion.id(), sesion.esAdmin());
    }

    @PostMapping("/comentarios")
    @ResponseStatus(HttpStatus.CREATED)
    public void comentar(@PathVariable("proyectoId") Long proyectoId,
                         @PathVariable("ticketId") Long ticketId,
                         @Valid @RequestBody ComentarioRequest request,
                         @AuthenticationPrincipal UsuarioSesion sesion) {
        detalleTicket.comentar(proyectoId, ticketId, request.texto(), sesion.id(), sesion.esAdmin());
    }
}
