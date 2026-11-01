package com.team.gestorproyectos.historia.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.team.gestorproyectos.historia.domain.model.HistoriaParaIntegrar.Ambiente;
import com.team.gestorproyectos.historia.domain.model.HistoriaParaIntegrar.HistoriaDeFase;
import com.team.gestorproyectos.historia.domain.model.HistoriaParaIntegrar.Modulo;
import com.team.gestorproyectos.historia.domain.model.HistoriaParaIntegrar.Publicada;
import com.team.gestorproyectos.historia.domain.model.HistoriaParaIntegrar.PullRequest;
import com.team.gestorproyectos.historia.domain.model.HistoriaParaIntegrar.Resultado;
import com.team.gestorproyectos.historia.domain.model.HistoriaParaIntegrar.Revision;
import com.team.gestorproyectos.historia.domain.model.HistoriaParaIntegrar.TicketAIntegrar;

class HistoriaParaIntegrarTest {

    private static final Long API = 1L;
    private static final Long APP = 3L;
    private static final LocalDateTime HOY = LocalDateTime.of(2026, 6, 20, 12, 10);

    private static final Modulo PORTAL_API = new Modulo(API, "API", "portal-api", "Spring Boot", 2);
    private static final Modulo PORTAL_APP = new Modulo(APP, "MOVIL", "portal-app", "Flutter", 1);

    private static PullRequest pr(int numero, String estado, String ci, boolean conflictos) {
        return new PullRequest(numero, estado, ci, conflictos, HOY);
    }

    private static TicketAIntegrar ticket(long id, String tipo, String estado, Long modulo, PullRequest pr) {
        return new TicketAIntegrar(id, tipo, (int) id, "Ticket " + id, estado, modulo,
                "feature/" + tipo.toLowerCase() + "-" + id, pr);
    }

    /** Como HU-0016 en el PDF: MEJR-07 ya en develop, ACTV-41 y ACTV-42 aprobados con su PR. */
    private static List<TicketAIntegrar> comoEnElPdf() {
        return List.of(
                ticket(7, "MEJR", "APROBADA_QA", API, pr(41, "INTEGRADO", "PASO", false)),
                ticket(41, "ACTV", "APROBADA", API, pr(45, "APROBADO", "PASO", false)),
                ticket(42, "ACTV", "APROBADA", APP, pr(12, "APROBADO", "PASO", false)));
    }

    private static HistoriaParaIntegrar historia(String estado, List<TicketAIntegrar> tickets) {
        return new HistoriaParaIntegrar("HU-0016", estado, 4, "v1.3.0", "Ana López", HOY, tickets,
                List.of(PORTAL_API, PORTAL_APP),
                List.of(new HistoriaDeFase(16L, 16, estado), new HistoriaDeFase(14L, 14, "INTEGRADA"),
                        new HistoriaDeFase(15L, 15, "INTEGRADA"), new HistoriaDeFase(17L, 17, "EN_DESARROLLO")),
                HOY.minusDays(1), new Publicada("v1.2.0", HOY.minusDays(50)));
    }

    @Test
    void conTodoEnOrdenSePuedeIntegrar() {
        HistoriaParaIntegrar h = historia("ACEPTADA", comoEnElPdf());

        assertThat(h.impedimentos()).isEmpty();
        assertThat(h.revision()).extracting(Revision::resultado).containsOnly(Resultado.OK);
        assertThat(h.revision()).extracting(Revision::detalle)
                .contains("Ana López", "3 de 3", "2 pendientes de merge, 1 ya integrado", "portal-api y portal-app");
        assertThat(h.porIntegrar()).extracting(TicketAIntegrar::codigo).containsExactly("ACTV-41", "ACTV-42");
        assertThat(h.queSeIntegra()).hasSize(2);
        assertThat(h.queSeIntegra().get(0).tickets()).hasSize(2);
    }

