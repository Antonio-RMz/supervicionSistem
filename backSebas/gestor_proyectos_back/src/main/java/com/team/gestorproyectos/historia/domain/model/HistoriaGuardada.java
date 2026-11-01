package com.team.gestorproyectos.historia.domain.model;

import java.util.List;
import java.util.Set;

/** Cómo está guardada la historia hoy, para llenar el formulario de Editar historia. */
public record HistoriaGuardada(
        Long id,
        int numero,
        String estado,
        Long faseId,
        String titulo,
        String como,
        String quiero,
        String para,
        List<String> criterios,
        String figmaUrl,
        boolean disenoAprobado,
        Long analistaId,
        String prioridad,
        Integer puntos,
        Set<Long> moduloIds) {

    public HistoriaGuardada {
        criterios = List.copyOf(criterios);
        moduloIds = Set.copyOf(moduloIds);
    }
}
