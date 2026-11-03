package com.team.gestorproyectos.ticket.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.team.gestorproyectos.ticket.domain.model.Mantenimiento.TicketDeMantenimiento;

class MantenimientoTest {

    private static TicketDeMantenimiento t(String tipo, int numero, String estado, String severidad) {
        return new TicketDeMantenimiento((long) numero, tipo, numero, tipo + " " + numero, estado, severidad, null,
                3L, "Pedro Ramírez", null, null, 0);
    }

    /** Como en el PDF: dos urgentes en camino, uno listo para el parche y el trabajo normal. */
    private static final List<TicketDeMantenimiento> TICKETS = List.of(
            t("BUGS", 52, "CONFIRMADO", "ALTA"),
            t("BUGS", 50, "EN_CORRECCION", "CRITICA"),
            t("BUGS", 48, "APROBADO", "ALTA"),
            t("BUGS", 51, "REPORTADO", null),
            t("BUGS", 45, "POR_HACER", "BAJA"),
            t("MEJR", 12, "PROPUESTA", null),
            t("MEJR", 20, "EN_PROGRESO", null),
            t("BUGS", 40, "PUBLICADO", "CRITICA"),
            t("BUGS", 41, "CERRADO", "MEDIA"),
            t("BUGS", 42, "NO_REPRODUCIBLE", "ALTA"));

    @Test
    void reparteLoUrgenteLoListoParaElParcheYLoNormal() {
        Mantenimiento m = Mantenimiento.armar(6L, TICKETS, "v1.3.0", 5, List.of(), List.of(), true);

        // Crítica primero; el reportado sin severidad espera al Líder junto a lo urgente.
        assertThat(m.urgentes()).extracting(TicketDeMantenimiento::codigo)
                .containsExactly("BUGS-50", "BUGS-52", "BUGS-51");
        assertThat(m.listos()).extracting(TicketDeMantenimiento::codigo).containsExactly("BUGS-48");
        assertThat(m.normales()).extracting(TicketDeMantenimiento::codigo)
                .containsExactly("BUGS-45", "MEJR-12", "MEJR-20");
        assertThat(m.urgentesConfirmados()).isEqualTo(2);
        assertThat(m.criticos()).isEqualTo(1);
        assertThat(m.siguienteParche()).isEqualTo("v1.3.1");
    }

    @Test
    void elTiempoMedioDeHotfixEsEnHoras() {
        assertThat(Mantenimiento.armar(6L, TICKETS, null, null, List.of(300L, 420L), List.of(), false)
                .horasMedioHotfix()).isEqualTo(6.0);
        assertThat(Mantenimiento.armar(6L, TICKETS, null, null, List.of(), List.of(), false).horasMedioHotfix())
                .isNull();
    }
}
