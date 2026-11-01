package com.team.gestorproyectos.fase.infrastructure.api;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.team.gestorproyectos.fase.domain.model.DisenoFase;
import com.team.gestorproyectos.fase.domain.port.in.CrearFaseUseCase;
import com.team.gestorproyectos.fase.domain.port.in.EditarFaseUseCase;
import com.team.gestorproyectos.fase.domain.port.in.GestionarFaseUseCase;
import com.team.gestorproyectos.fase.infrastructure.api.dto.FasesDtos.DisenoFaseRequest;
import com.team.gestorproyectos.fase.infrastructure.api.dto.FasesDtos.EdicionResponse;
import com.team.gestorproyectos.fase.infrastructure.api.dto.FasesDtos.FaseCreadaResponse;
import com.team.gestorproyectos.fase.infrastructure.api.dto.FasesDtos.NuevaFaseRequest;
import com.team.gestorproyectos.fase.infrastructure.api.dto.FasesDtos.PreparacionResponse;
import com.team.gestorproyectos.fase.infrastructure.api.dto.GestionFaseDtos.AccionDeListaResponse;
import com.team.gestorproyectos.fase.infrastructure.api.mapper.FasesApiMapper;
import com.team.gestorproyectos.fase.infrastructure.api.mapper.GestionFaseApiMapper;
import com.team.gestorproyectos.shared.infrastructure.security.UsuarioSesion;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/** Nueva fase (PDF pág. 17), Editar fase y los botones de las fases en el árbol (pág. 20). */
@RestController
@RequestMapping("/api/equipo/proyectos/{proyectoId}/fases")
@RequiredArgsConstructor
public class FaseController {

    private final CrearFaseUseCase crearFase;
    private final EditarFaseUseCase editarFase;
    private final GestionarFaseUseCase gestionarFase;
    private final FasesApiMapper mapper;
    private final GestionFaseApiMapper gestionMapper;

    /** El botón de cada fase en el Dashboard y Mis tickets (PDF pág. 20). */
    @GetMapping("/acciones")
    public List<AccionDeListaResponse> accionesDeLista(@PathVariable("proyectoId") Long proyectoId,
                                                       @AuthenticationPrincipal UsuarioSesion sesion) {
        return gestionMapper.aAccionesDeLista(gestionarFase.accionesDeLista(proyectoId, sesion.id(), sesion.esAdmin()));
    }

    /** Número, versión y fechas sugeridas, módulos y equipo para el formulario. */
    @GetMapping("/nueva")
    public PreparacionResponse preparar(@PathVariable("proyectoId") Long proyectoId,
                                        @AuthenticationPrincipal UsuarioSesion sesion) {
        return mapper.aPreparacion(crearFase.preparar(proyectoId, sesion.id(), sesion.esAdmin()));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FaseCreadaResponse crear(@PathVariable("proyectoId") Long proyectoId,
                                    @Valid @RequestBody NuevaFaseRequest request,
                                    @AuthenticationPrincipal UsuarioSesion sesion) {
        return new FaseCreadaResponse(crearFase.crear(proyectoId, sesion.id(), sesion.esAdmin(),
                mapper.aNuevaFase(request)));
    }

    /** Opciones del formulario y los datos actuales de la fase. */
    @GetMapping("/{faseId}/editar")
    public EdicionResponse prepararEdicion(@PathVariable("proyectoId") Long proyectoId,
                                           @PathVariable("faseId") Long faseId,
                                           @AuthenticationPrincipal UsuarioSesion sesion) {
        return mapper.aEdicion(editarFase.preparar(proyectoId, faseId, sesion.id(), sesion.esAdmin()));
    }

    @PutMapping("/{faseId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void editar(@PathVariable("proyectoId") Long proyectoId, @PathVariable("faseId") Long faseId,
                       @Valid @RequestBody NuevaFaseRequest request,
                       @AuthenticationPrincipal UsuarioSesion sesion) {
        editarFase.editar(proyectoId, faseId, sesion.id(), sesion.esAdmin(), mapper.aNuevaFase(request));
    }

    /** El enlace de Figma de toda la fase y si lo heredan las historias sin diseño propio. */
    @PutMapping("/{faseId}/diseno")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void guardarDiseno(@PathVariable("proyectoId") Long proyectoId, @PathVariable("faseId") Long faseId,
                              @Valid @RequestBody DisenoFaseRequest request,
                              @AuthenticationPrincipal UsuarioSesion sesion) {
        editarFase.guardarDiseno(proyectoId, faseId, sesion.id(), sesion.esAdmin(),
                new DisenoFase(request.figmaUrl(), request.paraTodas()));
    }
}
