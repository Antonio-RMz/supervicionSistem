package com.team.gestorproyectos.ticket.domain.model;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Un parche: los hotfix aprobados que salen juntos a producción con su propia versión (PDF pág. 23).
 * Hace merge a main, crea el tag, despliega y regresa la corrección a develop en un paso.
 *
 * @param version  "v1.3.1"
 * @param bugs     los hotfix aprobados que incluye ("BUGS-50")
 */
public record Parche(String version, List<Incluido> bugs) {

    private static final Pattern VERSION = Pattern.compile("^v(\\d+)\\.(\\d+)\\.(\\d+)$");

    public record Incluido(Long id, String codigo, String titulo) {
    }

    public Parche {
        bugs = List.copyOf(bugs);
    }

    /** El parche que saldría ahora, después de la versión que está en producción. */
    public static Parche despuesDe(String enProduccion, List<Incluido> aprobados) {
        return new Parche(siguienteVersion(enProduccion), aprobados);
    }

    /** Sube el último número de la versión en producción: v1.3.0 -> v1.3.1. Sin versiones, v1.0.1. */
    public static String siguienteVersion(String enProduccion) {
        Matcher m = enProduccion == null ? null : VERSION.matcher(enProduccion);
        if (m == null || !m.matches()) {
            return "v1.0.1";
        }
        return "v" + m.group(1) + "." + m.group(2) + "." + (Integer.parseInt(m.group(3)) + 1);
    }
}
