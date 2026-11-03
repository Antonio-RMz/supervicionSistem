package com.team.gestorproyectos.fase.infrastructure.api.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.team.gestorproyectos.fase.domain.model.AccionFase;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** JSON del detalle de la fase y sus acciones (PDF pág. 20, 25 y 32). */
public final class GestionFaseDtos {

    private GestionFaseDtos() {
    }

    public record DetalleFaseResponse(
            InfoFaseResponse info, int avance, long ticketsTerminados, long ticketsTotal,
            List<GrupoResponse> ticketsPorEstado, List<CriterioResponse> criterios, String versionAnterior,
            BugResponse bugReversion, String rol, boolean esResponsable, List<AccionResponse> acciones,
            boolean puedeMarcarCriterios, boolean puedeCrearHistoria, boolean puedeEditar, boolean puedeEliminar,
            List<Long> historiasEliminables) {
    }

    public record InfoFaseResponse(
            Long id, Long proyectoId, String proyectoNombre, String proyectoCodigo, String formaTrabajo,
            String clienteNombre, int numero,
            String nombre, String objetivo, String estado, String prioridad, LocalDate fechaInicio, LocalDate fechaFin,
            String responsableNombre, String versionRelease, boolean mantenimiento, String figmaUrl,
            boolean figmaParaTodas, String motivoEstado,
            LocalDate reanudarEl, LocalDateTime enEstadoDesde, String cambioEstadoPor, List<ModuloResponse> modulos,
            List<HistoriaResponse> historias, List<ActividadResponse> actividad, ImpactoResponse aCancelar,
            List<PersonaResponse> equipo) {
    }

    public record ModuloResponse(String tipo, String nombre) {
    }

    public record HistoriaResponse(Long id, int numero, String titulo, String estado, long terminados, long total,
                                   String analistaIniciales, boolean incompleta, boolean disenoPropio,
                                   boolean sinTickets) {
    }

    public record ActividadResponse(String descripcion, String clase, LocalDateTime fecha) {
    }

    public record ImpactoResponse(long historias, long tickets) {
    }

    public record PersonaResponse(Long id, String nombreCompleto, List<String> roles) {
    }

    public record GrupoResponse(String nombre, long cantidad) {
    }

    public record CriterioResponse(Long id, String descripcion, boolean manual, boolean cumplido, String detalle,
                                   String cumplidoPor, LocalDateTime cumplidoEn) {
    }

    public record BugResponse(Long id, String codigo, boolean abierto) {
    }

    public record AccionResponse(String accion, String texto, List<String> impedimentos) {
    }

    public record AccionFaseRequest(
            @NotNull AccionFase accion,
            @NotBlank String estadoEsperado,
            @Size(max = 500) String motivo,
            LocalDate reanudarEl,
            String versionConfirmada,
            Long asignarBugA) {
    }

    public record AccionDeListaResponse(Long faseId, int numero, String nombre, String estado, String accion, String texto,
                                        String impedimento) {
    }

    /** El release de la fase (PDF pág. 27). */
    public record ReleaseFaseResponse(InfoReleaseResponse info, String notas, boolean puedeEditarNotas) {
    }

    public record InfoReleaseResponse(
            String version, String estado, LocalDateTime publicadoEn, String publicadoPor,
            VersionPublicadaResponse enProduccion, String liderNombre, List<ModuloReleaseResponse> modulos,
            boolean enviarNotas, List<CambioResponse> cambios, DemoResponse demo) {
    }

    public record VersionPublicadaResponse(String version, LocalDateTime fecha) {
    }

    public record ModuloReleaseResponse(String tipo, String nombre, String tecnologia) {
    }

    public record CambioResponse(String tipo, String codigo, String titulo) {
    }

    public record DemoResponse(String estado, String revisadaPor, LocalDateTime revisadaEn) {
    }

    public record NotasRequest(@NotBlank @Size(max = 5000) String notas, boolean enviarAlCliente) {
    }

    public record ResultadoResponse(String accion, String estadoNuevo, String bugCreado) {
    }
}
