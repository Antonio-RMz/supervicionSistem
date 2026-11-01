package com.team.gestorproyectos.proyecto.infrastructure.api;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.team.gestorproyectos.proyecto.domain.port.in.UsuariosDelSistemaUseCase;
import com.team.gestorproyectos.proyecto.infrastructure.api.dto.UsuariosDelSistemaDtos.GuardarUsuariosRequest;
import com.team.gestorproyectos.proyecto.infrastructure.api.dto.UsuariosDelSistemaDtos.UsuariosDelSistemaResponse;
import com.team.gestorproyectos.proyecto.infrastructure.api.mapper.UsuariosDelSistemaApiMapper;
import com.team.gestorproyectos.shared.infrastructure.security.UsuarioSesion;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/** Pestaña Usuarios del sistema: quiénes usan el software que se construye. */
@RestController
@RequestMapping("/api/equipo/proyectos/{proyectoId}/usuarios-sistema")
@RequiredArgsConstructor
public class UsuariosDelSistemaController {

    private final UsuariosDelSistemaUseCase usuariosDelSistema;
    private final UsuariosDelSistemaApiMapper mapper;

    @GetMapping
    public UsuariosDelSistemaResponse consultar(@PathVariable("proyectoId") Long proyectoId,
                                                @AuthenticationPrincipal UsuarioSesion sesion) {
        return mapper.aRespuesta(usuariosDelSistema.consultar(proyectoId, sesion.id(), sesion.esAdmin()));
    }

    @PutMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void guardar(@PathVariable("proyectoId") Long proyectoId, @Valid @RequestBody GuardarUsuariosRequest request,
                        @AuthenticationPrincipal UsuarioSesion sesion) {
        usuariosDelSistema.guardar(proyectoId, sesion.id(), sesion.esAdmin(), mapper.aUsuarios(request.usuarios()));
    }
}
