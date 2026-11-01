package com.team.gestorproyectos.cliente.infrastructure.api.dto;

import java.util.List;

import com.team.gestorproyectos.cliente.domain.model.Giro;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** JSON de Clientes. El giro viaja como código (COMERCIO, SERVICIOS...); el front pone el nombre. */
public final class ClientesDtos {

    private ClientesDtos() {
    }

    /** @param puedeGestionar si quien consulta puede crear y editar (si no, la pantalla es de solo lectura) */
    public record ListadoClientesResponse(List<ClienteResponse> clientes, boolean puedeGestionar) {
    }

    public record ClienteResponse(
            Long id,
            String razonSocial,
            String nombreCorto,
            String iniciales,
            String rfc,
            String giro,
            String ciudad,
            String contactoNombre,
            String contactoCorreo,
            String contactoTelefono,
            String notas,
            boolean activo,
            List<ProyectoResponse> proyectos) {
    }

    public record ProyectoResponse(Long id, String nombre) {
    }

    public record ClienteRequest(
            @NotBlank @Size(max = 150) String razonSocial,
            @Size(max = 80) String nombreCorto,
            @Size(max = 13) String rfc,
            Giro giro,
            @Size(max = 80) String ciudad,
            @Size(max = 120) String contactoNombre,
            @Email @Size(max = 120) String contactoCorreo,
            @Size(max = 20) String contactoTelefono,
            String notas,
            boolean activo) {
    }

    public record EstadoRequest(@NotNull Boolean activo) {
    }
}
