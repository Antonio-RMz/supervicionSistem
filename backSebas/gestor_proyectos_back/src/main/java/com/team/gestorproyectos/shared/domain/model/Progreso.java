package com.team.gestorproyectos.shared.domain.model;

import java.util.Collection;

/** Tickets terminados contra tickets totales. */
public record Progreso(long terminados, long total) {

    public static Progreso de(Collection<ConteoTickets> conteos) {
        long total = 0;
        long terminados = 0;
        for (ConteoTickets conteo : conteos) {
            if (!conteo.cuenta()) {
                continue;
            }
            total += conteo.cantidad();
            if (conteo.terminado()) {
                terminados += conteo.cantidad();
            }
        }
        return new Progreso(terminados, total);
    }

    /** Avance de una fase: publicada = 100%; si no, sus tickets terminados contra los totales. */
    public static int deFase(Long faseId, String estado, Collection<ConteoTickets> conteosDelProyecto) {
        if ("PUBLICADA".equals(estado)) {
            return 100;
        }
        return de(conteosDelProyecto.stream().filter(c -> faseId.equals(c.faseId())).toList()).porcentaje();
    }

    public int porcentaje() {
        return total == 0 ? 0 : (int) Math.round(terminados * 100.0 / total);
    }
}
