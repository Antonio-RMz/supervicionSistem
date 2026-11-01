package com.team.gestorproyectos.proyecto.infrastructure.api;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.team.gestorproyectos.proyecto.domain.port.in.ConsultarReleasesUseCase;
import com.team.gestorproyectos.proyecto.infrastructure.api.dto.ReleasesDtos.ReleasesResponse;
import com.team.gestorproyectos.proyecto.infrastructure.api.mapper.ReleasesApiMapper;
import com.team.gestorproyectos.shared.infrastructure.security.UsuarioSesion;

import lombok.RequiredArgsConstructor;

/**
 * Pestaña Releases del proyecto (PDF pág. 36). Integrar, publicar y revertir se hacen en sus propias vistas
 * (Integrar historia, Release de la fase, detalle del bug); aquí se reúnen.
 */
@RestController
@RequestMapping("/api/equipo/proyectos/{proyectoId}/releases")
@RequiredArgsConstructor
public class ReleasesProyectoController {

    private final ConsultarReleasesUseCase consultarReleases;
    private final ReleasesApiMapper mapper;

    @GetMapping
    public ReleasesResponse releases(@PathVariable("proyectoId") Long proyectoId,
                                     @AuthenticationPrincipal UsuarioSesion sesion) {
        return mapper.aReleases(consultarReleases.releases(proyectoId, sesion.id(), sesion.esAdmin()));
    }
}
