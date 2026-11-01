package com.team.gestorproyectos.proyecto.domain.model;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * La pestaña Releases del proyecto (PDF pág. 36): lo que espera entrar a develop, el próximo release de fase,
 * el parche en preparación y todo lo que ya se publicó.
 *
 * @param listasParaIntegrar historias aceptadas por el Analista que esperan entrar a develop
 * @param puedeIntegrar      el Líder del proyecto (o un Admin) con el permiso historia.integrar
 * @param proximaFaseId      la fase cuyo release sigue; null si no hay ninguna en curso
 * @param parche             el parche en preparación; null si no hay hotfix en camino
 */
public record ReleasesProyecto(List<HistoriaLista> listasParaIntegrar, boolean puedeIntegrar, Long proximaFaseId,
                               ParcheEnPreparacion parche, List<Publicado> publicados) {

    public record HistoriaLista(Long id, int numero, String titulo) {
    }

    /**
     * @param version      la que tendrá al publicarse ("v1.2.1")
     * @param aprobados    hotfix listos para salir
     * @param enCamino     hotfix confirmados, en corrección o en QA
     */
    public record ParcheEnPreparacion(String version, int aprobados, int enCamino, Long mantenimientoFaseId) {
    }

    /**
     * Un release publicado (o revertido).
     *
     * @param tipo         FASE o PARCHE
     * @param estado       PUBLICADA o REVERTIDA
     * @param bugs         de un parche: los bugs que salieron en él
     * @param enProduccion es la versión que está hoy en producción
     */
    public record Publicado(Long id, String version, String tipo, String estado, Long faseId, Integer faseNumero,
                            String faseNombre, LocalDateTime fecha, int historias, int tickets, List<BugDelParche> bugs,
                            String publicadoPor, boolean enProduccion) {
    }

    public record BugDelParche(Long id, String codigo, String titulo) {
    }

    /** La más reciente de las publicadas (no revertidas) es la que está en producción. */
    public static List<Publicado> marcarProduccion(List<Publicado> publicados) {
        Long actual = publicados.stream().filter(p -> "PUBLICADA".equals(p.estado()) && p.fecha() != null)
                .max(Comparator.comparing(Publicado::fecha).thenComparing(Publicado::id))
                .map(Publicado::id).orElse(null);
        return publicados.stream()
                .sorted(Comparator.comparing(Publicado::fecha, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(Publicado::id, Comparator.reverseOrder()))
                .map(p -> new Publicado(p.id(), p.version(), p.tipo(), p.estado(), p.faseId(), p.faseNumero(),
                        p.faseNombre(), p.fecha(), p.historias(), p.tickets(), p.bugs(), p.publicadoPor(),
                        Objects.equals(p.id(), actual)))
                .toList();
    }
}
