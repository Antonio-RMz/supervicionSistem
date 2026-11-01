package com.team.gestorproyectos.ticket.domain.model;

import java.util.Map;
import java.util.Optional;

/**
 * Las cinco columnas del Tablero (PDF pág. 12). Cada tipo de ticket tiene sus propios estados;
 * aquí se acomodan según en qué punto va el trabajo.
 */
public enum ColumnaTablero {
    POR_HACER, EN_PROGRESO, EN_QA, APROBADA, CERRADA;

    private static final Map<String, ColumnaTablero> POR_ESTADO = Map.ofEntries(
            Map.entry("BORRADOR", POR_HACER),
            Map.entry("POR_HACER", POR_HACER),
            Map.entry("REPORTADO", POR_HACER),
            Map.entry("CONFIRMADO", POR_HACER),
            Map.entry("PROPUESTA", POR_HACER),
            Map.entry("EN_PROGRESO", EN_PROGRESO),
            Map.entry("EN_CORRECCION", EN_PROGRESO),
            Map.entry("BLOQUEADA", EN_PROGRESO),
            Map.entry("INVESTIGANDO", EN_PROGRESO),
            Map.entry("REDACTANDO", EN_PROGRESO),
            Map.entry("EN_QA", EN_QA),
            Map.entry("EN_REVISION", EN_QA),
            Map.entry("CONCLUSIONES", EN_QA),
            Map.entry("APROBADO", APROBADA),
            Map.entry("APROBADA_QA", APROBADA),
            Map.entry("HECHA", APROBADA),
            Map.entry("INTEGRADA", APROBADA),
            Map.entry("INTEGRADO", APROBADA),
            Map.entry("PUBLICADO", APROBADA),
            Map.entry("CERRADA", CERRADA),
            Map.entry("CERRADO", CERRADA),
            Map.entry("PUBLICADA", CERRADA),
            Map.entry("REVISADA", CERRADA));

    /** Vacío para lo que ya no se hará (cancelado, no reproducible, mejora rechazada): no sale en el tablero. */
    public static Optional<ColumnaTablero> de(String tipo, String estado) {
        // APROBADA es "aprobada en QA" para una actividad, pero "propuesta aceptada, por iniciar" para una mejora.
        if ("APROBADA".equals(estado)) {
            return Optional.of("MEJR".equals(tipo) ? POR_HACER : APROBADA);
        }
        return Optional.ofNullable(POR_ESTADO.get(estado));
    }
}
