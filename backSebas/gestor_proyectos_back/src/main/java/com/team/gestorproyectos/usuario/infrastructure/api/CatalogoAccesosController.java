package com.team.gestorproyectos.usuario.infrastructure.api;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.team.gestorproyectos.usuario.domain.port.in.ConsultarCatalogoAccesosUseCase;
import com.team.gestorproyectos.usuario.infrastructure.api.dto.UsuariosDtos.PermisoResponse;
import com.team.gestorproyectos.usuario.infrastructure.api.dto.UsuariosDtos.RolResponse;
import com.team.gestorproyectos.usuario.infrastructure.api.mapper.UsuariosApiMapper;

import lombok.RequiredArgsConstructor;

/** Roles y permisos que existen: para las casillas, "Agregar permiso" y la Matriz de permisos. */
@RestController
@RequestMapping("/api/equipo/accesos")
@RequiredArgsConstructor
public class CatalogoAccesosController {

    private final ConsultarCatalogoAccesosUseCase catalogo;
    private final UsuariosApiMapper mapper;

    @GetMapping("/roles")
    public List<RolResponse> roles() {
        return mapper.aRoles(catalogo.roles());
    }

    @GetMapping("/permisos")
    public List<PermisoResponse> permisos() {
        return mapper.aPermisos(catalogo.permisos());
    }
}
