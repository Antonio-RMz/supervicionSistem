package com.team.gestorproyectos.historia.infrastructure.api;

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

import com.team.gestorproyectos.historia.domain.port.in.CrearHistoriaUseCase;
import com.team.gestorproyectos.historia.domain.port.in.EditarHistoriaUseCase;
import com.team.gestorproyectos.historia.infrastructure.api.dto.HistoriasDtos.EdicionResponse;
import com.team.gestorproyectos.historia.infrastructure.api.dto.HistoriasDtos.HistoriaCreadaResponse;
import com.team.gestorproyectos.historia.infrastructure.api.dto.HistoriasDtos.ImportarHistoriasRequest;
import com.team.gestorproyectos.historia.infrastructure.api.dto.HistoriasDtos.NuevaHistoriaRequest;
import com.team.gestorproyectos.historia.infrastructure.api.dto.HistoriasDtos.PreparacionResponse;
import com.team.gestorproyectos.historia.infrastructure.api.dto.HistoriasDtos.ResultadoImportacionResponse;
import com.team.gestorproyectos.historia.infrastructure.api.mapper.HistoriasApiMapper;
import com.team.gestorproyectos.shared.infrastructure.security.UsuarioSesion;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/** Nueva historia de usuario (PDF pág. 18) y Editar historia. */
@RestController
@RequestMapping("/api/equipo/proyectos/{proyectoId}/historias")
@RequiredArgsConstructor
public class HistoriaController {

    private final CrearHistoriaUseCase crearHistoria;
    private final EditarHistoriaUseCase editarHistoria;
    private final HistoriasApiMapper mapper;

    /** Número, fases que reciben historias (con sus módulos) y el equipo. */
    @GetMapping("/nueva")
    public PreparacionResponse preparar(@PathVariable("proyectoId") Long proyectoId,
                                        @AuthenticationPrincipal UsuarioSesion sesion) {
        return mapper.aPreparacion(crearHistoria.preparar(proyectoId, sesion.id(), sesion.esAdmin()));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public HistoriaCreadaResponse crear(@PathVariable("proyectoId") Long proyectoId,
                                       @Valid @RequestBody NuevaHistoriaRequest request,
                                       @AuthenticationPrincipal UsuarioSesion sesion) {
        return new HistoriaCreadaResponse(crearHistoria.crear(proyectoId, sesion.id(), sesion.esAdmin(),
                mapper.aNuevaHistoria(request)));
    }

    /** Importar historias: varias de un JSON, en Borrador dentro de la fase. Usa la misma preparación que /nueva. */
    @PostMapping("/importar")
    @ResponseStatus(HttpStatus.CREATED)
    public ResultadoImportacionResponse importar(@PathVariable("proyectoId") Long proyectoId,
                                                 @Valid @RequestBody ImportarHistoriasRequest request,
                                                 @AuthenticationPrincipal UsuarioSesion sesion) {
        return mapper.aResultado(crearHistoria.importar(proyectoId, sesion.id(), sesion.esAdmin(), request.faseId(),
                request.analistaId(), mapper.aImportadas(request.historias())));
    }

    /** Opciones del formulario y los datos actuales de la historia. */
    @GetMapping("/{historiaId}/editar")
    public EdicionResponse prepararEdicion(@PathVariable("proyectoId") Long proyectoId,
                                           @PathVariable("historiaId") Long historiaId,
                                           @AuthenticationPrincipal UsuarioSesion sesion) {
        return mapper.aEdicion(editarHistoria.preparar(proyectoId, historiaId, sesion.id(), sesion.esAdmin()));
    }

    @PutMapping("/{historiaId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void editar(@PathVariable("proyectoId") Long proyectoId, @PathVariable("historiaId") Long historiaId,
                       @Valid @RequestBody NuevaHistoriaRequest request,
                       @AuthenticationPrincipal UsuarioSesion sesion) {
        editarHistoria.editar(proyectoId, historiaId, sesion.id(), sesion.esAdmin(), mapper.aNuevaHistoria(request));
    }
}
