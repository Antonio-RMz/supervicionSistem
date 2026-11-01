package com.team.gestorproyectos.ticket.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

class TicketEditableTest {

    private static final Long JORGE = 1L; // Líder
    private static final Long ANA = 2L;   // lo creó
    private static final Long PEDRO = 3L; // Desarrollador

    private static TicketEditable ticket(String estado) {
        var datos = new NuevoTicket(10L, 20L, TipoTicket.ACTV, "Login", null, 5L, Set.of(), Set.of(), List.of(),
                Set.of(), PEDRO, null, "MEDIA", null, null);
        return new TicketEditable(30L, "ACTV-30", estado, ANA, JORGE, datos);
    }

    @Test
    void loEditanQuienLoCreoOElLiderMientrasNoEmpiece() {
        assertThat(ticket("POR_HACER").puedeEditar(ANA, false)).isTrue();
        assertThat(ticket("POR_HACER").puedeEditar(JORGE, false)).isTrue();
        assertThat(ticket("POR_HACER").puedeEditar(PEDRO, false)).isFalse();
        assertThat(ticket("POR_HACER").puedeEditar(PEDRO, true)).isTrue();
    }

    @Test
    void yaEmpezadoNadieLoEdita() {
        assertThat(ticket("EN_PROGRESO").puedeEditar(JORGE, false)).isFalse();
        assertThat(ticket("EN_PROGRESO").motivoSinEdicion()).isEqualTo("El ticket ya empezó: ya no se edita");
    }
}
