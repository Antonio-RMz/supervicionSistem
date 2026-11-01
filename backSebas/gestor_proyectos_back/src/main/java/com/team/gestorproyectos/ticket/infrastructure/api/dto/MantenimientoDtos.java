package com.team.gestorproyectos.ticket.infrastructure.api.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import com.team.gestorproyectos.ticket.domain.model.TipoTicket;
import com.team.gestorproyectos.ticket.infrastructure.api.dto.NuevoTicketDtos.ModuloResponse;
import com.team.gestorproyectos.ticket.infrastructure.api.dto.NuevoTicketDtos.PersonaResponse;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** JSON de la vista de Mantenimiento (PDF pág. 37) y de sus modales (págs. 38 y 39). */
public final class MantenimientoDtos {

    private MantenimientoDtos() {
    }

    public record MantenimientoResponse(Long faseId, String enProduccion, String siguienteParche,
                                        long urgentesConfirmados, long criticos, List<TicketResponse> urgentes,
                                        List<TicketResponse> listos, List<TicketResponse> normales,
                                        Integer proximaFaseNumero, Double horasMedioHotfix,
                                        List<ParcheResponse> parches, boolean puedePublicar) {
    }

    /** @param severidad CRITICA, ALTA, MEDIA o BAJA; null si aún no se confirma */
    public record TicketResponse(Long id, String tipo, String codigo, String titulo, String estado, String severidad,
                                 String rama, Long responsableId, String responsableNombre, boolean porConfirmar) {
    }

    public record ParcheResponse(String version, LocalDateTime fecha, int bugs) {
    }

    /** @param siguientes número que tendría un ticket de cada tipo: {"BUGS": 53, ...} */
    public record PreparacionResponse(Long proyectoId, Long faseId, Long liderId, String enProduccion,
                                      Integer proximaFaseNumero, List<ModuloResponse> modulos,
                                      List<PersonaResponse> equipo, Map<String, Integer> siguientes,
                                      List<HistoriaResponse> historias, boolean puedeCrear, boolean puedeReportar) {
    }

    /** @param codigo "HU-0017" */
    public record HistoriaResponse(Long id, String codigo, String titulo) {
    }

    public record NuevoTicketRequest(
            @NotNull TipoTicket tipo,
            @NotBlank @Size(max = 200) String titulo,
            String descripcion,
            @NotNull Long moduloId,
            String prioridad,
            String severidad,
            Long responsableId) {
    }

    public record ReporteFallaRequest(
            @NotBlank @Size(max = 200) String titulo,
            @NotNull Long moduloId,
            @NotBlank String severidad,
            @NotBlank String descripcion,
            Long historiaOrigenId) {
    }
}
