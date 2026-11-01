package com.team.gestorproyectos.proyecto.infrastructure.api;

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

import com.team.gestorproyectos.proyecto.domain.port.in.ConsultarProyectoUseCase;
import com.team.gestorproyectos.proyecto.domain.port.in.GestionarProyectoUseCase;
import com.team.gestorproyectos.proyecto.domain.port.in.ListarProyectosUseCase;
import com.team.gestorproyectos.proyecto.infrastructure.api.dto.ProyectosDtos.ConfiguracionResponse;
import com.team.gestorproyectos.proyecto.infrastructure.api.dto.ProyectosDtos.CreadoResponse;
import com.team.gestorproyectos.proyecto.infrastructure.api.dto.ProyectosDtos.FaseArbolResponse;
import com.team.gestorproyectos.proyecto.infrastructure.api.dto.ProyectosDtos.ListadoResponse;
import com.team.gestorproyectos.proyecto.infrastructure.api.dto.ProyectosDtos.ProyectoRequest;
import com.team.gestorproyectos.proyecto.infrastructure.api.dto.ProyectosDtos.ResumenResponse;
import com.team.gestorproyectos.proyecto.infrastructure.api.mapper.ProyectosApiMapper;
import com.team.gestorproyectos.shared.infrastructure.security.UsuarioSesion;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/** Proyectos (PDF págs. 13-16): lista, detalle (Resumen, Fases e historias), crear y editar. */
@RestController
@RequestMapping("/api/equipo/proyectos")
@RequiredArgsConstructor
public class ProyectoDetalleController {

    private final ListarProyectosUseCase listarProyectos;
    private final ConsultarProyectoUseCase consultarProyecto;
    private final GestionarProyectoUseCase gestionarProyecto;
    private final ProyectosApiMapper mapper;

    @GetMapping("/tarjetas")
    public ListadoResponse tarjetas(@AuthenticationPrincipal UsuarioSesion sesion) {
        return mapper.aListado(listarProyectos.listar(sesion.id(), sesion.esAdmin()));
    }

    @GetMapping("/{id}/resumen")
    public ResumenResponse resumen(@PathVariable("id") Long id, @AuthenticationPrincipal UsuarioSesion sesion) {
        return mapper.aResumen(consultarProyecto.resumen(id, sesion.id(), sesion.esAdmin()));
    }

    @GetMapping("/{id}/arbol")
    public List<FaseArbolResponse> arbol(@PathVariable("id") Long id, @AuthenticationPrincipal UsuarioSesion sesion) {
        return mapper.aArbol(consultarProyecto.arbol(id, sesion.id(), sesion.esAdmin()));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreadoResponse crear(@Valid @RequestBody ProyectoRequest request,
                                @AuthenticationPrincipal UsuarioSesion sesion) {
        return new CreadoResponse(gestionarProyecto.crear(sesion.id(), mapper.aDatos(request)));
    }

    @GetMapping("/{id}/configuracion")
    public ConfiguracionResponse configuracion(@PathVariable("id") Long id,
                                               @AuthenticationPrincipal UsuarioSesion sesion) {
        return mapper.aConfiguracion(gestionarProyecto.configuracion(id, sesion.id(), sesion.esAdmin()));
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void actualizar(@PathVariable("id") Long id, @Valid @RequestBody ProyectoRequest request,
                           @AuthenticationPrincipal UsuarioSesion sesion) {
        gestionarProyecto.actualizar(id, sesion.id(), sesion.esAdmin(), mapper.aDatos(request));
    }
}
