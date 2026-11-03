package com.team.gestorproyectos.proyecto.infrastructure.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import com.team.gestorproyectos.proyecto.domain.model.EstadoProyecto;
import com.team.gestorproyectos.proyecto.domain.model.TipoModulo;
import com.team.gestorproyectos.shared.domain.model.FormaTrabajo;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** JSON de la lista, el detalle y el formulario de proyectos. Estados y módulos viajan como código. */
public final class ProyectosDtos {

    private ProyectosDtos() {
    }

    // ─── Lista (PDF pág. 13) ─────────────────────────────────────────────────

    public record ListadoResponse(List<TarjetaResponse> proyectos, boolean puedeCrear) {
    }

    public record TarjetaResponse(
            Long id, String codigo, String nombre, String estado, String prioridad, Long clienteId,
            String clienteNombre, List<String> modulos, int avance, long fases, long historias, long tickets,
            List<MiembroResponse> equipo, String liderNombre) {
    }

    public record MiembroResponse(Long id, String nombreCompleto, String iniciales, List<String> roles) {
    }

    // ─── Resumen (PDF pág. 14) ───────────────────────────────────────────────

    public record ResumenResponse(
            InfoResponse info,
            int avance,
            List<ModuloResponse> modulos,
            List<MiembroResponse> equipo,
            List<FaseResponse> fases,
            FaseResponse faseActual,
            long ticketsAbiertos,
            long bugsAbiertos,
            long bugsEnProduccion,
            long mantenimientoAbiertos,
            long ticketsListos,
            long ticketsFaseActual,
            String ritmo,
            SaludResponse salud,
            List<DetenidoResponse> detenidos,
            boolean puedeEditar,
            boolean puedeCrearFase,
            boolean puedeCrearHistoria,
            boolean puedeCrearTicket) {
    }

    public record InfoResponse(
            Long id, String codigo, String nombre, String descripcion, String estado, Long clienteId,
            String clienteNombre, String prioridad, LocalDate fechaInicio, LocalDate fechaEntrega,
            Long liderId, String liderNombre, ReglasDto reglas) {
    }

    public record ModuloResponse(
            Long id, String tipo, String nombre, String tecnologia, String repositorio, String ramaBase,
            String ciEstado, LocalDateTime ciFecha) {
    }

    public record FaseResponse(
            Long id, int numero, String nombre, String estado, LocalDate fechaInicio, LocalDate fechaFin,
            String versionRelease, boolean mantenimiento, List<String> modulos, long historias, long tickets,
            int avance) {
    }

    public record SaludResponse(
            long rechazosQaMes, Double diasPromedioEnQa, BigDecimal horasEstimadas, BigDecimal horasReales,
            long historiasSinTickets) {
    }

    public record DetenidoResponse(Long ticketId, String codigo, String titulo, String estado, LocalDateTime desde,
                                   String motivo) {
    }

    // ─── Fases e historias (PDF pág. 15) ─────────────────────────────────────

    public record FaseArbolResponse(FaseResponse fase, List<HistoriaResponse> historias,
                                    List<TicketResponse> sueltos) {
    }

    public record HistoriaResponse(
            Long id, String codigo, String titulo, String estado, Long analistaId, String analistaIniciales,
            long listos, long total, List<TicketResponse> tickets) {
    }

    /** @param accion botón si me toca; @param quien "Ana · Analista" si le toca a alguien más */
    public record TicketResponse(
            Long id, String tipo, String codigo, String titulo, String estado, String modulo, Long responsableId,
            String responsableIniciales, String responsableNombre, String accion, String quien) {
    }

    // ─── Formulario (PDF pág. 16) ────────────────────────────────────────────

    /** @param formaTrabajo GITFLOW si no se dice; al editar se ignora (ya no cambia) */
    public record ReglasDto(boolean permitirAutovalidacion, boolean comentarioAlRechazar, boolean ramaAlIniciar,
                            boolean notificarClienteFase, FormaTrabajo formaTrabajo, boolean requierePr) {
    }

    public record ModuloDto(
            Long id,
            @NotNull TipoModulo tipo,
            @NotBlank @Size(max = 60) String nombre,
            @Size(max = 80) String tecnologia,
            @Size(max = 200) String repositorio,
            @Size(max = 60) String ramaBase) {
    }

    /**
     * @param clienteId null = proyecto interno
     * @param estado    solo al editar; al crear siempre nace en Planeación
     */
    public record ProyectoRequest(
            @NotBlank @Size(max = 120) String nombre,
            Long clienteId,
            String prioridad,
            EstadoProyecto estado,
            LocalDate fechaInicio,
            LocalDate fechaEntrega,
            String descripcion,
            @NotNull Long liderId,
            Set<Long> miembroIds,
            List<@Valid ModuloDto> modulos,
            @NotNull ReglasDto reglas) {
    }

    public record ConfiguracionResponse(
            Long id, String codigo, String nombre, Long clienteId, String prioridad, String estado,
            LocalDate fechaInicio, LocalDate fechaEntrega, String descripcion, Long liderId, Set<Long> miembroIds,
            List<ModuloDto> modulos, ReglasDto reglas) {
    }

    public record CreadoResponse(Long id) {
    }
}
