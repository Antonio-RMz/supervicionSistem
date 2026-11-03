package com.team.gestorproyectos.historia.infrastructure.api.dto;

import java.util.List;
import java.util.Set;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** JSON de Nueva historia de usuario (PDF pág. 18). */
public final class HistoriasDtos {

    private HistoriasDtos() {
    }

    public record PreparacionResponse(Long proyectoId, String proyectoNombre, int numero, List<FaseResponse> fases,
                                      List<AnalistaResponse> analistas,
                                      List<UsuarioDelSistemaResponse> usuariosDelSistema,
                                      List<AnalistaResponse> desarrolladores) {
    }

    /** Un Desarrollador de la historia; moduloId null = en todos sus repositorios. */
    public record DesarrolladorRequest(@NotNull Long usuarioId, Long moduloId) {
    }

    public record UsuarioDelSistemaResponse(String nombre, String descripcion) {
    }

    public record FaseResponse(Long id, int numero, String nombre, String estado, List<OpcionResponse> modulos) {
    }

    public record OpcionResponse(Long id, String tipo, String nombre) {
    }

    public record AnalistaResponse(Long id, String nombreCompleto, List<String> roles) {
    }

    /** @param borrador true = "Guardar borrador" (pide menos); false = "Crear historia" */
    public record NuevaHistoriaRequest(
            boolean borrador,
            @NotNull Long faseId,
            @NotBlank @Size(max = 150) String titulo,
            @Size(max = 200) String como,
            @Size(max = 300) String quiero,
            @Size(max = 300) String para,
            List<@Size(max = 300) String> criterios,
            @Size(max = 300) String figmaUrl,
            boolean disenoAprobado,
            @NotNull Long analistaId,
            String prioridad,
            Integer puntos,
            Set<Long> moduloIds,
            List<@Valid DesarrolladorRequest> desarrolladores) {
    }

    public record HistoriaCreadaResponse(Long id) {
    }

    /** Importar historias: el JSON que devolvió la IA, ya leído, más la fase y el Analista elegidos. */
    public record ImportarHistoriasRequest(
            @NotNull Long faseId,
            @NotNull Long analistaId,
            @NotEmpty @Size(max = 50) List<@Valid HistoriaImportadaRequest> historias) {
    }

    public record HistoriaImportadaRequest(
            @NotBlank @Size(max = 150) String titulo,
            @Size(max = 200) String como,
            @Size(max = 300) String quiero,
            @Size(max = 300) String para,
            String prioridad,
            Integer puntos,
            List<@Size(max = 300) String> criterios) {
    }

    public record ResultadoImportacionResponse(int creadas, int incompletas) {
    }

    /** Editar historia: las opciones del formulario (solo su fase) y cómo está guardada hoy. */
    public record EdicionResponse(PreparacionResponse preparacion, HistoriaGuardadaResponse historia) {
    }

    public record HistoriaGuardadaResponse(
            Long id, int numero, String estado, Long faseId, String titulo, String como, String quiero, String para,
            List<String> criterios, String figmaUrl, boolean disenoAprobado, Long analistaId,
            String prioridad, Integer puntos, Set<Long> moduloIds, List<DesarrolladorRequest> desarrolladores) {
    }
}
