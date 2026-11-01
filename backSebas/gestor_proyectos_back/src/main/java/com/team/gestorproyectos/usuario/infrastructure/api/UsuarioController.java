package com.team.gestorproyectos.usuario.infrastructure.api;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.team.gestorproyectos.shared.infrastructure.security.UsuarioSesion;
import com.team.gestorproyectos.usuario.domain.port.in.CambiarEstadoUsuarioUseCase;
import com.team.gestorproyectos.usuario.domain.port.in.ConsultarPerfilUsuarioUseCase;
import com.team.gestorproyectos.usuario.domain.port.in.CrearUsuarioUseCase;
import com.team.gestorproyectos.usuario.domain.port.in.GestionarAccesosUseCase;
import com.team.gestorproyectos.usuario.domain.port.in.ListarUsuariosUseCase;
import com.team.gestorproyectos.usuario.infrastructure.api.dto.UsuariosDtos.AccesosRequest;
import com.team.gestorproyectos.usuario.infrastructure.api.dto.UsuariosDtos.AccesosResponse;
import com.team.gestorproyectos.usuario.infrastructure.api.dto.UsuariosDtos.EstadoRequest;
import com.team.gestorproyectos.usuario.infrastructure.api.dto.UsuariosDtos.NuevoUsuarioRequest;
import com.team.gestorproyectos.usuario.infrastructure.api.dto.UsuariosDtos.PerfilResponse;
import com.team.gestorproyectos.usuario.infrastructure.api.dto.UsuariosDtos.UsuarioFilaResponse;
import com.team.gestorproyectos.usuario.infrastructure.api.mapper.UsuariosApiMapper;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/** Usuarios y roles (PDF págs. 6-8). Solo el Administrador entra aquí (ver SecurityConfig). */
@RestController
@RequestMapping("/api/equipo/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final ListarUsuariosUseCase listarUsuarios;
    private final CrearUsuarioUseCase crearUsuario;
    private final GestionarAccesosUseCase accesos;
    private final CambiarEstadoUsuarioUseCase cambiarEstado;
    private final ConsultarPerfilUsuarioUseCase consultarPerfil;
    private final UsuariosApiMapper mapper;

    @GetMapping
    public List<UsuarioFilaResponse> listar() {
        return mapper.aFilas(listarUsuarios.listar());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioFilaResponse crear(@Valid @RequestBody NuevoUsuarioRequest request) {
        return mapper.aFila(crearUsuario.crear(mapper.aNuevoUsuario(request)));
    }

    @GetMapping("/{id}")
    public PerfilResponse perfil(@PathVariable("id") Long id) {
        return mapper.aPerfil(consultarPerfil.consultar(id));
    }

    @GetMapping("/{id}/accesos")
    public AccesosResponse accesos(@PathVariable("id") Long id) {
        return mapper.aAccesosResponse(accesos.consultar(id));
    }

    @PutMapping("/{id}/accesos")
    public AccesosResponse actualizarAccesos(@PathVariable("id") Long id,
                                             @Valid @RequestBody AccesosRequest request,
                                             @AuthenticationPrincipal UsuarioSesion sesion) {
        return mapper.aAccesosResponse(accesos.actualizar(sesion.id(), id, mapper.aAccesos(request)));
    }

    @PatchMapping("/{id}/estado")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cambiarEstado(@PathVariable("id") Long id,
                              @Valid @RequestBody EstadoRequest request,
                              @AuthenticationPrincipal UsuarioSesion sesion) {
        cambiarEstado.cambiar(sesion.id(), id, request.activo());
    }
}
