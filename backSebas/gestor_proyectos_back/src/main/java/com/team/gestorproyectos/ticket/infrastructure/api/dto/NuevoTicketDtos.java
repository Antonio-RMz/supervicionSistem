package com.team.gestorproyectos.ticket.infrastructure.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.team.gestorproyectos.ticket.domain.model.TipoTicket;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** JSON de Nuevo ticket de trabajo (PDF pág. 19). */
public final class NuevoTicketDtos {

    private NuevoTicketDtos() {
    }

    /** @param siguientes número que tendría un ticket de cada tipo: {"ACTV": 41, ...} */
    public record PreparacionResponse(
            Long proyectoId, String proyectoNombre, Long liderId, List<FaseResponse> fases,
            List<PersonaResponse> equipo, Map<String, Integer> siguientes, List<AbiertoResponse> abiertos,
            List<String> capas) {
    }

    public record FaseResponse(Long id, int numero, String nombre, List<ModuloResponse> modulos,
                               List<HistoriaResponse> historias) {
    }

    public record ModuloResponse(Long id, String tipo, String nombre, String tecnologia, String repositorio,
                                 String ramaBase) {
    }

    public record HistoriaResponse(Long id, int numero, String titulo, String estado, String como, String quiero,
                                   String para, String analistaNombre, Set<Long> moduloIds, long tickets,
                                   List<CriterioResponse> criterios) {
    }

    public record CriterioResponse(Long id, String descripcion, String cubiertoPor) {
    }

    public record PersonaResponse(Long id, String nombreCompleto, List<String> roles) {
    }

    public record AbiertoResponse(Long id, String codigo, String titulo, String estado) {
    }

    public record NuevoTicketRequest(
            @NotNull Long faseId,
            @NotNull Long historiaId,
            @NotNull TipoTicket tipo,
            @NotBlank @Size(max = 200) String titulo,
            String descripcion,
            @NotNull Long moduloId,
            Set<String> capas,
            Set<Long> criterioIds,
            List<@Size(max = 300) String> subtareas,
            Set<Long> dependeDe,
            @NotNull Long responsableId,
            Long testerId,
            String prioridad,
            BigDecimal estimacionHoras,
            LocalDate fechaLimite) {
    }

    public record TicketCreadoResponse(Long id) {
    }

    /** Importar tickets: el JSON que devolvió la IA, ya leído, más la historia y el responsable elegidos. */
    public record ImportarTicketsRequest(
            @NotNull Long historiaId,
            @NotNull Long responsableId,
            @NotEmpty @Size(max = 50) List<@Valid TicketImportadoRequest> tickets) {
    }

    public record TicketImportadoRequest(
            @NotBlank @Size(max = 200) String titulo,
            String descripcion,
            @Size(max = 100) String repositorio,
            @Size(max = 30) List<@Size(max = 300) String> subtareas) {
    }

    public record TicketsImportadosResponse(int creados) {
    }

    /** Editar ticket: el mismo formulario de Nuevo ticket con lo que ya tiene. */
    public record EdicionResponse(PreparacionResponse preparacion, TicketEditableResponse ticket) {
    }

    /** @param datos lo capturado, con la misma forma que al crearlo (sin subtareas) */
    public record TicketEditableResponse(Long id, String codigo, NuevoTicketRequest datos) {
    }
}
