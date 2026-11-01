package com.team.gestorproyectos.proyecto.infrastructure.api;

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

import com.team.gestorproyectos.proyecto.domain.port.in.GestionarModulosUseCase;
import com.team.gestorproyectos.proyecto.infrastructure.api.dto.ModulosDtos.ModuloCreadoResponse;
import com.team.gestorproyectos.proyecto.infrastructure.api.dto.ModulosDtos.ModuloRequest;
import com.team.gestorproyectos.proyecto.infrastructure.api.dto.ModulosDtos.ModulosResponse;
import com.team.gestorproyectos.proyecto.infrastructure.api.mapper.ModulosApiMapper;
import com.team.gestorproyectos.shared.infrastructure.security.UsuarioSesion;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/** Pestaña Módulos del proyecto (PDF pág. 35): cada parte del sistema con sus ambientes, CI y tickets. */
@RestController
@RequestMapping("/api/equipo/proyectos/{proyectoId}/modulos")
@RequiredArgsConstructor
public class ModulosProyectoController {

    private final GestionarModulosUseCase gestionarModulos;
    private final ModulosApiMapper mapper;

    @GetMapping
    public ModulosResponse modulos(@PathVariable("proyectoId") Long proyectoId,
                                   @AuthenticationPrincipal UsuarioSesion sesion) {
        return mapper.aModulos(gestionarModulos.modulos(proyectoId, sesion.id(), sesion.esAdmin()));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ModuloCreadoResponse crear(@PathVariable("proyectoId") Long proyectoId,
                                      @Valid @RequestBody ModuloRequest request,
                                      @AuthenticationPrincipal UsuarioSesion sesion) {
        return new ModuloCreadoResponse(gestionarModulos.guardar(proyectoId, mapper.aModulo(null, request),
                request.responsableId(), sesion.id(), sesion.esAdmin()));
    }

    @PutMapping("/{moduloId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void actualizar(@PathVariable("proyectoId") Long proyectoId, @PathVariable("moduloId") Long moduloId,
                           @Valid @RequestBody ModuloRequest request, @AuthenticationPrincipal UsuarioSesion sesion) {
        gestionarModulos.guardar(proyectoId, mapper.aModulo(moduloId, request), request.responsableId(), sesion.id(),
                sesion.esAdmin());
    }

    /** Solo si aún no tiene tickets, fases ni PR. */
    @DeleteMapping("/{moduloId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void quitar(@PathVariable("proyectoId") Long proyectoId, @PathVariable("moduloId") Long moduloId,
                       @AuthenticationPrincipal UsuarioSesion sesion) {
        gestionarModulos.quitar(proyectoId, moduloId, sesion.id(), sesion.esAdmin());
    }
}
