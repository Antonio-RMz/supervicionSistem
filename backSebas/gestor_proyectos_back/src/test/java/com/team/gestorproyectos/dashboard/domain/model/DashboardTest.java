package com.team.gestorproyectos.dashboard.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.team.gestorproyectos.shared.domain.model.ItemPendiente;
import com.team.gestorproyectos.shared.domain.model.ItemTrabajo;
import com.team.gestorproyectos.shared.domain.model.Movimiento;
import com.team.gestorproyectos.shared.domain.model.Persona;

class DashboardTest {

    private static final Long JORGE = 1L; // Líder
    private static final Long ANA = 2L;   // Analista
    private static final Long PEDRO = 3L; // Desarrollador
    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 7, 10, 12, 0);

    private static final ProyectoDashboard PROYECTO = new ProyectoDashboard(10L, "Portal de Clientes", JORGE);
    private static final Map<Long, Persona> PERSONAS = Map.of(
            JORGE, new Persona(JORGE, "Jorge"), ANA, new Persona(ANA, "Ana"), PEDRO, new Persona(PEDRO, "Pedro"));

    private static ItemTrabajo ticket(String tipo, int numero, String estado, String historiaEstado) {
        return new ItemTrabajo((long) numero, tipo, numero, tipo + " " + numero, estado, "ALTA",
                PEDRO, ANA, JORGE, historiaEstado, AHORA.minusDays(1), AHORA.minusDays(5));
    }

    private static Dashboard de(Long usuario, List<ItemTrabajo> items) {
        return Dashboard.armar(usuario, PROYECTO, items, PERSONAS, new ConteoCierres(0, 0), List.of(), List.of(), AHORA);
    }

    @Test
    void cadaAccionLeTocaASuRol() {
        List<ItemTrabajo> items = List.of(
                ticket("BUGS", 31, "REPORTADO", "EN_DESARROLLO"),   // Líder confirma
                ticket("ACTV", 30, "EN_REVISION", "EN_DESARROLLO"), // Líder revisa el PR
                ticket("ACTV", 35, "EN_PROGRESO", "EN_DESARROLLO")); // Desarrollador envía su PR

        assertThat(de(JORGE, items).teToca()).extracting(ItemPendiente::accion)
                .containsExactlyInAnyOrder("Confirmar", "Aprobar PR");
        assertThat(de(ANA, items).teToca()).isEmpty();
        assertThat(de(PEDRO, items).teToca()).extracting(ItemPendiente::accion).containsExactly("Enviar PR");

        // Jorge creó los tres: el de Pedro aparece como "Esperando a otros" con quién lo tiene
        assertThat(de(JORGE, items).esperandoAOtros())
                .extracting(p -> p.actor().nombre() + " · " + p.rolActor())
                .containsExactly("Pedro · Desarrollador");
    }

    @Test
    void noSePuedeIniciarUnTicketSiSuHistoriaNoHaEmpezado() {
        assertThat(de(PEDRO, List.of(ticket("ACTV", 41, "POR_HACER", "LISTA"))).teToca()).isEmpty();
        assertThat(de(PEDRO, List.of(ticket("ACTV", 41, "POR_HACER", "EN_DESARROLLO"))).teToca()).hasSize(1);
    }

    @Test
    void metricasCuentanSoloTickets() {
        ItemTrabajo historia = new ItemTrabajo(1L, "HIST", 16, "Pedidos", "EN_VALIDACION", "ALTA",
                ANA, null, ANA, null, AHORA, AHORA);
        Metricas m = de(JORGE, List.of(historia, ticket("ACTV", 30, "EN_QA", null), ticket("BUGS", 31, "REPORTADO", null)))
                .metricas();

        assertThat(m.ticketsAbiertos()).isEqualTo(2);
        assertThat(m.enQa()).isEqualTo(1);
        assertThat(m.enQaMasDe3Dias()).isEqualTo(1);
        assertThat(m.bugsPrioridadAlta()).isEqualTo(1);
    }

    @Test
    void elHistorialSeCuentaEnUnaFrase() {
        Movimiento rechazo = new Movimiento("Ana", "TICKET", "ACTV", 30, "Login", "EN_QA", "EN_PROGRESO", false, AHORA);
        Movimiento reporte = new Movimiento("Ana", "TICKET", "BUGS", 31, "Token", null, "REPORTADO", false, AHORA);
        Movimiento fase = new Movimiento("Jorge", "FASE", "FASE", 3, "Pruebas", null, "PLANEADA", false, AHORA);

        assertThat(rechazo.descripcion()).isEqualTo("Ana rechazó ACTV-30 en QA");
        assertThat(reporte.descripcion()).isEqualTo("Ana reportó BUGS-31");
        assertThat(fase.descripcion()).isEqualTo("Jorge creó la fase 3 · Pruebas");
    }
}
