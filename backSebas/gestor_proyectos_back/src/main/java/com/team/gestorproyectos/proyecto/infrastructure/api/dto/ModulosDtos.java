package com.team.gestorproyectos.proyecto.infrastructure.api.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.team.gestorproyectos.proyecto.domain.model.TipoModulo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** JSON de la pestaña Módulos del proyecto (PDF pág. 35). */
public final class ModulosDtos {

    private ModulosDtos() {
    }

    public record ModulosResponse(List<ModuloResponse> modulos, List<String> faltan, boolean puedeGestionar) {
    }

    /** @param ultimoBuild null si nunca ha corrido el CI */
    public record ModuloResponse(Long id, String tipo, String nombre, String tecnologia, List<String> tecnologias,
                                 String repositorio, String ramaBase, ResponsableResponse responsable,
                                 List<AmbienteResponse> ambientes, BuildResponse ultimoBuild, int porHacer,
                                 int enProgreso, int enQa, int bugsProduccion, boolean conTrabajo) {
    }

    public record ResponsableResponse(Long id, String nombre) {
    }

    public record AmbienteResponse(String nombre, String version, LocalDateTime fecha) {
    }

    /** @param estado PASO, FALLO o PENDIENTE */
    public record BuildResponse(String estado, LocalDateTime fecha) {
    }

    public record ModuloRequest(
            @NotNull TipoModulo tipo,
            @NotBlank @Size(max = 60) String nombre,
            @Size(max = 80) String tecnologia,
            @Size(max = 200) String repositorio,
            @Size(max = 60) String ramaBase,
            Long responsableId) {
    }

    public record ModuloCreadoResponse(Long id) {
    }
}