    @Test
    void unPrSinAprobarConCiRojoOConflictosImpideIntegrar() {
        HistoriaParaIntegrar h = historia("ACEPTADA", List.of(
                ticket(41, "ACTV", "APROBADA", API, pr(45, "ABIERTO", "FALLO", false)),
                ticket(42, "ACTV", "APROBADA", APP, pr(12, "APROBADO", "PENDIENTE", true))));

        assertThat(h.impedimentos()).containsExactly(
                "El PR #45 de ACTV-41 aún no se aprueba en revisión de código",
                "El CI falló en el PR #45",
                "El CI sigue corriendo en el PR #12",
                "El PR #12 tiene conflictos con develop");
        assertThat(h.revision()).extracting(Revision::resultado)
                .containsExactly(Resultado.OK, Resultado.OK, Resultado.FALTA, Resultado.FALTA, Resultado.FALTA);
    }

    @Test
    void unTicketSinPrSoloSeAvisaYSuRamaSeMezclaIgual() {
        HistoriaParaIntegrar h = historia("ACEPTADA", List.of(
                ticket(41, "ACTV", "APROBADA", API, pr(45, "APROBADO", "PASO", false)),
                ticket(43, "ACTV", "APROBADA", APP, null)));

        assertThat(h.impedimentos()).isEmpty();
        assertThat(h.revision().get(2).resultado()).isEqualTo(Resultado.AVISO);
        assertThat(h.revision().get(2).detalle()).isEqualTo("Sin PR registrado: ACTV-43");
        assertThat(h.pasos().get(0))
                .isEqualTo("Merge de PR #45 (portal-api) y la rama de ACTV-43 (portal-app) a develop con squash");
    }

    @Test
    void ticketsSinAprobarYHistoriaSinAceptarImpidenIntegrar() {
        HistoriaParaIntegrar h = historia("EN_VALIDACION", List.of(
                ticket(41, "ACTV", "EN_QA", API, null),
                ticket(44, "ACTV", "CANCELADA", API, null),
                ticket(2, "BUGS", "APROBADO", null, null)));

        assertThat(h.impedimentos()).containsExactly("Falta que el Analista la acepte", "ACTV-41 aún no se aprueba en QA");
        assertThat(h.revision().get(1).detalle()).isEqualTo("1 de 2");
        // Ni la cancelada ni la investigación llevan código a develop.
        assertThat(h.queSeIntegra()).hasSize(1);
        assertThat(h.queSeIntegra().get(0).tickets()).extracting(TicketAIntegrar::codigo).containsExactly("ACTV-41");
    }

    @Test
    void losPasosDicenQueSeMezclaYEnQueVersionQuedaStaging() {
        assertThat(historia("ACEPTADA", comoEnElPdf()).pasos()).containsExactly(
                "Merge de PR #45 (portal-api) y PR #12 (portal-app) a develop con squash",
                "Se borran las ramas feature/… ya integradas",
                "El CI despliega develop en staging: portal-api 1.3.0-rc3 y portal-app build 1.3.0-rc2",
                "HU-0016 pasa a Integrada y queda incluida en el release v1.3.0");
    }

    @Test
    void losAmbientesLlevanLaCandidataDeLaFaseYLoPublicado() {
        List<Ambiente> ambientes = historia("ACEPTADA", comoEnElPdf()).ambientes();

        assertThat(ambientes).extracting(Ambiente::nombre).containsExactly("develop", "staging", "producción (main)");
        assertThat(ambientes).extracting(Ambiente::version).containsExactly("1.3.0-rc2", "1.3.0-rc2", "v1.2.0");
        assertThat(ambientes.get(1).detalle()).isEqualTo("cambia a rc3");
    }

    @Test
    void siYaTodoEstaEnDevelopNoHayRamasQueMezclar() {
        HistoriaParaIntegrar h = historia("ACEPTADA", List.of(ticket(7, "MEJR", "APROBADA_QA", API, pr(41, "INTEGRADO", "PASO", false))));

        assertThat(h.porIntegrar()).isEmpty();
        assertThat(h.revision().get(2).detalle()).isEqualTo("Todo ya está en develop");
        assertThat(h.pasos().get(0)).isEqualTo("Su código ya está en develop: no hay ramas que mezclar");
        assertThat(h.pasos()).hasSize(3);
    }
}
