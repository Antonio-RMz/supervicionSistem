package com.team.gestorproyectos.proyecto.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.team.gestorproyectos.proyecto.domain.port.out.ProyectoConsultaPort.HistoriaBase;
import com.team.gestorproyectos.proyecto.domain.port.out.ProyectoConsultaPort.TicketBase;
import com.team.gestorproyectos.shared.domain.exception.ReglaNegocioException;
import com.team.gestorproyectos.shared.domain.model.ItemDeProyecto;
import com.team.gestorproyectos.shared.domain.model.ItemTrabajo;
import com.team.gestorproyectos.shared.domain.model.Persona;

class ProyectoDominioTest {

    @Test
    void elCodigoSonLasInicialesYNoSeRepite() {
        assertThat(CodigoProyecto.sugerir("Portal de Clientes", Set.of())).isEqualTo("PC");
        assertThat(CodigoProyecto.sugerir("Portal de Clientes", Set.of("PC"))).isEqualTo("PC2");
        assertThat(CodigoProyecto.sugerir("Logística", Set.of())).isEqualTo("LO");
        assertThat(CodigoProyecto.sugerir("App de Inventario", Set.of("AI", "AI2"))).isEqualTo("AI3");
    }

    @Test
    void elRitmoComparaAvanceContraTiempo() {
        LocalDate inicio = LocalDate.of(2026, 3, 1);
        LocalDate fin = LocalDate.of(2026, 3, 31);
        assertThat(Ritmo.de(inicio, fin, 50, LocalDate.of(2026, 3, 16))).isEqualTo(Ritmo.EN_TIEMPO);
        assertThat(Ritmo.de(inicio, fin, 10, LocalDate.of(2026, 3, 25))).isEqualTo(Ritmo.EN_RIESGO);
        assertThat(Ritmo.de(inicio, fin, 90, LocalDate.of(2026, 4, 2))).isEqualTo(Ritmo.ATRASADO);
        assertThat(Ritmo.de(null, fin, 0, LocalDate.of(2026, 3, 2))).isEqualTo(Ritmo.SIN_FECHAS);
    }

    @Test
    void elLiderSiempreQuedaEnElEquipoYLosModulosNoSeRepiten() {
        DatosProyecto datos = datos(List.of(new ModuloProyecto(null, TipoModulo.API, " portal-api ", null, null, " ")));
        assertThat(datos.miembroIds()).containsExactlyInAnyOrder(1L, 2L);
        assertThat(datos.modulos().get(0).ramaBase()).isEqualTo("develop");

        assertThatThrownBy(() -> datos(List.of(
                new ModuloProyecto(null, TipoModulo.API, "portal-api", null, null, null),
                new ModuloProyecto(null, TipoModulo.WEB, "Portal-API", null, null, null))))
                .isInstanceOf(ReglaNegocioException.class);
    }

    @Test
    void unProyectoInternoNoLlevaCliente() {
        DatosProyecto interno = new DatosProyecto("T-repor", null, "MEDIA", null,
                null, null, null, 1L, Set.of(), List.of(), reglas());

        assertThat(interno.clienteId()).isNull();
    }

    @Test
    void laEntregaNoPuedeSerAntesDelInicio() {
        assertThatThrownBy(() -> new DatosProyecto("Portal", 1L, "ALTA", null,
                LocalDate.of(2026, 5, 1), LocalDate.of(2026, 4, 1), null, 1L, Set.of(), List.of(), reglas()))
                .isInstanceOf(ReglaNegocioException.class);
    }

    @Test
    void elArbolCuentaListosYDiceQuienSigue() {
        Long jorge = 1L;
        Long ana = 2L;
        Long pedro = 3L;
        FaseResumen fase = new FaseResumen(10L, 2, "Desarrollo", "EN_CURSO", null, null, "v1.2.0", false,
                List.of(TipoModulo.API), 1, 3, 45);
        HistoriaBase hist = new HistoriaBase(20L, 10L, 12, "Login con JWT", "EN_DESARROLLO", ana, "AL");
        var arbol = ArbolProyecto.armar(pedro, List.of(fase), List.of(hist), List.of(
                ticket(30, "EN_REVISION", 20L, pedro, ana, jorge),
                ticket(35, "POR_HACER", 20L, pedro, ana, jorge),
                ticket(29, "CERRADA", 20L, pedro, ana, jorge),
                ticket(31, "REPORTADO", null, pedro, ana, jorge)),
                Map.of(ana, new Persona(ana, "Ana"), jorge, new Persona(jorge, "Jorge")));

        HistoriaArbol h = arbol.get(0).historias().get(0);
        assertThat(h.listos()).isEqualTo(1);
        assertThat(h.total()).isEqualTo(3);
        assertThat(h.tickets()).filteredOn(t -> t.codigo().equals("ACTV-30")).first()
                .satisfies(t -> assertThat(t.quien()).isEqualTo("Jorge · Líder"));
        assertThat(h.tickets()).filteredOn(t -> t.codigo().equals("ACTV-35")).first()
                .satisfies(t -> assertThat(t.accion()).isEqualTo("Iniciar"));
        assertThat(arbol.get(0).sueltos()).extracting(TicketArbol::codigo).containsExactly("ACTV-31");
    }

    private static TicketBase ticket(int numero, String estado, Long historiaId, Long responsable, Long tester,
                                     Long lider) {
        var item = new ItemTrabajo((long) numero, "ACTV", numero, "T" + numero, estado, "ALTA", responsable, tester,
                lider, "EN_DESARROLLO", LocalDateTime.now(), LocalDateTime.now());
        return new TicketBase(new ItemDeProyecto(item, lider, null), 10L, historiaId, TipoModulo.API, "PR");
    }

    private static DatosProyecto datos(List<ModuloProyecto> modulos) {
        return new DatosProyecto(" Portal ", 1L, "alta", null, null, null, " ",
                1L, Set.of(2L), modulos, reglas());
    }

    private static ReglasFlujo reglas() {
        return new ReglasFlujo(false, true, true, false);
    }
}
