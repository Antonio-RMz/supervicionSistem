package com.team.gestorproyectos.shared.domain.model;

import java.util.Set;

/** Cuántos tickets hay en cierto estado, dentro de una fase y un módulo. */
public record ConteoTickets(Long faseId, Long moduloId, String estado, long cantidad) {

    /** Estados en los que el trabajo ya funciona (aprobado en QA o más adelante). Cubre todos los tipos de ticket. */
    private static final Set<String> TERMINADOS = Set.of(
            "APROBADA", "APROBADO", "APROBADA_QA", "HECHA",
            "INTEGRADA", "INTEGRADO", "PUBLICADO", "PUBLICADA",
            "CERRADA", "CERRADO", "REVISADA");

    /** Estados que no cuentan para el avance porque ese trabajo ya no se hará. */
    private static final Set<String> FUERA = Set.of("CANCELADA", "NO_REPRODUCIBLE", "RECHAZADA");

    public boolean cuenta() {
        return !FUERA.contains(estado);
    }

    public boolean terminado() {
        return TERMINADOS.contains(estado);
    }
}
