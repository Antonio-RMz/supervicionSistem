package com.team.gestorproyectos.historia.infrastructure.api;

import java.util.List;

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

import com.team.gestorproyectos.historia.domain.port.in.GestionarHistoriaUseCase;
import com.team.gestorproyectos.historia.infrastructure.api.dto.GestionHistoriaDtos.AccionDeListaResponse;
import com.team.gestorproyectos.historia.infrastructure.api.dto.GestionHistoriaDtos.AccionHistoriaRequest;
import com.team.gestorproyectos.historia.infrastructure.api.dto.GestionHistoriaDtos.ComentarioRequest;
import com.team.gestorproyectos.historia.infrastructure.api.dto.GestionHistoriaDtos.CriterioRequest;
import com.team.gestorproyectos.historia.infrastructure.api.dto.GestionHistoriaDtos.TrabajoHechoRequest;
import com.team.gestorproyectos.historia.infrastructure.api.dto.GestionHistoriaDtos.NuevoTrabajoRequest;
import com.team.gestorproyectos.historia.infrastructure.api.dto.GestionHistoriaDtos.MiParteRequest;
import com.team.gestorproyectos.historia.infrastructure.api.dto.GestionHistoriaDtos.DetalleHistoriaResponse;
import com.team.gestorproyectos.historia.infrastructure.api.dto.GestionHistoriaDtos.IntegracionResponse;
import com.team.gestorproyectos.historia.infrastructure.api.dto.GestionHistoriaDtos.ResultadoResponse;
import com.team.gestorproyectos.historia.infrastructure.api.mapper.GestionHistoriaApiMapper;
import com.team.gestorproyectos.shared.infrastructure.security.UsuarioSesion;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/** Detalle de la historia (PDF pág. 24), sus acciones por estado (pág. 21), Integrar (pág. 28) y confirmaciones (pág. 33). */
@RestController
@RequestMapping("/api/equipo/proyectos/{proyectoId}/historias")
@RequiredArgsConstructor
public class GestionHistoriaController {

    private final GestionarHistoriaUseCase gestionarHistoria;
    private final GestionHistoriaApiMapper mapper;

    /** El botón de cada historia en las listas: árbol, Dashboard y Mis tickets (PDF pág. 21). */
    @GetMapping("/acciones")
    public List<AccionDeListaResponse> accionesDeLista(@PathVariable("proyectoId") Long proyectoId,
                                                       @AuthenticationPrincipal UsuarioSesion sesion) {
        return mapper.aAccionesDeLista(gestionarHistoria.accionesDeLista(proyectoId, sesion.id(), sesion.esAdmin()));
    }

    @GetMapping("/{historiaId}")
    public DetalleHistoriaResponse detalle(@PathVariable("proyectoId") Long proyectoId,
                                           @PathVariable("historiaId") Long historiaId,
                                           @AuthenticationPrincipal UsuarioSesion sesion) {
        return mapper.aDetalle(gestionarHistoria.detalle(proyectoId, historiaId, sesion.id(), sesion.esAdmin()));
    }

    /** La vista Integrar historia (PDF pág. 28): qué entra a develop y la revisión previa. */
    @GetMapping("/{historiaId}/integracion")
    public IntegracionResponse integracion(@PathVariable("proyectoId") Long proyectoId,
                                           @PathVariable("historiaId") Long historiaId,
                                           @AuthenticationPrincipal UsuarioSesion sesion) {
        return mapper.aIntegracion(gestionarHistoria.integracion(proyectoId, historiaId, sesion.id(), sesion.esAdmin()));
    }

    @PostMapping("/{historiaId}/acciones")
    public ResultadoResponse ejecutar(@PathVariable("proyectoId") Long proyectoId,
                                      @PathVariable("historiaId") Long historiaId,
                                      @Valid @RequestBody AccionHistoriaRequest request,
                                      @AuthenticationPrincipal UsuarioSesion sesion) {
        return mapper.aResultado(gestionarHistoria.ejecutar(proyectoId, historiaId, sesion.id(), sesion.esAdmin(),
                request.accion(), mapper.aDatos(request)));
    }

