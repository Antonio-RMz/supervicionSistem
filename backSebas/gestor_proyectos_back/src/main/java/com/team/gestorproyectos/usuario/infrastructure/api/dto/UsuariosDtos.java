package com.team.gestorproyectos.usuario.infrastructure.api.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** JSON de Usuarios y roles. Los roles viajan como código (ADMIN, ANALISTA...); el front pone el nombre. */
public final class UsuariosDtos {

    private UsuariosDtos() {
    }

    // ─── Lista ───────────────────────────────────────────────────────────────

    public record UsuarioFilaResponse(
            Long id,
            String nombreCompleto,
            String iniciales,
            String correo,
            Set<String> roles,
            long proyectos,
            boolean activo) {
    }

    public record NuevoUsuarioRequest(
            @NotBlank @Size(max = 80) String nombre,
            @Size(max = 120) String apellidos,
            @NotBlank @Email @Size(max = 120) String correo,
            @NotBlank String password,
            boolean cambiarPassword,
            boolean activo,
            @NotEmpty Set<String> roles,
            Set<Long> proyectoIds,
            @Size(max = 100) String usuarioGit) {
    }

    /** @param usuarioGit vacío o null lo quita */
    public record UsuarioGitRequest(@Size(max = 100) String usuarioGit) {
    }

    public record EstadoRequest(@NotNull Boolean activo) {
    }

    // ─── Roles y permisos extra de una persona ───────────────────────────────

    public record AccesosResponse(Set<String> roles, List<PermisoExtraResponse> permisosExtra) {
    }

    /** @param proyectoId null = todos los proyectos */
    public record PermisoExtraResponse(
            String permisoCodigo,
            String permisoNombre,
            Long proyectoId,
            String proyectoNombre,
            boolean otorgado) {
    }

    public record AccesosRequest(@NotEmpty Set<String> roles, @NotNull List<@Valid PermisoExtraRequest> permisosExtra) {
    }

    public record PermisoExtraRequest(@NotBlank String permisoCodigo, Long proyectoId, boolean otorgado) {
    }

    // ─── Catálogo ────────────────────────────────────────────────────────────

    public record RolResponse(String codigo, String nombre, String descripcion, Set<String> permisos) {
    }

    public record PermisoResponse(String codigo, String nombre, String grupo) {
    }

    // ─── Detalle de una persona ──────────────────────────────────────────────

    public record PerfilResponse(
            Long id,
            String nombreCompleto,
            String iniciales,
            String correo,
            Set<String> roles,
            boolean activo,
            LocalDateTime ultimoAcceso,
            MetricasPerfilResponse metricas,
            List<TareaResponse> trabajandoAhora,
            List<TareaResponse> leToca,
            List<TareaResponse> esperandoAOtros,
            List<HechoResponse> actividad,
            List<ProyectoUsuarioResponse> proyectos,
            String usuarioGit) {
    }

    public record MetricasPerfilResponse(
            int trabajandoAhora,
            int leToca,
            int esperandoAOtros,
            long cerradosEsteMes,
            long rechazadosEnQaEsteMes) {
    }

    /** @param actorNombre y actorRol solo importan en "Esperando a otros" ("Ana · Analista") */
    public record TareaResponse(
            Long id,
            String tipo,
            String codigo,
            String titulo,
            String estado,
            String accion,
            String actorNombre,
            String actorRol,
            boolean rechazadaEnQa,
            LocalDateTime actualizadoEn) {
    }

    /** @param clase INICIO, ENVIO, RECHAZO, CIERRE, COMENTARIO u OTRO */
    public record HechoResponse(String descripcion, String clase, LocalDateTime fecha) {
    }

    public record ProyectoUsuarioResponse(Long id, String nombre, long ticketsAbiertos) {
    }
}
