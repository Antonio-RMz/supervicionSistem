package com.team.gestorproyectos.proyecto.infrastructure.api;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.team.gestorproyectos.proyecto.domain.port.in.ListarProyectosDelUsuarioUseCase;
import com.team.gestorproyectos.proyecto.infrastructure.api.dto.ProyectoResumenResponse;
import com.team.gestorproyectos.proyecto.infrastructure.api.mapper.ProyectoApiMapper;
import com.team.gestorproyectos.shared.infrastructure.security.UsuarioSesion;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/equipo/proyectos")
@RequiredArgsConstructor
public class ProyectoController {

    private final ListarProyectosDelUsuarioUseCase listarProyectos;
    private final ProyectoApiMapper mapper;

    @GetMapping
    public List<ProyectoResumenResponse> misProyectos(@AuthenticationPrincipal UsuarioSesion sesion) {
        return mapper.aResponses(listarProyectos.listar(sesion.id(), sesion.esAdmin()));
    }
}
