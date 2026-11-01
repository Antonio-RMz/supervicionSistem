package com.team.gestorproyectos.ticket.domain.model;

import java.util.Locale;
import java.util.Set;

import com.team.gestorproyectos.shared.domain.exception.ReglaNegocioException;

/**
 * "Reportar falla en producción" (PDF pág. 39). Cualquiera del equipo puede reportar; nace como bug Reportado
 * en Mantenimiento y el Líder confirma la severidad.
 *
 * @param severidad        provisional: CRITICA o ALTA va por hotfix desde main, MEDIA o BAJA con la siguiente fase
 * @param historiaOrigenId "Viene de", opcional
 */
public record ReporteFalla(String titulo, Long moduloId, String severidad, String descripcion, Long historiaOrigenId) {

    private static final Set<String> SEVERIDADES = Set.of("CRITICA", "ALTA", "MEDIA", "BAJA");
    private static final Set<String> HOTFIX = Set.of("CRITICA", "ALTA");

    public ReporteFalla {
        titulo = titulo == null ? "" : titulo.trim();
        if (titulo.isEmpty()) {
            throw new ReglaNegocioException("Escribe qué falla");
        }
        if (moduloId == null) {
            throw new ReglaNegocioException("Elige el repositorio donde falla");
        }
        severidad = severidad == null ? null : severidad.trim().toUpperCase(Locale.ROOT);
        if (severidad == null || !SEVERIDADES.contains(severidad)) {
            throw new ReglaNegocioException("Elige la severidad: Crítica, Alta, Media o Baja");
        }
        descripcion = descripcion == null ? "" : descripcion.trim();
        if (descripcion.isEmpty()) {
            throw new ReglaNegocioException("Cuenta qué pasó y cómo reproducirlo");
        }
    }

    /** Igual que TicketParaMover.esHotfix: Crítica o Alta sale de main y se publica en un parche. */
    public boolean vaPorHotfix() {
        return HOTFIX.contains(severidad);
    }
}
