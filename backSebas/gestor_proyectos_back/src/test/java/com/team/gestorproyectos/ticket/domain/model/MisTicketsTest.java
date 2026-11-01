package com.team.gestorproyectos.ticket.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.team.gestorproyectos.shared.domain.model.ItemDeProyecto;
import com.team.gestorproyectos.shared.domain.model.ItemTrabajo;
import com.team.gestorproyectos.shared.domain.model.Persona;

class MisTicketsTest {

    private static final Long JORGE = 1L; // Líder
    private static final Long ANA = 2L;   // Analista: prueba los hotfix
    private static final Long PEDRO = 3L; // Desarrollador
    private static final Long LUIS = 4L;  // Otro desarrollador
    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 7, 10, 12, 0);

    private static final Map<Long, Persona> PERSONAS = Map.of(
            JORGE, new Persona(JORGE, "Jorge"), ANA, new Persona(ANA, "Ana"),
            PEDRO, new Persona(PEDRO, "Pedro"), LUIS, new Persona(LUIS, "Luis"));

    private static FilaTicket ticket(String tipo, int numero, String estado, Long responsable, Long creadoPor,
                                     String ultimoEstadoAnterior) {
        var item = new ItemTrabajo((long) numero, tipo, numero, tipo + " " + numero, estado, "MEDIA",
                responsable, ANA, creadoPor, "EN_DESARROLLO", AHORA.minusDays(3), AHORA.minusDays(1));
        return new FilaTicket(new ItemDeProyecto(item, JORGE, ultimoEstadoAnterior), 10L, 2, 20L, 12, "PR");
    }

    @Test
    void elDesarrolladorVeLoSuyoYNadaAjeno() {
        var mios = MisTickets.armar(PEDRO, List.of(
                ticket("ACTV", 33, "EN_PROGRESO", PEDRO, JORGE, "POR_HACER"),
                ticket("ACTV", 40, "POR_HACER", LUIS, JORGE, null)), PERSONAS);

        assertThat(mios).hasSize(1);
        TicketMio actv33 = mios.get(0);
        assertThat(actv33.meToca()).isTrue();
        assertThat(actv33.asignado()).isTrue();
        assertThat(actv33.creadoPorMi()).isFalse();
        assertThat(actv33.accion()).isEqualTo("Enviar PR");
        assertThat(actv33.responsable().nombre()).isEqualTo("Pedro");
    }

    @Test
    void elLiderSigueTodoElProyectoPeroElBotonSoloSaleCuandoLeToca() {
        var mios = MisTickets.armar(JORGE, List.of(
                ticket("BUGS", 31, "REPORTADO", PEDRO, ANA, null),     // Líder confirma
                ticket("ACTV", 40, "POR_HACER", LUIS, ANA, null)),     // le toca a Luis
                PERSONAS);

        assertThat(mios).extracting(t -> t.fila().item().item().codigo()).containsExactly("BUGS-31", "ACTV-40");
        assertThat(mios).extracting(TicketMio::accion).containsExactly("Confirmar", null);
    }

    @Test
    void loRechazadoEnQaSePideCorregir() {
        var mios = MisTickets.armar(PEDRO, List.of(ticket("ACTV", 30, "EN_PROGRESO", PEDRO, JORGE, "EN_QA")), PERSONAS);

        assertThat(mios.get(0).rechazadaEnQa()).isTrue();
        assertThat(mios.get(0).accion()).isEqualTo("Corregir");
    }

    @Test
    void unaHistoriaListaLeTocaAlLiderCrearSusActividades() {
        var item = new ItemTrabajo(8L, "HIST", 8, "Pedidos", "LISTA", "ALTA", ANA, null, ANA, null,
                AHORA.minusDays(1), AHORA);
        var historia = new FilaTicket(new ItemDeProyecto(item, JORGE, null), 10L, 2, null, null, "AN");

        TicketMio delLider = MisTickets.armar(JORGE, List.of(historia), PERSONAS).get(0);
        assertThat(delLider.meToca()).isTrue();
        assertThat(delLider.accion()).isEqualTo("Crear actividades");
        assertThat(delLider.ejecutable()).isFalse();
        assertThat(MisTickets.armar(ANA, List.of(historia), PERSONAS).get(0).meToca()).isFalse();
    }
}
