package com.team.gestorproyectos.cliente.infrastructure.api;

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

import com.team.gestorproyectos.cliente.domain.port.in.GestionarClientesUseCase;
import com.team.gestorproyectos.cliente.domain.port.in.ListarClientesUseCase;
import com.team.gestorproyectos.cliente.infrastructure.api.dto.ClientesDtos.ClienteRequest;
import com.team.gestorproyectos.cliente.infrastructure.api.dto.ClientesDtos.ClienteResponse;
import com.team.gestorproyectos.cliente.infrastructure.api.dto.ClientesDtos.EstadoRequest;
import com.team.gestorproyectos.cliente.infrastructure.api.dto.ClientesDtos.ListadoClientesResponse;
import com.team.gestorproyectos.cliente.infrastructure.api.mapper.ClientesApiMapper;
import com.team.gestorproyectos.shared.infrastructure.security.UsuarioSesion;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/** Clientes (PDF págs. 9-10). La pantalla es solo del Admin; la lista la lee también el formulario de proyecto. */
@RestController
@RequestMapping("/api/equipo/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final ListarClientesUseCase listarClientes;
    private final GestionarClientesUseCase gestionarClientes;
    private final ClientesApiMapper mapper;

    @GetMapping
    public ListadoClientesResponse listar(@AuthenticationPrincipal UsuarioSesion sesion) {
        return mapper.aListado(listarClientes.listar(sesion.id()));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ClienteResponse crear(@Valid @RequestBody ClienteRequest request,
                                 @AuthenticationPrincipal UsuarioSesion sesion) {
        return mapper.aResponse(gestionarClientes.crear(sesion.id(), mapper.aDatos(request)));
    }

    @PutMapping("/{id}")
    public ClienteResponse actualizar(@PathVariable("id") Long id,
                                      @Valid @RequestBody ClienteRequest request,
                                      @AuthenticationPrincipal UsuarioSesion sesion) {
        return mapper.aResponse(gestionarClientes.actualizar(sesion.id(), id, mapper.aDatos(request)));
    }

    @PatchMapping("/{id}/estado")
    public ClienteResponse cambiarEstado(@PathVariable("id") Long id,
                                         @Valid @RequestBody EstadoRequest request,
                                         @AuthenticationPrincipal UsuarioSesion sesion) {
        return mapper.aResponse(gestionarClientes.cambiarEstado(sesion.id(), id, request.activo()));
    }
}
