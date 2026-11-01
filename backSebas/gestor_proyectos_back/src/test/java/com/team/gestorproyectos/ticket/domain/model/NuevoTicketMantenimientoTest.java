package com.team.gestorproyectos.ticket.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.team.gestorproyectos.shared.domain.exception.ReglaNegocioException;

class NuevoTicketMantenimientoTest {

    private static NuevoTicketMantenimiento ticket(TipoTicket tipo, String severidad, Long responsableId) {
        return new NuevoTicketMantenimiento(tipo, " Actualizar a Spring Boot 3.3 ", " ", 1L, null, severidad,
                responsableId);
    }

    @Test
    void soloMejorasYBugsMenores() {
        assertThatThrownBy(() -> ticket(TipoTicket.ACTV, null, 3L))
                .isInstanceOf(ReglaNegocioException.class).hasMessageContaining("Mantenimiento");
    }

    @Test
    void limpiaLoCapturadoYLaPrioridadEsMediaSiNoSeDice() {
        NuevoTicketMantenimiento t = ticket(TipoTicket.MEJR, "ALTA", 3L);
        assertThat(t.titulo()).isEqualTo("Actualizar a Spring Boot 3.3");
        assertThat(t.descripcion()).isNull();
        assertThat(t.prioridad()).isEqualTo("MEDIA");
        assertThat(t.severidad()).as("solo un bug lleva severidad").isNull();
    }

    @Test
    void unBugMenorEsMedioOBajoLoUrgenteVaPorReportarFalla() {
        assertThat(ticket(TipoTicket.BUGS, null, null).severidad()).isEqualTo("MEDIA");
        assertThat(ticket(TipoTicket.BUGS, "baja", null).severidad()).isEqualTo("BAJA");
        assertThatThrownBy(() -> ticket(TipoTicket.BUGS, "CRITICA", null))
                .isInstanceOf(ReglaNegocioException.class).hasMessageContaining("Reportar falla");
    }

    @Test
    void laMejoraNoPideAQuienSeAsigna() {
        assertThat(ticket(TipoTicket.MEJR, null, null).responsableId()).isNull();
    }
}
