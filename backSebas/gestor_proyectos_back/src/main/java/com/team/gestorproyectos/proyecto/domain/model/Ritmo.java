package com.team.gestorproyectos.proyecto.domain.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/** ¿La fase actual va al día? Compara su avance con el tiempo que ya pasó de sus fechas. */
public enum Ritmo {
    EN_TIEMPO, EN_RIESGO, ATRASADO, SIN_FECHAS;

    /** Si el tiempo transcurrido le saca más de esto al avance, la fase va en riesgo. */
    private static final int HOLGURA = 20;

    public static Ritmo de(LocalDate inicio, LocalDate fin, int avance, LocalDate hoy) {
        if (inicio == null || fin == null || !fin.isAfter(inicio)) {
            return SIN_FECHAS;
        }
        if (avance >= 100) {
            return EN_TIEMPO;
        }
        if (hoy.isAfter(fin)) {
            return ATRASADO;
        }
        if (hoy.isBefore(inicio)) {
            return EN_TIEMPO;
        }
        long total = ChronoUnit.DAYS.between(inicio, fin);
        long pasados = ChronoUnit.DAYS.between(inicio, hoy);
        int tiempo = (int) Math.round(pasados * 100.0 / total);
        return tiempo - avance > HOLGURA ? EN_RIESGO : EN_TIEMPO;
    }
}
