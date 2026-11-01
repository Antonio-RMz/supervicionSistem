package com.team.gestorproyectos.fase.domain.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Las notas del release (PDF pág. 27): se arman solas con los tickets de la fase y el Líder puede
 * editarlas antes de publicar. Actividades = Nuevo, mejoras = Mejorado, bugs = Corregido.
 */
public final class NotasRelease {

    public static final int LARGO_MAXIMO = 5000;

    private static final Map<String, String> SECCIONES = Map.of(
            "ACTV", "Nuevo",
            "MEJR", "Mejorado",
            "BUGS", "Corregido");

    private static final List<String> ORDEN = List.of("ACTV", "MEJR", "BUGS");

    private NotasRelease() {
    }

    /** Un ticket que entra en las notas. */
    public record Cambio(String tipo, String codigo, String titulo) {
    }

    /** Solo cuentan actividades, mejoras y bugs. */
    public static boolean entra(String tipo) {
        return SECCIONES.containsKey(tipo);
    }

    /**
     * Nuevo
     * - Consulta de pedidos con filtro por estado
     *
     * Corregido
     * - El token JWT expiraba antes de tiempo
     */
    public static String sugeridas(List<Cambio> cambios) {
        List<String> lineas = new ArrayList<>();
        for (String tipo : ORDEN) {
            List<Cambio> deEsteTipo = cambios.stream().filter(c -> tipo.equals(c.tipo())).toList();
            if (deEsteTipo.isEmpty()) {
                continue;
            }
            if (!lineas.isEmpty()) {
                lineas.add("");
            }
            lineas.add(SECCIONES.get(tipo));
            deEsteTipo.forEach(c -> lineas.add("- " + c.titulo()));
        }
        return String.join("\n", lineas);
    }
}
