package com.team.gestorproyectos.shared.domain.model;

import java.util.List;
import java.util.Optional;

/**
 * Lo que la historia hace sola según sus tickets (PDF pág. 21): pasa a En validación (la prueba su Analista)
 * cuando todos sus PR están aprobados y regresa a En desarrollo si alguno vuelve a trabajarse.
 */
public final class AvanceHistoria {

    private AvanceHistoria() {
    }

    public record TicketEstado(String tipo, String estado) {

        boolean cuenta() {
            return new ConteoTickets(null, null, estado, 1).cuenta();
        }

        /** En una mejora "Aprobada" es la propuesta aceptada, aún sin empezar. */
        boolean terminado() {
            return new ConteoTickets(null, null, estado, 1).terminado()
                    && !("MEJR".equals(tipo) && "APROBADA".equals(estado));
        }
    }

    /** El estado al que debe pasar la historia; vacío si se queda como está. */
    public static Optional<String> siguienteEstado(String estadoHistoria, List<TicketEstado> tickets) {
        List<TicketEstado> cuentan = tickets.stream().filter(TicketEstado::cuenta).toList();
        boolean todosTerminados = !cuentan.isEmpty() && cuentan.stream().allMatch(TicketEstado::terminado);
        if ("EN_DESARROLLO".equals(estadoHistoria) && todosTerminados) {
            return Optional.of("EN_VALIDACION");
        }
        if ("EN_VALIDACION".equals(estadoHistoria) && !todosTerminados) {
            return Optional.of("EN_DESARROLLO");
        }
        return Optional.empty();
    }
}
