package com.team.gestorproyectos.usuario.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.team.gestorproyectos.shared.domain.model.ItemDeProyecto;
import com.team.gestorproyectos.shared.domain.model.ItemTrabajo;
import com.team.gestorproyectos.shared.domain.model.Persona;

/** Las tres listas de "Trabajo actual" del detalle de Pedro (PDF pág. 7). */
class TrabajoActualTest {

    private static final Long JORGE = 1L; // Líder
    private static final Long ANA = 2L;   // Analista: prueba los hotfix
    private static final Long PEDRO = 3L; // Desarrollador
    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 7, 10, 12, 0);

    private static final Map<Long, Persona> PERSONAS = Map.of(
            JORGE, new Persona(JORGE, "Jorge"), ANA, new Persona(ANA, "Ana"), PEDRO, new Persona(PEDRO, "Pedro"));

    private static ItemDeProyecto ticket(String tipo, int numero, String estado, String ultimoEstadoAnterior) {
        var item = new ItemTrabajo((long) numero, tipo, numero, tipo + " " + numero, estado, "MEDIA",
                PEDRO, ANA, JORGE, "EN_DESARROLLO", AHORA.minusDays(3), AHORA.minusDays(1), true);
        return new ItemDeProyecto(item, JORGE, ultimoEstadoAnterior);
    }

    private static List<String> codigos(List<Tarea> tareas) {
        return tareas.stream().map(t -> t.pendiente().item().codigo()).toList();
    }

    @Test
    void reparteSusTicketsEntreLasTresListas() {
        var trabajo = TrabajoActual.armar(PEDRO, List.of(
                ticket("ACTV", 33, "EN_PROGRESO", "POR_HACER"),   // lo inició: trabajando ahora
                ticket("ACTV", 35, "POR_HACER", null),            // le toca iniciar
                ticket("ACTV", 30, "EN_PROGRESO", "EN_REVISION"), // el Líder pidió cambios: le toca corregir
                ticket("MEJR", 7, "EN_REVISION", "EN_PROGRESO"),  // espera al Líder (su PR)
                ticket("MEJR", 5, "PROPUESTA", null)),             // espera al Líder
                PERSONAS);

        assertThat(codigos(trabajo.trabajandoAhora())).containsExactly("ACTV-33");
        assertThat(codigos(trabajo.leToca())).containsExactly("ACTV-30", "ACTV-35");
        assertThat(codigos(trabajo.esperandoAOtros())).containsExactlyInAnyOrder("MEJR-07", "MEJR-05");
    }

    @Test
    void loRechazadoEnQaSePideCorregir() {
        var trabajo = TrabajoActual.armar(PEDRO, List.of(ticket("ACTV", 30, "EN_PROGRESO", "EN_QA")), PERSONAS);

        Tarea tarea = trabajo.leToca().get(0);
        assertThat(tarea.rechazadaEnQa()).isTrue();
        assertThat(tarea.pendiente().accion()).isEqualTo("Corregir");
    }

    @Test
    void esperandoAOtrosDiceQuienTieneLaAccion() {
        var trabajo = TrabajoActual.armar(PEDRO, List.of(
                ticket("BUGS", 7, "PUBLICADO", "APROBADO"),
                ticket("MEJR", 5, "PROPUESTA", null)), PERSONAS);

        assertThat(trabajo.esperandoAOtros())
                .extracting(t -> t.pendiente().actor().nombre() + " · " + t.pendiente().rolActor())
                .containsExactlyInAnyOrder("Ana · Analista", "Jorge · Líder");
    }

    @Test
    void elLiderVeLoQueLeTocaAunqueNoParticipe() {
        var trabajo = TrabajoActual.armar(JORGE, List.of(ticket("MEJR", 5, "PROPUESTA", null)), PERSONAS);

        assertThat(trabajo.leToca()).extracting(t -> t.pendiente().accion()).containsExactly("Aprobar mejora");
    }
}
