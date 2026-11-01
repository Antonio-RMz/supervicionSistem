package com.team.gestorproyectos.historia.infrastructure.api.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.team.gestorproyectos.historia.domain.model.AccionHistoria;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** JSON del detalle de la historia y sus acciones (PDF pág. 21, 24, 28 y 33). */
public final class GestionHistoriaDtos {

    private GestionHistoriaDtos() {
    }

    public record DetalleHistoriaResponse(
            InfoHistoriaResponse info, String actor, boolean meToca, List<AccionResponse> acciones,
            boolean puedeVerificarCriterios, boolean puedeEditar, boolean puedeAdjuntar, boolean puedeCrearTicket,
            int ticketsSinEmpezar,
            List<TicketReabribleResponse> ticketsReabribles, List<FaseDestinoResponse> fasesDestino,
            boolean puedeEliminar) {
    }

    public record InfoHistoriaResponse(
            Long id, Long proyectoId, String proyectoNombre, Long faseId, int faseNumero, String faseNombre,
            String faseEstado, LocalDate faseInicio, LocalDate faseFin, int numero, String titulo, String como,
            String quiero, String para, String estado, String prioridad, Integer puntos, String analistaNombre,
            String figmaUrl, boolean disenoDeFase, boolean disenoAprobado, String disenoAprobadoPor,
            LocalDateTime disenoAprobadoEn, List<String> modulos, List<CriterioResponse> criterios,
            List<TicketResponse> tickets, List<CambioResponse> historial, List<AdjuntoResponse> adjuntos,
            List<ComentarioResponse> comentarios) {
    }

    public record CriterioResponse(Long id, String descripcion, boolean verificado, List<String> tickets) {
    }

    public record TicketResponse(Long id, String tipo, int numero, String titulo, String estado, String moduloTipo,
                                 String responsableNombre, List<String> dependeDe) {
    }

    public record CambioResponse(String estado, String motivo, boolean automatico, String usuario,
                                 LocalDateTime fecha) {
    }

    public record AdjuntoResponse(Long id, String nombre, String tipoMime, Long tamanoBytes) {
    }

    public record ComentarioResponse(String usuario, String texto, LocalDateTime fecha) {
    }

    public record AccionResponse(String accion, String texto, List<String> impedimentos) {
    }

    public record TicketReabribleResponse(Long id, String codigo, String titulo) {
    }

    public record FaseDestinoResponse(Long id, String titulo) {
    }

    public record AccionDeListaResponse(Long historiaId, String estado, String accion, String texto,
                                        String impedimento) {
    }

    public record AccionHistoriaRequest(
            @NotNull AccionHistoria accion,
            @NotBlank String estadoEsperado,
            @Size(max = 1000) String motivo,
            Long ticketId,
            boolean crearBug,
            Long faseDestinoId) {
    }

    public record CriterioRequest(boolean verificado) {
    }

    public record ComentarioRequest(@NotBlank @Size(max = 2000) String texto) {
    }

    // ─── Integrar historia (PDF pág. 28) ────────────────────────────────────

    public record IntegracionResponse(
            Long historiaId, Long proyectoId, String proyectoNombre, Long faseId, int faseNumero, String version,
            String codigo, String titulo, String estado, String analistaNombre, String rol, List<String> impedimentos,
            List<String> prefijosRama, int prsPorIntegrar, int ticketsConCodigo, List<ModuloIntegrarResponse> modulos,
            List<RevisionResponse> revision, List<AmbienteResponse> ambientes,
            List<HistoriaDeFaseResponse> historiasDeFase, List<String> pasos) {
    }

    public record ModuloIntegrarResponse(String tipo, String nombre, String tecnologia,
                                         List<TicketIntegrarResponse> tickets) {
    }

    /** @param prEstado ABIERTO, APROBADO, INTEGRADO, CERRADO; null si no tiene PR */
    public record TicketIntegrarResponse(Long id, String tipo, String codigo, String titulo, String rama,
                                         Integer prNumero, String prEstado, String prCi, boolean conConflictos,
                                         boolean enDevelop) {
    }

    /** @param resultado OK, AVISO o FALTA */
    public record RevisionResponse(String texto, String resultado, String detalle, LocalDateTime fecha) {
    }

    public record AmbienteResponse(String nombre, String version, String detalle, LocalDateTime fecha) {
    }

    public record HistoriaDeFaseResponse(Long id, int numero, String estado) {
    }

    public record ResultadoResponse(String accion, String estadoNuevo, String bugCreado) {
    }
}
