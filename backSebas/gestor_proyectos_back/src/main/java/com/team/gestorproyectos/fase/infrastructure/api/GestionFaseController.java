package com.team.gestorproyectos.fase.infrastructure.api;

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

import com.team.gestorproyectos.fase.domain.port.in.GestionarFaseUseCase;
import com.team.gestorproyectos.fase.infrastructure.api.dto.GestionFaseDtos.AccionFaseRequest;
import com.team.gestorproyectos.fase.infrastructure.api.dto.GestionFaseDtos.DetalleFaseResponse;
import com.team.gestorproyectos.fase.infrastructure.api.dto.GestionFaseDtos.NotasRequest;
import com.team.gestorproyectos.fase.infrastructure.api.dto.GestionFaseDtos.ReleaseFaseResponse;
import com.team.gestorproyectos.fase.infrastructure.api.dto.GestionFaseDtos.ResultadoResponse;
import com.team.gestorproyectos.fase.infrastructure.api.mapper.GestionFaseApiMapper;
import com.team.gestorproyectos.shared.infrastructure.security.UsuarioSesion;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/** Detalle de la fase (PDF pág. 32), sus acciones por estado (pág. 20) y confirmaciones (pág. 25). */
@RestController
@RequestMapping("/api/equipo/proyectos/{proyectoId}/fases/{faseId}")
@RequiredArgsConstructor
public class GestionFaseController {

    private final GestionarFaseUseCase gestionarFase;
    private final GestionFaseApiMapper mapper;

    @GetMapping
    public DetalleFaseResponse detalle(@PathVariable("proyectoId") Long proyectoId,
                                       @PathVariable("faseId") Long faseId,
                                       @AuthenticationPrincipal UsuarioSesion sesion) {
        return mapper.aDetalle(gestionarFase.detalle(proyectoId, faseId, sesion.id(), sesion.esAdmin()));
    }

    @PostMapping("/acciones")
    public ResultadoResponse ejecutar(@PathVariable("proyectoId") Long proyectoId,
                                      @PathVariable("faseId") Long faseId,
                                      @Valid @RequestBody AccionFaseRequest request,
                                      @AuthenticationPrincipal UsuarioSesion sesion) {
        return mapper.aResultado(gestionarFase.ejecutar(proyectoId, faseId, sesion.id(), sesion.esAdmin(),
                request.accion(), mapper.aDatos(request)));
    }

    /** El release de la fase (PDF pág. 27): qué sube a main, qué incluye, notas y plan de reversa. */
    @GetMapping("/release")
    public ReleaseFaseResponse release(@PathVariable("proyectoId") Long proyectoId,
                                       @PathVariable("faseId") Long faseId,
                                       @AuthenticationPrincipal UsuarioSesion sesion) {
        return mapper.aRelease(gestionarFase.release(proyectoId, faseId, sesion.id(), sesion.esAdmin()));
    }

    @PutMapping("/release/notas")
    public void guardarNotas(@PathVariable("proyectoId") Long proyectoId,
                             @PathVariable("faseId") Long faseId,
                             @Valid @RequestBody NotasRequest request,
                             @AuthenticationPrincipal UsuarioSesion sesion) {
        gestionarFase.guardarNotas(proyectoId, faseId, sesion.id(), sesion.esAdmin(), request.notas(),
                request.enviarAlCliente());
    }

    @PostMapping("/criterios/{criterioId}/cumplido")
    public ResultadoResponse marcarCriterio(@PathVariable("proyectoId") Long proyectoId,
                                            @PathVariable("faseId") Long faseId,
                                            @PathVariable("criterioId") Long criterioId,
                                            @AuthenticationPrincipal UsuarioSesion sesion) {
        return mapper.aResultado(gestionarFase.marcarCriterio(proyectoId, faseId, criterioId, sesion.id(),
                sesion.esAdmin()));
    }

    /** La borra con sus historias, solo si está Planeada y nada se ha iniciado; si no, se cancela. */
    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable("proyectoId") Long proyectoId, @PathVariable("faseId") Long faseId,
                         @AuthenticationPrincipal UsuarioSesion sesion) {
        gestionarFase.eliminar(proyectoId, faseId, sesion.id(), sesion.esAdmin());
    }
}
