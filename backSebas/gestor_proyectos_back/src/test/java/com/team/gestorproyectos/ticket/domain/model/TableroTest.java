package com.team.gestorproyectos.ticket.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.team.gestorproyectos.shared.domain.model.ItemDeProyecto;
import com.team.gestorproyectos.shared.domain.model.ItemTrabajo;
import com.team.gestorproyectos.shared.domain.model.Persona;

class TableroTest {

    private static final Long JORGE = 1L; // Líder
    private static final Long ANA = 2L;   // Analista: prueba los hotfix
    private static final Long PEDRO = 3L; // Desarrollador
    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 7, 10, 12, 0);
    private static final Map<Long, Persona> PERSONAS = Map.of(PEDRO, new Persona(PEDRO, "Pedro"));

    private static FilaTicket ticket(String tipo, int numero, String estado) {
        var item = new ItemTrabajo((long) numero, tipo, numero, tipo + " " + numero, estado, "MEDIA",
                PEDRO, ANA, JORGE, "EN_DESARROLLO", AHORA, AHORA);
        return new FilaTicket(new ItemDeProyecto(item, JORGE, null), 10L, 2, null, null, "PR");
    }

    @Test
    void cadaEstadoCaeEnSuColumna() {
        var tarjetas = Tablero.armar(PEDRO, List.of(
                ticket("BUGS", 31, "REPORTADO"), ticket("MEJR", 7, "APROBADA"), ticket("ACTV", 29, "APROBADA"),
                ticket("MEJR", 5, "EN_QA"), ticket("BUGS", 4, "CERRADO"), ticket("ACTV", 9, "CANCELADA")),
                PERSONAS);

        assertThat(tarjetas).extracting(t -> t.fila().item().item().codigo() + ":" + t.columna())
                .containsExactlyInAnyOrder("BUGS-31:POR_HACER", "MEJR-07:POR_HACER", "ACTV-29:APROBADA",
                        "MEJR-05:EN_QA", "BUGS-04:CERRADA");
    }

    @Test
    void rechazarUnaMejoraNoSeHaceDesdeElTableroPorqueRechazadaNoTieneColumna() {
        Tarjeta propuesta = Tablero.armar(JORGE, List.of(ticket("MEJR", 8, "PROPUESTA")), PERSONAS).get(0);

        assertThat(propuesta.avance().columnaDestino()).isEqualTo(ColumnaTablero.POR_HACER);
        assertThat(propuesta.rechazo()).isNull();
    }

    @Test
    void laTarjetaDiceQuienPuedeMoverla() {
        Tarjeta enRevision = Tablero.armar(PEDRO, List.of(ticket("ACTV", 30, "EN_REVISION")), PERSONAS).get(0);
        assertThat(enRevision.avance()).isNull();
        assertThat(enRevision.bloqueadaPor()).isEqualTo("Líder");

        Tarjeta paraJorge = Tablero.armar(JORGE, List.of(ticket("ACTV", 30, "EN_REVISION")), PERSONAS).get(0);
        assertThat(paraJorge.avance().columnaDestino()).isEqualTo(ColumnaTablero.APROBADA);
        assertThat(paraJorge.rechazo().columnaDestino()).isEqualTo(ColumnaTablero.EN_PROGRESO);
        assertThat(paraJorge.bloqueadaPor()).isNull();
    }

    @Test
    void laFaseInicialEsLaEnCurso() {
        var fases = List.of(
                new FaseTablero(6L, 0, "Mantenimiento", "EN_CURSO", true),
                new FaseTablero(1L, 1, "Análisis", "PUBLICADA", false),
                new FaseTablero(2L, 2, "Desarrollo", "EN_CURSO", false),
                new FaseTablero(3L, 3, "Pruebas", "PLANEADA", false));
        assertThat(Tablero.faseInicial(fases)).map(FaseTablero::id).contains(2L);
    }
}
