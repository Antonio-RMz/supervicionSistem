package com.team.gestorproyectos.proyecto.domain.model;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/** El código corto del proyecto ("PC" para Portal de Clientes). Es único y va en los avatares. */
public final class CodigoProyecto {

    private static final int LARGO_MAXIMO = 5;
    private static final Set<String> PALABRAS_VACIAS = Set.of("de", "del", "la", "las", "el", "los", "y", "para", "en");

    private CodigoProyecto() {
    }

    /** Iniciales de las dos primeras palabras; si ya existe, se le agrega un número: PC, PC2, PC3... */
    public static String sugerir(String nombre, Set<String> existentes) {
        String[] palabras = Arrays.stream(sinAcentos(nombre).split("[^A-Za-z0-9]+"))
                .filter(p -> !p.isBlank() && !PALABRAS_VACIAS.contains(p.toLowerCase(Locale.ROOT)))
                .toArray(String[]::new);
        String base = switch (palabras.length) {
            case 0 -> "PR";
            case 1 -> palabras[0].substring(0, Math.min(2, palabras[0].length()));
            default -> Arrays.stream(palabras).limit(2).map(p -> p.substring(0, 1)).collect(Collectors.joining());
        };
        base = base.toUpperCase(Locale.ROOT);

        Set<String> ocupados = existentes.stream().map(c -> c.toUpperCase(Locale.ROOT)).collect(Collectors.toSet());
        String codigo = base;
        for (int n = 2; ocupados.contains(codigo); n++) {
            String sufijo = String.valueOf(n);
            codigo = base.substring(0, Math.min(base.length(), LARGO_MAXIMO - sufijo.length())) + sufijo;
        }
        return codigo;
    }

    private static String sinAcentos(String texto) {
        return Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
    }
}
