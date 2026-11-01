package com.team.gestorproyectos.dashboard.infrastructure.api;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.team.gestorproyectos.dashboard.domain.port.in.ConsultarDashboardUseCase;
import com.team.gestorproyectos.dashboard.infrastructure.api.dto.DashboardDtos.DashboardResponse;
import com.team.gestorproyectos.dashboard.infrastructure.api.mapper.DashboardApiMapper;
import com.team.gestorproyectos.shared.infrastructure.security.UsuarioSesion;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/equipo/proyectos/{proyectoId}/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final ConsultarDashboardUseCase consultarDashboard;
    private final DashboardApiMapper mapper;

    @GetMapping
    public DashboardResponse dashboard(@PathVariable("proyectoId") Long proyectoId,
                                       @AuthenticationPrincipal UsuarioSesion sesion) {
        return mapper.aResponse(consultarDashboard.consultar(proyectoId, sesion.id(), sesion.esAdmin()));
    }
}
