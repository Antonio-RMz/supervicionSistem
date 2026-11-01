package com.team.gestorproyectos.ticket.infrastructure.api;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.team.gestorproyectos.shared.infrastructure.security.UsuarioSesion;
import com.team.gestorproyectos.ticket.domain.port.in.ConsultarTableroUseCase;
import com.team.gestorproyectos.ticket.domain.model.DatosMovimiento;
import com.team.gestorproyectos.ticket.domain.port.in.MoverTicketUseCase;
import com.team.gestorproyectos.ticket.infrastructure.api.dto.TableroDtos.MoverRequest;
import com.team.gestorproyectos.ticket.infrastructure.api.dto.TableroDtos.MoverResponse;
import com.team.gestorproyectos.ticket.infrastructure.api.dto.TableroDtos.TableroResponse;
import com.team.gestorproyectos.ticket.infrastructure.api.mapper.TableroApiMapper;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/** Tablero (PDF pág. 12) y la acción de mover un ticket, que también usan Mis tickets y el Dashboard. */
@RestController
@RequestMapping("/api/equipo")
@RequiredArgsConstructor
public class TableroController {

    private final ConsultarTableroUseCase consultarTablero;
    private final MoverTicketUseCase moverTicket;
    private final TableroApiMapper mapper;

    /** Sin faseId se muestra la fase en curso. */
    @GetMapping("/proyectos/{proyectoId}/tablero")
    public TableroResponse tablero(@PathVariable("proyectoId") Long proyectoId,
                                   @RequestParam(name = "faseId", required = false) Long faseId,
                                   @AuthenticationPrincipal UsuarioSesion sesion) {
        return mapper.aResponse(consultarTablero.consultar(proyectoId, faseId, sesion.id(), sesion.esAdmin()));
    }

    @PostMapping("/tickets/{ticketId}/movimientos")
    public MoverResponse mover(@PathVariable("ticketId") Long ticketId,
                               @Valid @RequestBody MoverRequest request,
                               @AuthenticationPrincipal UsuarioSesion sesion) {
        return mapper.aMoverResponse(moverTicket.mover(ticketId, sesion.id(), new DatosMovimiento(request.tipo(),
                request.estadoEsperado(), request.motivo(), request.bloqueadoPorId(), request.destrabadorId(),
                request.severidad(), request.responsableId())));
    }
}
