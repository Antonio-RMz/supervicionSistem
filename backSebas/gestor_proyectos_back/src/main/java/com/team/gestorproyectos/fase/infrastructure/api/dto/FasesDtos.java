package com.team.gestorproyectos.fase.infrastructure.api.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** JSON de Nueva fase (PDF pág. 17) y Editar fase. */
public final class FasesDtos {

    private FasesDtos() {
    }

    public record PreparacionResponse(
            Long proyectoId, String proyectoNombre, int numero, String versionSugerida, LocalDate inicioSugerido,
            Long liderId, boolean hayFaseActiva, String faseActiva, List<ModuloResponse> modulos,
            List<ResponsableResponse> equipo, List<String> criteriosAutomaticos) {
    }

    public record ModuloResponse(Long id, String tipo, String nombre, String tecnologia) {
    }

    public record ResponsableResponse(Long id, String nombreCompleto, String iniciales, List<String> roles) {
    }

    public record NuevaFaseRequest(
            @NotBlank @Size(max = 120) String nombre,
            String objetivo,
            @NotNull LocalDate fechaInicio,
            @NotNull LocalDate fechaFin,
            @NotNull Long responsableId,
            @Size(max = 20) String versionRelease,
            String prioridad,
            @NotEmpty Set<Long> moduloIds,
            List<@Size(max = 300) String> criteriosManuales) {
    }

    /** @param paraTodas las historias sin diseño propio heredan el enlace */
    public record DisenoFaseRequest(@Size(max = 300) String figmaUrl, boolean paraTodas) {
    }

    public record FaseCreadaResponse(Long id) {
    }

    /** Editar fase: las opciones del formulario y cómo está guardada hoy. */
    public record EdicionResponse(PreparacionResponse preparacion, FaseGuardadaResponse fase) {
    }

    public record FaseGuardadaResponse(
            Long id, int numero, String estado, String nombre, String objetivo, LocalDate fechaInicio,
            LocalDate fechaFin, Long responsableId, String versionRelease, String prioridad, Set<Long> moduloIds,
            List<String> criteriosManuales, Set<Long> modulosEnUso) {
    }
}
