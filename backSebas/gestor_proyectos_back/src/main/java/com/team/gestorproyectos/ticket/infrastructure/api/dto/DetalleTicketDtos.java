package com.team.gestorproyectos.ticket.infrastructure.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** JSON del detalle del ticket (PDF pág. 22, 26 y 29). */
public final class DetalleTicketDtos {

    private DetalleTicketDtos() {
    }

    public record DetalleTicketResponse(
            InfoTicketResponse info, String severidad, boolean hotfix, boolean requierePr,
            ParcheResponse parche, String actor, boolean meToca, List<OpcionResponse> opciones,
            boolean puedeMarcarSubtareas, boolean puedeEditarSubtareas, boolean puedeEditar,
            List<TicketAbiertoResponse> bloqueadores, List<PersonaEquipoResponse> equipo) {
    }

    public record InfoTicketResponse(
            Long id, Long proyectoId, String proyectoNombre, Long faseId, int faseNumero, String faseNombre,
            HistoriaResponse historia, String tipo, int numero, String titulo, String descripcion, String estado,
            String prioridad, List<String> capas, ModuloResponse modulo, PersonaResponse responsable,
            PersonaResponse tester, PersonaResponse creadoPor, BigDecimal estimacionHoras, BigDecimal horasRegistradas,
            LocalDate fechaLimite, String rama, String bloqueadoPor, long rechazosEnQa,
            List<DependenciaResponse> dependencias, List<SubtareaResponse> subtareas,
            List<PullRequestResponse> pullRequests, List<CriterioResponse> criterios,
            List<CambioResponse> historial, List<ComentarioResponse> comentarios) {
    }

    public record HistoriaResponse(Long id, int numero, String titulo, String estado, String como, String quiero,
                                   String para) {
    }

    public record ModuloResponse(String tipo, String nombre, String tecnologia, String repositorio, String ramaBase) {
    }

    public record PersonaResponse(Long id, String nombre) {
    }

    public record DependenciaResponse(String codigo, String titulo, String estado) {
    }

    public record SubtareaResponse(Long id, String descripcion, boolean hecha) {
    }

    public record PullRequestResponse(int numero, String ramaOrigen, String ramaDestino, String estado,
                                      String ciEstado, String url) {
    }

    public record CriterioResponse(String descripcion, boolean verificado, boolean soloEste) {
    }

    public record CambioResponse(String estadoAnterior, String estado, String motivo, boolean automatico,
                                 String usuario, LocalDateTime fecha) {
    }

    public record ComentarioResponse(String usuario, String texto, LocalDateTime fecha) {
    }

    public record OpcionResponse(String tipo, String texto, String destino, List<String> impedimentos) {
    }

    public record TicketAbiertoResponse(Long id, String codigo, String titulo, String estado) {
    }

    public record PersonaEquipoResponse(Long id, String nombreCompleto, List<String> roles) {
    }

    public record ParcheResponse(String version, List<IncluidoResponse> bugs) {
    }

    public record IncluidoResponse(Long id, String codigo, String titulo) {
    }

    /** @param version la que se vio en la confirmación ("v1.3.1") */
    public record ParcheRequest(@NotBlank String version) {
    }

    public record SubtareaRequest(boolean hecha) {
    }

    public record NuevaSubtareaRequest(@NotBlank @Size(max = 300) String descripcion) {
    }

    public record ComentarioRequest(@NotBlank @Size(max = 2000) String texto) {
    }
}
