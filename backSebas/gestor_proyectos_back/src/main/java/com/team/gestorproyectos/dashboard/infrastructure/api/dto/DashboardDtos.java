package com.team.gestorproyectos.dashboard.infrastructure.api.dto;

import java.time.LocalDateTime;
import java.util.List;

/** Respuesta JSON del Dashboard. Los estados viajan como texto; el front pone etiqueta y color. */
public final class DashboardDtos {

    private DashboardDtos() {
    }

    public record DashboardResponse(
            Long proyectoId,
            String proyectoNombre,
            MetricasResponse metricas,
            int pendientesMios,
            List<ItemPendienteResponse> teToca,
            List<ItemPendienteResponse> esperandoAOtros,
            List<FaseResponse> fases,
            List<MovimientoResponse> actividad) {
    }

    public record MetricasResponse(
            long ticketsAbiertos,
            long abiertosEstaSemana,
            long enQa,
            long enQaMasDe3Dias,
            long bugsAbiertos,
            long bugsPrioridadAlta,
            long historiasCerradasSemana,
            long ticketsCerradosSemana) {
    }

    /**
     * @param actorNombre y actorRol solo importan en "Esperando a otros" ("Ana · Analista")
     * @param ejecutable  el botón de "Te toca a ti" hace la acción ahí mismo
     */
    public record ItemPendienteResponse(
            Long id,
            String tipo,
            String codigo,
            String titulo,
            String estado,
            String accion,
            String actorNombre,
            String actorRol,
            boolean ejecutable) {
    }

    public record FaseResponse(Long id, int numero, String nombre, String estado, int avance) {
    }

    public record MovimientoResponse(String descripcion, LocalDateTime fecha) {
    }
}
