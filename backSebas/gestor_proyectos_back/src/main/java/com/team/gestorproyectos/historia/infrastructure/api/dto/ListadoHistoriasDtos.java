package com.team.gestorproyectos.historia.infrastructure.api.dto;

import java.time.LocalDateTime;
import java.util.List;

/** JSON del menú Historias. */
public final class ListadoHistoriasDtos {

    private ListadoHistoriasDtos() {
    }

    public record ListadoHistoriasResponse(Long proyectoId, String proyectoNombre,
                                           List<HistoriaDeListaResponse> historias, boolean puedeCrear) {
    }

    /** @param codigo "HU-0016" */
    public record HistoriaDeListaResponse(Long id, String codigo, String titulo, String estado, String prioridad,
                                          Integer puntos, Long faseId, int faseNumero, String faseNombre,
                                          String analistaNombre, int ticketsTerminados, int ticketsTotal,
                                          LocalDateTime actualizadoEn) {
    }
    /** Las historias de la fase completas, para copiarlas como JSON. */
    public record HistoriasDeFaseResponse(String proyectoNombre, int faseNumero, String faseNombre,
                                          List<HistoriaCompletaResponse> historias) {
    }

    public record HistoriaCompletaResponse(String codigo, String estado, String titulo, String como, String quiero,
                                           String para, String prioridad, Integer puntos, String figmaUrl,
                                           List<String> criterios) {
    }
}
