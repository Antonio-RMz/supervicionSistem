package com.team.gestorproyectos.fase.domain.model;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import com.team.gestorproyectos.fase.domain.model.InfoFase.TicketsEnEstado;

/** Los cinco grupos de "Tickets por estado" del detalle de la fase (PDF pág. 32). Junta todos los tipos. */
public enum GrupoTickets {
    POR_HACER("Por hacer"),
    EN_PROGRESO("En progreso"),
    EN_QA("En QA"),
    APROBADOS("Aprobados"),
    CERRADOS("Cerrados");

    private static final Set<String> FUERA = Set.of("CANCELADA", "NO_REPRODUCIBLE", "RECHAZADA");
    private static final Set<String> CERRADO = Set.of(
            "INTEGRADA", "INTEGRADO", "PUBLICADA", "PUBLICADO", "CERRADA", "CERRADO", "REVISADA");
    private static final Set<String> APROBADO = Set.of("APROBADO", "APROBADA_QA", "HECHA");
    private static final Set<String> REVISION = Set.of("EN_QA", "EN_REVISION", "CONCLUSIONES");
    private static final Set<String> TRABAJANDO = Set.of(
            "EN_PROGRESO", "EN_CORRECCION", "INVESTIGANDO", "REDACTANDO", "BLOQUEADA");

    private final String nombre;

    GrupoTickets(String nombre) {
        this.nombre = nombre;
    }

    public String nombre() {
        return nombre;
    }

    /** null si el ticket ya no cuenta (cancelado, rechazado...). */
    public static GrupoTickets de(String tipo, String estado) {
        if (FUERA.contains(estado)) {
            return null;
        }
        if (CERRADO.contains(estado)) {
            return CERRADOS;
        }
        // En una mejora "Aprobada" es la propuesta aceptada, todavía sin empezar.
        if (APROBADO.contains(estado) || ("APROBADA".equals(estado) && !"MEJR".equals(tipo))) {
            return APROBADOS;
        }
        if (REVISION.contains(estado)) {
            return EN_QA;
        }
        return TRABAJANDO.contains(estado) ? EN_PROGRESO : POR_HACER;
    }

    public record Conteo(String nombre, long cantidad) {
    }

    public static List<Conteo> contar(List<TicketsEnEstado> tickets) {
        return Arrays.stream(values())
                .map(g -> new Conteo(g.nombre(), tickets.stream()
                        .filter(t -> g == de(t.tipo(), t.estado()))
                        .mapToLong(TicketsEnEstado::cantidad).sum()))
                .toList();
    }
}
