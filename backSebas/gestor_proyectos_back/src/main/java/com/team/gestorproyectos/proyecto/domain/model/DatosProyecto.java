package com.team.gestorproyectos.proyecto.domain.model;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import com.team.gestorproyectos.shared.domain.exception.ReglaNegocioException;

/**
 * Lo que se captura en "Nuevo proyecto" o al editarlo. Se limpia y valida al construirse.
 *
 * @param clienteId null = proyecto interno (no es para un cliente)
 * @param estado    al crear siempre es PLANEACION; al editar se puede cambiar
 * @param miembroIds quién entra al proyecto; el Líder siempre queda incluido
 */
public record DatosProyecto(
        String nombre,
        Long clienteId,
        String prioridad,
        EstadoProyecto estado,
        LocalDate fechaInicio,
        LocalDate fechaEntrega,
        String descripcion,
        Long liderId,
        Set<Long> miembroIds,
        List<ModuloProyecto> modulos,
        ReglasFlujo reglas) {

    private static final Set<String> PRIORIDADES = Set.of("BAJA", "MEDIA", "ALTA");

    public DatosProyecto {
        nombre = nombre == null ? "" : nombre.trim();
        if (nombre.isEmpty()) {
            throw new ReglaNegocioException("Escribe el nombre del proyecto");
        }
        if (liderId == null) {
            throw new ReglaNegocioException("Elige al Líder del proyecto");
        }
        prioridad = prioridad == null ? "MEDIA" : prioridad.trim().toUpperCase(Locale.ROOT);
        if (!PRIORIDADES.contains(prioridad)) {
            throw new ReglaNegocioException("La prioridad debe ser Baja, Media o Alta");
        }
        if (fechaInicio != null && fechaEntrega != null && fechaEntrega.isBefore(fechaInicio)) {
            throw new ReglaNegocioException("La fecha de entrega no puede ser antes del inicio");
        }
        descripcion = descripcion == null || descripcion.isBlank() ? null : descripcion.trim();

        Set<Long> miembros = new LinkedHashSet<>();
        miembros.add(liderId);
        if (miembroIds != null) {
            miembros.addAll(miembroIds);
        }
        miembroIds = Set.copyOf(miembros);

        reglas = reglas == null ? new ReglasFlujo(false, false, false, false, null, true) : reglas;
        modulos = modulos == null ? List.of() : List.copyOf(modulos);
        Set<String> nombres = new HashSet<>();
        for (ModuloProyecto modulo : modulos) {
            if (modulo.nombre().isEmpty()) {
                throw new ReglaNegocioException("Cada repositorio necesita un nombre, ej. portal-api");
            }
            if (!nombres.add(modulo.nombre().toLowerCase(Locale.ROOT))) {
                throw new ReglaNegocioException("Hay dos repositorios llamados " + modulo.nombre());
            }
        }
    }
}
