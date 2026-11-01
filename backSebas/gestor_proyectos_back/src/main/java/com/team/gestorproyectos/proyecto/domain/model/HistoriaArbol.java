package com.team.gestorproyectos.proyecto.domain.model;

import java.util.List;

/** @param listos tickets que ya funcionan, de los que cuentan ("3/5 listos") */
public record HistoriaArbol(
        Long id,
        String codigo,
        String titulo,
        String estado,
        Long analistaId,
        String analistaIniciales,
        long listos,
        long total,
        List<TicketArbol> tickets) {
}