    /** El Analista marca (o desmarca) un criterio cuando lo comprueba en staging. */
    @PutMapping("/{historiaId}/criterios/{criterioId}")
    public void verificarCriterio(@PathVariable("proyectoId") Long proyectoId,
                                  @PathVariable("historiaId") Long historiaId,
                                  @PathVariable("criterioId") Long criterioId,
                                  @RequestBody CriterioRequest request,
                                  @AuthenticationPrincipal UsuarioSesion sesion) {
        gestionarHistoria.verificarCriterio(proyectoId, historiaId, criterioId, request.verificado(), sesion.id(),
                sesion.esAdmin());
    }

    @PostMapping("/{historiaId}/comentarios")
    @ResponseStatus(HttpStatus.CREATED)
    public void comentar(@PathVariable("proyectoId") Long proyectoId,
                         @PathVariable("historiaId") Long historiaId,
                         @Valid @RequestBody ComentarioRequest request,
                         @AuthenticationPrincipal UsuarioSesion sesion) {
        gestionarHistoria.comentar(proyectoId, historiaId, request.texto(), sesion.id(), sesion.esAdmin());
    }

    // ─── Historia con Desarrolladores ───

    @PutMapping("/{historiaId}/desarrollo/mi-parte")
    public void marcarMiParte(@PathVariable("proyectoId") Long proyectoId,
                              @PathVariable("historiaId") Long historiaId,
                              @RequestBody MiParteRequest request,
                              @AuthenticationPrincipal UsuarioSesion sesion) {
        gestionarHistoria.marcarMiParte(proyectoId, historiaId, request.terminado(), sesion.id(), sesion.esAdmin());
    }

    @PostMapping("/{historiaId}/trabajo")
    @ResponseStatus(HttpStatus.CREATED)
    public void agregarTrabajo(@PathVariable("proyectoId") Long proyectoId,
                               @PathVariable("historiaId") Long historiaId,
                               @Valid @RequestBody NuevoTrabajoRequest request,
                               @AuthenticationPrincipal UsuarioSesion sesion) {
        gestionarHistoria.agregarTrabajo(proyectoId, historiaId, request.descripcion(), request.moduloId(),
                sesion.id(), sesion.esAdmin());
    }

    @PutMapping("/{historiaId}/trabajo/{trabajoId}")
    public void marcarTrabajo(@PathVariable("proyectoId") Long proyectoId,
                              @PathVariable("historiaId") Long historiaId,
                              @PathVariable("trabajoId") Long trabajoId,
                              @RequestBody TrabajoHechoRequest request,
                              @AuthenticationPrincipal UsuarioSesion sesion) {
        gestionarHistoria.marcarTrabajo(proyectoId, historiaId, trabajoId, request.hecho(), sesion.id(),
                sesion.esAdmin());
    }

    @DeleteMapping("/{historiaId}/trabajo/{trabajoId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void quitarTrabajo(@PathVariable("proyectoId") Long proyectoId,
                              @PathVariable("historiaId") Long historiaId,
                              @PathVariable("trabajoId") Long trabajoId,
                              @AuthenticationPrincipal UsuarioSesion sesion) {
        gestionarHistoria.quitarTrabajo(proyectoId, historiaId, trabajoId, sesion.id(), sesion.esAdmin());
    }

    /** Solo si aún no se inicia; si ya se inició, se descarta. */
    @DeleteMapping("/{historiaId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable("proyectoId") Long proyectoId, @PathVariable("historiaId") Long historiaId,
                         @AuthenticationPrincipal UsuarioSesion sesion) {
        gestionarHistoria.eliminar(proyectoId, historiaId, sesion.id(), sesion.esAdmin());
    }
}
