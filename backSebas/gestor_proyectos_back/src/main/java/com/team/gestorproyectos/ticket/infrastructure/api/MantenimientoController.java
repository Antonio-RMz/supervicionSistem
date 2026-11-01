package com.team.gestorproyectos.ticket.infrastructure.api;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.team.gestorproyectos.shared.infrastructure.security.UsuarioSesion;
import com.team.gestorproyectos.ticket.domain.port.in.ConsultarMantenimientoUseCase;
import com.team.gestorproyectos.ticket.domain.port.in.CrearEnMantenimientoUseCase;
import com.team.gestorproyectos.ticket.infrastructure.api.dto.MantenimientoDtos.MantenimientoResponse;
import com.team.gestorproyectos.ticket.infrastructure.api.dto.MantenimientoDtos.NuevoTicketRequest;
import com.team.gestorproyectos.ticket.infrastructure.api.dto.MantenimientoDtos.PreparacionResponse;
import com.team.gestorproyectos.ticket.infrastructure.api.dto.MantenimientoDtos.ReporteFallaRequest;
import com.team.gestorproyectos.ticket.infrastructure.api.dto.NuevoTicketDtos.TicketCreadoResponse;
import com.team.gestorproyectos.ticket.infrastructure.api.mapper.MantenimientoApiMapper;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Vista de Mantenimiento (PDF pág. 37) con sus modales Nuevo ticket (pág. 38) y Reportar falla (pág. 39).
 * Publicar el parche se hace con POST /proyectos/{p}/tickets/{t}/parche (el mismo del detalle del bug).
 */
@RestController
@RequestMapping("/api/equipo/proyectos/{proyectoId}/mantenimiento")
@RequiredArgsConstructor
public class MantenimientoController {

    private final ConsultarMantenimientoUseCase consultarMantenimiento;
    private final CrearEnMantenimientoUseCase crearEnMantenimiento;
    private final MantenimientoApiMapper mapper;

    @GetMapping
    public MantenimientoResponse mantenimiento(@PathVariable("proyectoId") Long proyectoId,
                                               @AuthenticationPrincipal UsuarioSesion sesion) {
        return mapper.aMantenimiento(consultarMantenimiento.mantenimiento(proyectoId, sesion.id(), sesion.esAdmin()));
    }

    /** Módulos, equipo, historias y versión en producción para los dos modales. */
    @GetMapping("/nuevo")
    public PreparacionResponse preparar(@PathVariable("proyectoId") Long proyectoId,
                                        @AuthenticationPrincipal UsuarioSesion sesion) {
        return mapper.aPreparacion(crearEnMantenimiento.preparar(proyectoId, sesion.id(), sesion.esAdmin()));
    }

    @PostMapping("/tickets")
    @ResponseStatus(HttpStatus.CREATED)
    public TicketCreadoResponse crearTicket(@PathVariable("proyectoId") Long proyectoId,
                                            @Valid @RequestBody NuevoTicketRequest request,
                                            @AuthenticationPrincipal UsuarioSesion sesion) {
        return new TicketCreadoResponse(crearEnMantenimiento.crearTicket(proyectoId, sesion.id(), sesion.esAdmin(),
                mapper.aNuevoTicket(request)));
    }

    @PostMapping("/fallas")
    @ResponseStatus(HttpStatus.CREATED)
    public TicketCreadoResponse reportarFalla(@PathVariable("proyectoId") Long proyectoId,
                                              @Valid @RequestBody ReporteFallaRequest request,
                                              @AuthenticationPrincipal UsuarioSesion sesion) {
        return new TicketCreadoResponse(crearEnMantenimiento.reportarFalla(proyectoId, sesion.id(), sesion.esAdmin(),
                mapper.aReporte(request)));
    }
}
