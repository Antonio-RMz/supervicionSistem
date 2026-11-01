package com.team.gestorproyectos.fase.domain.model;

import java.util.Collection;
import java.util.Comparator;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** La versión que se publica al cerrar una fase: v1.3.0. El tag se crea al cerrar. */
public final class VersionRelease {

    private static final Pattern FORMATO = Pattern.compile("^v(\\d+)\\.(\\d+)\\.(\\d+)$");

    private VersionRelease() {
    }

    public static boolean valida(String version) {
        return FORMATO.matcher(version).matches();
    }

    /** Sube el número de en medio de la versión más alta: v1.2.0 -> v1.3.0. Sin versiones, v1.0.0. */
    public static String siguiente(Collection<String> existentes) {
        return existentes.stream()
                .filter(Objects::nonNull)
                .map(FORMATO::matcher)
                .filter(Matcher::matches)
                .map(m -> new int[] {Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2))})
                .max(Comparator.<int[]>comparingInt(v -> v[0]).thenComparingInt(v -> v[1]))
                .map(v -> "v" + v[0] + "." + (v[1] + 1) + ".0")
                .orElse("v1.0.0");
    }
}
