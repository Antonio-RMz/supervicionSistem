package com.team.gestorproyectos.ticket.domain.model;

import java.util.Locale;
import java.util.Set;

import com.team.gestorproyectos.shared.domain.exception.ReglaNegocioException;

/**
 * "Nuevo ticket de mantenimiento" (PDF pág. 38): trabajo fuera de las fases que sale con la siguiente fase,
 * desde develop. Lo urgente no entra por aquí sino por Reportar falla.
 *
 * @param severidad    solo de un bug menor: MEDIA o BAJA (provisional, el Líder la confirma)
 * @param responsableId a quién se asigna; la tarea técnica lo pide, la mejora y el bug lo reciben al aprobarse
 */
public record NuevoTicketMantenimiento(
        TipoTicket tipo,
        String titulo,
        String descripcion,
        Long moduloId,
        String prioridad,
        String severidad,
        Long responsableId) {

    /** Mejora pequeña y bug menor. */
    public static final Set<TipoTicket> TIPOS = Set.of(TipoTicket.MEJR, TipoTicket.BUGS);

    private static final Set<String> PRIORIDADES = Set.of("BAJA", "MEDIA", "ALTA");
    private static final Set<String> SEVERIDADES_MENORES = Set.of("MEDIA", "BAJA");

    public NuevoTicketMantenimiento {
        if (tipo == null || !TIPOS.contains(tipo)) {
            throw new ReglaNegocioException("En Mantenimiento se crean mejoras pequeñas o bugs menores");
        }
        titulo = titulo == null ? "" : titulo.trim();
        if (titulo.isEmpty()) {
            throw new ReglaNegocioException("Escribe el título");
        }
        descripcion = descripcion == null || descripcion.isBlank() ? null : descripcion.trim();
        if (moduloId == null) {
            throw new ReglaNegocioException("Elige el repositorio");
        }
        prioridad = prioridad == null || prioridad.isBlank() ? "MEDIA" : prioridad.trim().toUpperCase(Locale.ROOT);
        if (!PRIORIDADES.contains(prioridad)) {
            throw new ReglaNegocioException("La prioridad debe ser Baja, Media o Alta");
        }
        if (tipo == TipoTicket.BUGS) {
            severidad = severidad == null || severidad.isBlank() ? "MEDIA" : severidad.trim().toUpperCase(Locale.ROOT);
            if (!SEVERIDADES_MENORES.contains(severidad)) {
                throw new ReglaNegocioException("Un bug Crítico o Alto va por hotfix: usa Reportar falla en producción");
            }
        } else {
            severidad = null;
        }
    }
}
