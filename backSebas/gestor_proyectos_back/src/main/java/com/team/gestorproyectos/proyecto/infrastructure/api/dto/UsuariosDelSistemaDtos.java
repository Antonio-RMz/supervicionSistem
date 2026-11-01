package com.team.gestorproyectos.proyecto.infrastructure.api.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** JSON de la pestaña Usuarios del sistema. */
public final class UsuariosDelSistemaDtos {

    private UsuariosDelSistemaDtos() {
    }

    public record UsuariosDelSistemaResponse(List<UsuarioDelSistemaDto> usuarios, boolean puedeEditar) {
    }

    /** La lista completa, en orden: reemplaza a la anterior. */
    public record GuardarUsuariosRequest(@NotNull @Size(max = 30) List<@Valid UsuarioDelSistemaDto> usuarios) {
    }

    public record UsuarioDelSistemaDto(@Size(max = 80) String nombre, @Size(max = 300) String descripcion) {
    }
}
