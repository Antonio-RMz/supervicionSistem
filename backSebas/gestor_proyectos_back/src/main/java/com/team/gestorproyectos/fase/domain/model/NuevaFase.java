package com.team.gestorproyectos.fase.domain.model;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

import com.team.gestorproyectos.shared.domain.exception.ReglaNegocioException;

/**
 * Lo que se captura en "Nueva fase" (PDF pág. 17) o al editarla: la decisión de qué sale en un release. Lo que pide
 * el cliente y los criterios no van aquí; los escribe el Analista en cada historia. Se limpia y valida al
 * construirse. La fase siempre nace Planeada: se inicia cuando ya tiene historias.
 *
 * @param criteriosManuales  además de los automáticos, ej. "Visto bueno del cliente en la demo"
 */
public record NuevaFase(
        String nombre,
        String objetivo,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        Long responsableId,
        String versionRelease,
        String prioridad,
        Set<Long> moduloIds,
        List<String> criteriosManuales) {

    private static final Set<String> PRIORIDADES = Set.of("BAJA", "MEDIA", "ALTA");

    public NuevaFase {
        nombre = nombre == null ? "" : nombre.trim();
        if (nombre.isEmpty()) {
            throw new ReglaNegocioException("Escribe el nombre de la fase");
        }
        objetivo = objetivo == null || objetivo.isBlank() ? null : objetivo.trim();
        if (fechaInicio == null || fechaFin == null) {
            throw new ReglaNegocioException("Pon el inicio y el fin estimado de la fase");
        }
        if (fechaFin.isBefore(fechaInicio)) {
            throw new ReglaNegocioException("El fin estimado no puede ser antes del inicio");
        }
        if (responsableId == null) {
            throw new ReglaNegocioException("Elige al responsable de la fase");
        }
        versionRelease = versionRelease == null || versionRelease.isBlank() ? null : versionRelease.trim();
        if (versionRelease != null && !VersionRelease.valida(versionRelease)) {
            throw new ReglaNegocioException("La versión debe verse como v1.3.0");
        }
        prioridad = prioridad == null ? "MEDIA" : prioridad.trim().toUpperCase(Locale.ROOT);
        if (!PRIORIDADES.contains(prioridad)) {
            throw new ReglaNegocioException("La prioridad debe ser Baja, Media o Alta");
        }
        moduloIds = moduloIds == null ? Set.of() : Set.copyOf(moduloIds);
        if (moduloIds.isEmpty()) {
            throw new ReglaNegocioException("Elige al menos un repositorio que se trabaje en la fase");
        }
        criteriosManuales = limpios(criteriosManuales);
    }

    private static List<String> limpios(List<String> textos) {
        return textos == null ? List.of()
                : textos.stream().filter(Objects::nonNull).map(String::trim).filter(t -> !t.isEmpty()).toList();
    }
}
