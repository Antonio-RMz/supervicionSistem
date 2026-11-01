package com.team.gestorproyectos.portalcliente.infrastructure.api;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.team.gestorproyectos.portalcliente.domain.port.in.ConsultarAvanceUseCase;
import com.team.gestorproyectos.portalcliente.domain.port.in.ListarMisProyectosUseCase;
import com.team.gestorproyectos.portalcliente.infrastructure.api.dto.PortalClienteDtos.AvanceResponse;
import com.team.gestorproyectos.portalcliente.infrastructure.api.dto.PortalClienteDtos.MiProyectoResponse;
import com.team.gestorproyectos.portalcliente.infrastructure.api.mapper.PortalClienteApiMapper;
import com.team.gestorproyectos.shared.infrastructure.security.UsuarioSesion;

import lombok.RequiredArgsConstructor;

/** Portal de avance. Solo entran usuarios CLIENTE (ver SecurityConfig) y solo ven proyectos de su empresa. */
@RestController
@RequestMapping("/api/portal")
@RequiredArgsConstructor
public class PortalClienteController {

    private final ListarMisProyectosUseCase listarMisProyectos;
    private final ConsultarAvanceUseCase consultarAvance;
    private final PortalClienteApiMapper mapper;

    @GetMapping("/proyectos")
    public List<MiProyectoResponse> misProyectos(@AuthenticationPrincipal UsuarioSesion sesion) {
        return mapper.aMisProyectos(listarMisProyectos.listar(sesion.clienteId()));
    }

    @GetMapping("/proyectos/{id}/avance")
    public AvanceResponse avance(@PathVariable("id") Long proyectoId, @AuthenticationPrincipal UsuarioSesion sesion) {
        return mapper.aAvance(consultarAvance.consultar(proyectoId, sesion.clienteId()));
    }
}
