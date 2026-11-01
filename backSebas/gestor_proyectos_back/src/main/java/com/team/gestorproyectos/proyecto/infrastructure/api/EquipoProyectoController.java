package com.team.gestorproyectos.proyecto.infrastructure.api;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.team.gestorproyectos.proyecto.domain.port.in.GestionarEquipoUseCase;
import com.team.gestorproyectos.proyecto.infrastructure.api.dto.EquipoDtos.AgregarMiembroRequest;
import com.team.gestorproyectos.proyecto.infrastructure.api.dto.EquipoDtos.CandidatoResponse;
import com.team.gestorproyectos.proyecto.infrastructure.api.dto.EquipoDtos.EquipoResponse;
import com.team.gestorproyectos.proyecto.infrastructure.api.dto.EquipoDtos.ReasignadosResponse;
import com.team.gestorproyectos.proyecto.infrastructure.api.dto.EquipoDtos.ReasignarRequest;
import com.team.gestorproyectos.proyecto.infrastructure.api.mapper.EquipoApiMapper;
import com.team.gestorproyectos.shared.infrastructure.security.UsuarioSesion;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/** Pestaña Equipo del proyecto (PDF pág. 34): miembros, carga, cobertura de roles y reasignación. */
@RestController
@RequestMapping("/api/equipo/proyectos/{proyectoId}/equipo")
@RequiredArgsConstructor
public class EquipoProyectoController {

    private final GestionarEquipoUseCase gestionarEquipo;
    private final EquipoApiMapper mapper;

    @GetMapping
    public EquipoResponse equipo(@PathVariable("proyectoId") Long proyectoId,
                                 @AuthenticationPrincipal UsuarioSesion sesion) {
        return mapper.aEquipo(gestionarEquipo.equipo(proyectoId, sesion.id(), sesion.esAdmin()));
    }

    /** Quién se puede agregar: personas del equipo con cuenta activa que aún no están. */
    @GetMapping("/candidatos")
    public List<CandidatoResponse> candidatos(@PathVariable("proyectoId") Long proyectoId,
                                              @AuthenticationPrincipal UsuarioSesion sesion) {
        return mapper.aCandidatos(gestionarEquipo.candidatos(proyectoId, sesion.id(), sesion.esAdmin()));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void agregar(@PathVariable("proyectoId") Long proyectoId, @Valid @RequestBody AgregarMiembroRequest request,
                        @AuthenticationPrincipal UsuarioSesion sesion) {
        gestionarEquipo.agregar(proyectoId, request.usuarioId(), sesion.id(), sesion.esAdmin());
    }

    @DeleteMapping("/{miembroId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void quitar(@PathVariable("proyectoId") Long proyectoId, @PathVariable("miembroId") Long miembroId,
                       @AuthenticationPrincipal UsuarioSesion sesion) {
        gestionarEquipo.quitar(proyectoId, miembroId, sesion.id(), sesion.esAdmin());
    }

    /** Cuando alguien se ausenta: sus tickets abiertos pasan a otra persona del equipo. */
    @PostMapping("/{miembroId}/reasignar")
    public ReasignadosResponse reasignar(@PathVariable("proyectoId") Long proyectoId,
                                         @PathVariable("miembroId") Long miembroId,
                                         @RequestBody ReasignarRequest request,
                                         @AuthenticationPrincipal UsuarioSesion sesion) {
        return new ReasignadosResponse(gestionarEquipo.reasignar(proyectoId, miembroId, request.responsableNuevoId(),
                request.testerNuevoId(), sesion.id(), sesion.esAdmin()));
    }
}
