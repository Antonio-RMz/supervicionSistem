package com.team.gestorproyectos.fase.domain.model;

import java.time.LocalDate;
import java.util.List;

/**
 * Lo que necesita el formulario de Nueva fase: número, sugerencias y opciones.
 *
 * @param hayFaseActiva si ya hay una en curso (la nueva espera su turno para iniciar)
 */
public record PreparacionFase(
        Long proyectoId,
        String proyectoNombre,
        int numero,
        String versionSugerida,
        LocalDate inicioSugerido,
        Long liderId,
        boolean hayFaseActiva,
        String faseActiva,
        List<ModuloOpcion> modulos,
        List<Responsable> equipo,
        List<String> criteriosAutomaticos) {
}
