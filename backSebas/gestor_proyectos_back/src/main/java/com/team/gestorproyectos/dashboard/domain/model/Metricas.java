package com.team.gestorproyectos.dashboard.domain.model;

import java.time.LocalDateTime;
import java.util.List;

import com.team.gestorproyectos.shared.domain.model.ItemTrabajo;

/** Las cuatro tarjetas de arriba del Dashboard. */
public record Metricas(
        long ticketsAbiertos,
        long abiertosEstaSemana,
        long enQa,
        long enQaMasDe3Dias,
        long bugsAbiertos,
        long bugsPrioridadAlta,
        long historiasCerradasSemana,
        long ticketsCerradosSemana) {

    /** items: tickets e historias abiertos. Las métricas de tickets ignoran las historias. */
    public static Metricas calcular(List<ItemTrabajo> items, ConteoCierres cierres, LocalDateTime ahora) {
        List<ItemTrabajo> tickets = items.stream().filter(ItemTrabajo::esTicket).toList();
        LocalDateTime haceUnaSemana = ahora.minusDays(7);
        LocalDateTime haceTresDias = ahora.minusDays(3);

        List<ItemTrabajo> enQa = tickets.stream().filter(t -> "EN_QA".equals(t.estado())).toList();
        List<ItemTrabajo> bugs = tickets.stream().filter(t -> "BUGS".equals(t.tipo())).toList();

        return new Metricas(
                tickets.size(),
                tickets.stream().filter(t -> t.creadoEn() != null && t.creadoEn().isAfter(haceUnaSemana)).count(),
                enQa.size(),
                enQa.stream().filter(t -> t.actualizadoEn() != null && t.actualizadoEn().isBefore(haceTresDias)).count(),
                bugs.size(),
                bugs.stream().filter(t -> "ALTA".equals(t.prioridad())).count(),
                cierres.historias(),
                cierres.tickets());
    }
}
