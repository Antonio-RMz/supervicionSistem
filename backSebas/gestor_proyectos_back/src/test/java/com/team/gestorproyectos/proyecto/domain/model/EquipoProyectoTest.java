package com.team.gestorproyectos.proyecto.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.team.gestorproyectos.proyecto.domain.model.EquipoProyecto.Carga;
import com.team.gestorproyectos.proyecto.domain.model.EquipoProyecto.Cobertura;
import com.team.gestorproyectos.proyecto.domain.model.EquipoProyecto.MiembroEquipo;
import com.team.gestorproyectos.proyecto.domain.model.EquipoProyecto.Persona;
import com.team.gestorproyectos.proyecto.domain.model.EquipoProyecto.Reasignacion;
import com.team.gestorproyectos.proyecto.domain.model.EquipoProyecto.Resultado;
import com.team.gestorproyectos.proyecto.domain.port.out.ProyectoConsultaPort.HistoriaBase;
import com.team.gestorproyectos.proyecto.domain.port.out.ProyectoConsultaPort.TicketBase;
import com.team.gestorproyectos.shared.domain.model.ItemDeProyecto;
import com.team.gestorproyectos.shared.domain.model.ItemTrabajo;

class EquipoProyectoTest {

    private static final Long JORGE = 1L; // Líder y Admin
    private static final Long ANA = 2L;   // Analista
    private static final Long PEDRO = 3L; // Desarrollador
    private static final Long LUIS = 4L;  // Desarrollador
    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 7, 10, 12, 0);

    private static final Persona P_JORGE = new Persona(JORGE, "Jorge", "Sebastián", List.of("ADMIN", "LIDER"), AHORA);
    private static final Persona P_ANA = new Persona(ANA, "Ana", "López Hernández", List.of("ANALISTA"), AHORA);
    private static final Persona P_PEDRO = new Persona(PEDRO, "Pedro", "Ramírez", List.of("DESARROLLADOR"), null);
    private static final Persona P_LUIS = new Persona(LUIS, "Luis", null, List.of("DESARROLLADOR"), null);

    private static TicketBase ticket(long id, String tipo, String estado, Long responsable, Long tester, TipoModulo modulo) {
        var item = new ItemTrabajo(id, tipo, (int) id, "Ticket " + id, estado, "MEDIA", responsable, tester, JORGE,
                "EN_DESARROLLO", AHORA, AHORA, true);
        return new TicketBase(new ItemDeProyecto(item, JORGE, null), 4L, 16L, modulo, null);
    }

    /** Pedro con 10 tickets abiertos (Ana prueba 4) y uno ya aprobado, que ya no cuenta. */
    private static List<TicketBase> tickets() {
        List<TicketBase> tickets = new ArrayList<>();
        for (long i = 1; i <= 8; i++) {
            tickets.add(ticket(i, "ACTV", "POR_HACER", PEDRO, i <= 3 ? ANA : null, i % 2 == 0 ? TipoModulo.API : TipoModulo.MOVIL));
        }
        tickets.add(ticket(9, "ACTV", "EN_REVISION", PEDRO, ANA, TipoModulo.API));
        tickets.add(ticket(10, "ACTV", "APROBADA", PEDRO, ANA, TipoModulo.WEB));
        tickets.add(ticket(11, "MEJR", "PROPUESTA", PEDRO, null, TipoModulo.API));
        return tickets;
    }

    private static final List<HistoriaBase> HISTORIAS = List.of(
            new HistoriaBase(16L, 4L, 16, "Consultar mis pedidos", "EN_VALIDACION", ANA, "AL"),
            new HistoriaBase(18L, 4L, 18, "Avisos", "BORRADOR", ANA, "AL"),
            new HistoriaBase(12L, 2L, 12, "Login", "CERRADA", ANA, "AL"));

    private static EquipoProyecto equipo(Persona... personas) {
        return EquipoProyecto.armar(JORGE, List.of(personas), tickets(), HISTORIAS);
    }

    private static MiembroEquipo miembro(EquipoProyecto equipo, Long id) {
        return equipo.miembros().stream().filter(m -> m.persona().id().equals(id)).findFirst().orElseThrow();
    }

    @Test
    void cadaMiembroLlevaSuCargaYLoQueLeToca() {
        EquipoProyecto equipo = equipo(P_PEDRO, P_ANA, P_JORGE);

        assertThat(equipo.miembros()).extracting(m -> m.persona().nombre()).containsExactly("Jorge", "Ana", "Pedro");
        MiembroEquipo pedro = miembro(equipo, PEDRO);
        assertThat(pedro.ticketsAbiertos()).isEqualTo(10);
        assertThat(pedro.carga()).isEqualTo(Carga.ALTA);
        assertThat(pedro.modulos()).containsExactly(TipoModulo.API, TipoModulo.MOVIL);
        assertThat(pedro.leToca()).isEqualTo(8); // iniciar sus 8 tickets por hacer

        MiembroEquipo ana = miembro(equipo, ANA);
        assertThat(ana.leToca()).isEqualTo(2); // sus dos historias (aceptar, marcar lista)
        assertThat(miembro(equipo, JORGE).leToca()).isEqualTo(2); // aprobar el PR y la mejora propuesta
        assertThat(miembro(equipo, JORGE).carga()).isEqualTo(Carga.BAJA);
    }

    @Test
    void laCoberturaAvisaSiHayUnSoloDesarrolladorYLaAlertaSugiereReasignar() {
        EquipoProyecto equipo = equipo(P_PEDRO, P_ANA, P_JORGE);

        assertThat(equipo.cobertura()).extracting(Cobertura::rol, Cobertura::resultado, Cobertura::quien).containsExactly(
                org.assertj.core.groups.Tuple.tuple("Líder", Resultado.OK, "Jorge"),
                org.assertj.core.groups.Tuple.tuple("Analista", Resultado.OK, "Ana"),
                org.assertj.core.groups.Tuple.tuple("Desarrollador", Resultado.AVISO, "Solo Pedro"));
        assertThat(equipo.alertas()).containsExactly(
                "Pedro tiene 10 tickets abiertos y es el único desarrollador. Si se ausenta, usa “Reasignar tickets” desde su menú.");

        EquipoProyecto conLuis = equipo(P_PEDRO, P_ANA, P_JORGE, P_LUIS);
        assertThat(conLuis.cobertura().get(2).quien()).isEqualTo("Pedro y Luis");
        assertThat(conLuis.alertas().get(0)).doesNotContain("único");
        assertThat(equipo(P_JORGE, P_PEDRO).cobertura().get(1).resultado()).isEqualTo(Resultado.FALTA);
    }

    @Test
    void noSeQuitaAlLiderNiAQuienTieneTrabajoAbierto() {
        EquipoProyecto equipo = equipo(P_PEDRO, P_ANA, P_JORGE, P_LUIS);

        assertThat(equipo.impedimentosParaQuitar(JORGE, tickets(), HISTORIAS)).first().asString().contains("Líder");
        assertThat(equipo.impedimentosParaQuitar(PEDRO, tickets(), HISTORIAS))
                .containsExactly("Tiene 10 tickets abiertos: reasígnalos primero");
        assertThat(equipo.impedimentosParaQuitar(ANA, tickets(), HISTORIAS)).contains("Es Analista de HU-0016, HU-0018");
        assertThat(equipo.impedimentosParaQuitar(LUIS, tickets(), HISTORIAS)).isEmpty();
    }

    @Test
    void reasignaSoloLosTicketsAbiertosAOtraPersonaDelEquipo() {
        EquipoProyecto equipo = equipo(P_PEDRO, P_ANA, P_JORGE, P_LUIS);

        Reasignacion r = equipo.reasignar(PEDRO, LUIS, null, tickets());
        assertThat(r.comoResponsable()).hasSize(10).doesNotContain(10L);
        assertThat(r.comoTester()).isEmpty();
        assertThat(equipo.reasignar(ANA, null, JORGE, tickets()).comoTester()).hasSize(4);

        assertThatThrownBy(() -> equipo.reasignar(PEDRO, PEDRO, null, tickets())).hasMessageContaining("otra persona");
        assertThatThrownBy(() -> equipo.reasignar(PEDRO, 99L, null, tickets())).hasMessageContaining("otra persona");
        assertThatThrownBy(() -> equipo.reasignar(LUIS, PEDRO, null, tickets())).hasMessageContaining("No hay tickets");
    }
}
