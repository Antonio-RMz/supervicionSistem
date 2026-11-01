package com.team.gestorproyectos.historia.domain.model;

import java.util.List;

/**
 * Una historia del JSON de "Importar historias", tal como la devuelve la IA. Solo título y Como son
 * obligatorios; lo que falte se completa después y la historia queda marcada como incompleta.
 */
public record HistoriaImportada(
        String titulo,
        String como,
        String quiero,
        String para,
        String prioridad,
        Integer puntos,
        List<String> criterios) {
}
