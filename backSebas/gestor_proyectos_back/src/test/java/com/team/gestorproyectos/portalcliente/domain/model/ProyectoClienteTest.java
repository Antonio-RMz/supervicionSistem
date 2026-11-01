package com.team.gestorproyectos.portalcliente.domain.model;

import com.team.gestorproyectos.shared.domain.model.ConteoTickets;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

/** El dominio es Java puro: se prueba sin Spring ni base de datos. */
class ProyectoClienteTest {

    private static final Modulo API = new Modulo(1L, "portal-api", TipoModulo.API);
    private static final Modulo APP = new Modulo(2L, "portal-app", TipoModulo.MOVIL);

    private static Fase fase(long id, int numero, EstadoFase estado) {
        return new Fase(id, numero, "Fase " + numero, null, estado, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 2, 1), null);
    }

    @Test
    void faseEntregadaCuentaComo100YLaActualCuentaSusTicketsTerminados() {
        List<Fase> fases = List.of(fase(10, 1, EstadoFase.PUBLICADA), fase(20, 2, EstadoFase.EN_CURSO));
        List<ConteoTickets> conteos = List.of(
                new ConteoTickets(20L, 1L, "APROBADA", 3),
                new ConteoTickets(20L, 1L, "EN_PROGRESO", 1),
                new ConteoTickets(20L, 1L, "CANCELADA", 5)); // las canceladas no cuentan

        ProyectoCliente proyecto = ProyectoCliente.armar(
                new ProyectoBase(1L, "PC", "Portal", EstadoProyecto.EN_CURSO, List.of(API)), fases, conteos, 0);

        assertThat(proyecto.fases()).extracting(AvanceFase::porcentaje).containsExactly(100, 75);
        assertThat(proyecto.avance()).isEqualTo(88);
        assertThat(proyecto.faseActual()).get().extracting(a -> a.fase().numero()).isEqualTo(2);
    }

    @Test
    void soloAparecenLosModulosConTicketsEnLaFase() {
        List<ConteoTickets> conteos = List.of(
                new ConteoTickets(20L, 1L, "HECHA", 1),
                new ConteoTickets(20L, 1L, "POR_HACER", 1),
                new ConteoTickets(99L, 2L, "CERRADA", 4)); // de otra fase

        List<AvanceModulo> avances = AvanceModulo.calcular(List.of(API, APP), 20L, conteos);

        assertThat(avances).containsExactly(new AvanceModulo(API, 50));
    }

    @Test
    void elClienteVeEstadosSimples() {
        assertThat(EstadoFase.LISTA_PARA_RELEASE.paraCliente()).isEqualTo(EstadoFaseCliente.EN_CURSO);
        assertThat(EstadoHistoria.ACEPTADA.paraCliente()).isEqualTo(EstadoHistoriaCliente.EN_PRUEBAS);
        assertThat(new HistoriaCliente("Facturas", "cliente", "descargar mis facturas", EstadoHistoria.LISTA).descripcion())
                .isEqualTo("Como cliente, quiero descargar mis facturas.");
    }
}
