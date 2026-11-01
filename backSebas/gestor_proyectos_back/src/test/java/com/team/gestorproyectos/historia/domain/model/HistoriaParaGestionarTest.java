package com.team.gestorproyectos.historia.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.team.gestorproyectos.historia.domain.model.HistoriaParaGestionar.FaseDestino;
import com.team.gestorproyectos.shared.domain.exception.AccesoDenegadoException;
import com.team.gestorproyectos.shared.domain.exception.ReglaNegocioException;
import com.team.gestorproyectos.shared.domain.model.AvanceHistoria;
import com.team.gestorproyectos.shared.domain.model.AvanceHistoria.TicketEstado;
import com.team.gestorproyectos.shared.domain.port.PermisosPort;

class HistoriaParaGestionarTest {

    private static final Long JORGE = 1L; // Líder
    private static final Long ANA = 2L;   // Analista y Tester
    private static final Long PEDRO = 3L; // Desarrollador

    private static final QuienGestiona LIDER = new QuienGestiona(JORGE, false, Set.of(
            PermisosPort.MARCAR_HISTORIA_LISTA, PermisosPort.INICIAR_HISTORIAS, PermisosPort.DESCARTAR_HISTORIAS, PermisosPort.INTEGRAR_HISTORIAS));
    private static final QuienGestiona ANALISTA = new QuienGestiona(ANA, false, Set.of(
            PermisosPort.MARCAR_HISTORIA_LISTA, PermisosPort.ACEPTAR_HISTORIAS, PermisosPort.DESCARTAR_HISTORIAS));
    private static final QuienGestiona DESARROLLADOR = new QuienGestiona(PEDRO, false, Set.of());

    private static TicketDeHistoria ticket(long id, String tipo, String estado) {
        return new TicketDeHistoria(id, tipo, (int) id, "Ticket " + id, estado, PEDRO, ANA);
    }

    private static HistoriaParaGestionar historia(String estado, List<TicketDeHistoria> tickets) {
        return historia(estado, "EN_CURSO", true, tickets);
    }

    private static HistoriaParaGestionar historia(String estado, String faseEstado, boolean completa,
                                                  List<TicketDeHistoria> tickets) {
        return new HistoriaParaGestionar(1016L, 1001L, 16, "Consultar mis pedidos", estado, ANA, JORGE, 1004L,
                faseEstado, completa, completa ? 4 : 0, 2, tickets, List.of(new FaseDestino(1005L, "Fase 5 · Pagos")));
    }

    private static DatosAccionHistoria datos(String estado) {
        return new DatosAccionHistoria(estado, null, null, false, null);
    }

    @Test
    void cadaDecisionLeTocaASuRol() {
        assertThat(historia("BORRADOR", List.of()).acciones(ANALISTA))
                .containsExactly(AccionHistoria.INICIAR_PLANEACION, AccionHistoria.DESCARTAR);
        assertThat(historia("BORRADOR", List.of()).acciones(LIDER))
                .containsExactly(AccionHistoria.INICIAR_PLANEACION, AccionHistoria.DESCARTAR, AccionHistoria.MOVER_DE_FASE);
        assertThat(historia("EN_PLANEACION", List.of()).acciones(LIDER))
                .containsExactly(AccionHistoria.MARCAR_LISTA, AccionHistoria.DESCARTAR, AccionHistoria.MOVER_DE_FASE);
        // En Lista el Líder crea sus Actividades: con la primera pasa sola a Lista para desarrollo.
        assertThat(historia("LISTA", List.of()).acciones(LIDER)).containsExactly(
                AccionHistoria.REGRESAR_A_PLANEACION, AccionHistoria.DESCARTAR, AccionHistoria.MOVER_DE_FASE);
        assertThat(historia("LISTA", List.of()).acciones(ANALISTA))
                .containsExactly(AccionHistoria.REGRESAR_A_PLANEACION, AccionHistoria.DESCARTAR);
        assertThat(historia("EN_VALIDACION", List.of()).acciones(ANALISTA))
                .containsExactly(AccionHistoria.ACEPTAR, AccionHistoria.DEVOLVER);
        assertThat(historia("ACEPTADA", List.of()).acciones(LIDER)).containsExactly(AccionHistoria.INTEGRAR);
        assertThat(historia("EN_DESARROLLO", List.of()).acciones(LIDER)).containsExactly(AccionHistoria.MOVER_DE_FASE);
        assertThat(historia("EN_VALIDACION", List.of()).acciones(DESARROLLADOR)).isEmpty();
        assertThatThrownBy(() -> historia("EN_VALIDACION", List.of())
                .ejecutar(LIDER, AccionHistoria.ACEPTAR, datos("EN_VALIDACION")))
                .isInstanceOf(AccesoDenegadoException.class)
                .hasMessageContaining("Analista");
    }

    @Test
    void seEditaMientrasNoTengaTickets() {
        assertThat(historia("BORRADOR", List.of()).puedeEditar(ANALISTA)).isTrue();
        assertThat(historia("LISTA", List.of()).puedeEditar(LIDER)).isTrue();
        assertThat(historia("BORRADOR", List.of()).puedeEditar(DESARROLLADOR)).isFalse();
        // Un ticket cancelado no cuenta; uno vivo, sí.
        assertThat(historia("LISTA", List.of(ticket(1, "ACTV", "CANCELADA"))).puedeEditar(LIDER)).isTrue();
        HistoriaParaGestionar conTicket = historia("LISTA", List.of(ticket(1, "ACTV", "POR_HACER")));
        assertThat(conTicket.puedeEditar(LIDER)).isFalse();
        assertThat(conTicket.motivoSinEdicion()).contains("ya tiene tickets");
        assertThat(historia("EN_DESARROLLO", List.of()).puedeEditar(LIDER)).isFalse();
    }

    @Test
    void dePlaneacionALista() {
        assertThat(historia("BORRADOR", List.of()).ejecutar(ANALISTA, AccionHistoria.INICIAR_PLANEACION,
                datos("BORRADOR")).estadoNuevo()).isEqualTo("EN_PLANEACION");
        HistoriaParaGestionar incompleta = historia("EN_PLANEACION", "EN_CURSO", false, List.of());
        assertThat(incompleta.impedimentos(AccionHistoria.MARCAR_LISTA))
                .contains("Falta completar Como / Quiero / Para", "Falta al menos un criterio de aceptación");
        // Lista ya no pide Actividades: esas las crea el Líder después.
        assertThat(historia("EN_PLANEACION", List.of()).ejecutar(ANALISTA, AccionHistoria.MARCAR_LISTA,
                datos("EN_PLANEACION")).estadoNuevo()).isEqualTo("LISTA");
    }

    @Test
    void cualquierAnalistaDelProyectoLaDefineYLaMarcaLista() {
        // Otro Analista del equipo, no el responsable de la historia.
        QuienGestiona otroAnalista = new QuienGestiona(5L, false, Set.of(PermisosPort.MARCAR_HISTORIA_LISTA));
        assertThat(historia("BORRADOR", List.of()).acciones(otroAnalista)).contains(AccionHistoria.INICIAR_PLANEACION);
        assertThat(historia("EN_PLANEACION", List.of()).acciones(otroAnalista)).containsExactly(AccionHistoria.MARCAR_LISTA);
        assertThat(historia("EN_PLANEACION", List.of()).puedeEditar(otroAnalista)).isTrue();
        // Ya Lista, la edita su responsable o el Líder; un Desarrollador nunca.
        assertThat(historia("LISTA", List.of()).puedeEditar(otroAnalista)).isFalse();
        assertThat(historia("EN_PLANEACION", List.of()).acciones(DESARROLLADOR)).isEmpty();
    }

    @Test
    void soloSeDescartaSiNingunTicketEmpezo() {
        HistoriaParaGestionar conTicketEmpezado = historia("LISTA",
                List.of(ticket(41, "ACTV", "EN_PROGRESO"), ticket(42, "ACTV", "POR_HACER")));
        assertThat(conTicketEmpezado.impedimentos(AccionHistoria.DESCARTAR)).containsExactly("Ya hay 1 ticket iniciado");

        HistoriaParaGestionar sinEmpezar = historia("BORRADOR",
                List.of(ticket(41, "ACTV", "POR_HACER"), ticket(8, "MEJR", "APROBADA")));
        assertThat(sinEmpezar.ticketsSinEmpezar()).hasSize(2);
        assertThatThrownBy(() -> sinEmpezar.ejecutar(ANALISTA, AccionHistoria.DESCARTAR, datos("BORRADOR")))
                .hasMessageContaining("motivo");
    }

    @Test
    void devolverReabreUnTicketAprobadoOCreaUnBug() {
        HistoriaParaGestionar enValidacion = historia("EN_VALIDACION",
                List.of(ticket(41, "ACTV", "APROBADA"), ticket(7, "MEJR", "APROBADA_QA"), ticket(31, "BUGS", "APROBADO")));

        CambioHistoria reabre = enValidacion.ejecutar(ANALISTA, AccionHistoria.DEVOLVER,
                new DatosAccionHistoria("EN_VALIDACION", "Falta el mensaje sin pedidos", 31L, false, null));
        assertThat(reabre.estadoNuevo()).isEqualTo("EN_DESARROLLO");
        assertThat(reabre.ticketAReabrir()).isEqualTo(31L);
        assertThat(reabre.estadoAlReabrir()).isEqualTo("EN_CORRECCION");

        CambioHistoria conBug = enValidacion.ejecutar(ANALISTA, AccionHistoria.DEVOLVER,
                new DatosAccionHistoria("EN_VALIDACION", "El filtro no guarda la selección", null, true, null));
        assertThat(conBug.bug().responsableId()).isEqualTo(PEDRO);
        // Sin Tester: el bug sigue el flujo de sus tickets y vuelve a validarse con la historia.
        assertThat(conBug.bug().testerId()).isNull();

        assertThatThrownBy(() -> enValidacion.ejecutar(ANALISTA, AccionHistoria.DEVOLVER,
                new DatosAccionHistoria("EN_VALIDACION", "x", 99L, false, null)))
                .isInstanceOf(ReglaNegocioException.class);
    }

    @Test
    void moverDeFaseSoloAUnaQueReciba() {
        HistoriaParaGestionar lista = historia("LISTA", List.of(ticket(41, "ACTV", "POR_HACER")));
        assertThat(lista.ejecutar(LIDER, AccionHistoria.MOVER_DE_FASE,
                new DatosAccionHistoria("LISTA", null, null, false, 1005L)).faseDestinoId()).isEqualTo(1005L);
        assertThatThrownBy(() -> lista.ejecutar(LIDER, AccionHistoria.MOVER_DE_FASE,
                new DatosAccionHistoria("LISTA", null, null, false, 1010L)))
                .hasMessageContaining("Elige una fase");
    }

    @Test
    void elAnalistaLaPruebaCompletaYLaAceptaODevuelve() {
        HistoriaParaGestionar enValidacion = historia("EN_VALIDACION", List.of(ticket(41, "ACTV", "APROBADA")));
        assertThat(enValidacion.actor()).isEqualTo("Analista");
        assertThat(enValidacion.ejecutar(ANALISTA, AccionHistoria.ACEPTAR, datos("EN_VALIDACION")).estadoNuevo())
                .isEqualTo("ACEPTADA");
        // Al devolver puede crear un bug para quien más tickets trabaja.
        CambioHistoria devuelta = enValidacion.ejecutar(ANALISTA, AccionHistoria.DEVOLVER,
                new DatosAccionHistoria("EN_VALIDACION", "El filtro por fecha no funciona", null, true, null));
        assertThat(devuelta.estadoNuevo()).isEqualTo("EN_DESARROLLO");
        assertThat(devuelta.bug().responsableId()).isEqualTo(PEDRO);
    }

    @Test
    void lasImagenesLasSubenSuAnalistaOElLiderHastaQueSeIntegra() {
        assertThat(historia("EN_DESARROLLO", List.of()).puedeAdjuntar(ANALISTA)).isTrue();
        assertThat(historia("BORRADOR", List.of()).puedeAdjuntar(LIDER)).isTrue();
        assertThat(historia("BORRADOR", List.of()).puedeAdjuntar(DESARROLLADOR)).isFalse();
        assertThat(historia("INTEGRADA", List.of()).puedeAdjuntar(ANALISTA)).isFalse();
        assertThat(historia("INTEGRADA", List.of()).motivoSinImagenes()).contains("ya no recibe");
    }

    @Test
    void enDesarrolloSeMueveConSusTicketsYSigueIgual() {
        HistoriaParaGestionar enDesarrollo = historia("EN_DESARROLLO", List.of(ticket(41, "ACTV", "EN_PROGRESO")));
        assertThat(enDesarrollo.impedimentos(AccionHistoria.MOVER_DE_FASE)).isEmpty();
        CambioHistoria cambio = enDesarrollo.ejecutar(LIDER, AccionHistoria.MOVER_DE_FASE,
                new DatosAccionHistoria("EN_DESARROLLO", null, null, false, 1005L));
        assertThat(cambio.faseDestinoId()).isEqualTo(1005L);
        assertThat(cambio.estadoNuevo()).isEqualTo("EN_DESARROLLO");
    }

    @Test
    void elAnalistaVerificaCriteriosEnDesarrolloOValidacion() {
        historia("EN_VALIDACION", List.of()).verificarCriterio(ANALISTA, 5L, List.of(5L, 6L));
        assertThatThrownBy(() -> historia("BORRADOR", List.of()).verificarCriterio(ANALISTA, 5L, List.of(5L)))
                .isInstanceOf(AccesoDenegadoException.class);
        assertThatThrownBy(() -> historia("EN_VALIDACION", List.of()).verificarCriterio(DESARROLLADOR, 5L, List.of(5L)))
                .isInstanceOf(AccesoDenegadoException.class);
    }

    @Test
    void laHistoriaAvanzaSolaSegunSusTickets() {
        assertThat(AvanceHistoria.siguienteEstado("EN_DESARROLLO",
                List.of(new TicketEstado("ACTV", "APROBADA"), new TicketEstado("MEJR", "APROBADA_QA"),
                        new TicketEstado("ACTV", "CANCELADA"))))
                .contains("EN_VALIDACION");
        assertThat(AvanceHistoria.siguienteEstado("EN_DESARROLLO",
                List.of(new TicketEstado("ACTV", "APROBADA"), new TicketEstado("MEJR", "APROBADA")))).isEmpty();
        assertThat(AvanceHistoria.siguienteEstado("EN_VALIDACION",
                List.of(new TicketEstado("ACTV", "EN_PROGRESO")))).contains("EN_DESARROLLO");
        assertThat(AvanceHistoria.siguienteEstado("LISTA", List.of(new TicketEstado("ACTV", "APROBADA")))).isEmpty();
    }

    @Test
    void seEliminaSoloSiNoSeHaIniciado() {
        assertThat(historia("BORRADOR", List.of()).impedimentosParaEliminar(0)).isEmpty();
        assertThat(historia("LISTA", List.of()).impedimentosParaEliminar(0)).isEmpty();
        assertThat(historia("EN_DESARROLLO", List.of()).impedimentosParaEliminar(0))
                .containsExactly("La historia ya se inició (en desarrollo): descártala en lugar de eliminarla");
        assertThat(historia("EN_PLANEACION", List.of(ticket(41, "ACTV", "CANCELADA"))).impedimentosParaEliminar(0))
                .containsExactly("Ya tiene 1 actividad o ticket");
        assertThat(historia("BORRADOR", List.of()).impedimentosParaEliminar(2))
                .containsExactly("Hay tickets de Mantenimiento que vienen de ella");
    }

    @Test
    void soloLaEliminanElLiderOElAdmin() {
        HistoriaParaGestionar borrador = historia("BORRADOR", List.of());
        assertThat(borrador.puedeEliminar(LIDER)).isTrue();
        assertThat(borrador.puedeEliminar(new QuienGestiona(99L, true, Set.of()))).isTrue();
        // Su Analista ya no: aunque pueda descartarla.
        assertThat(borrador.puedeEliminar(ANALISTA)).isFalse();
        assertThat(borrador.puedeEliminar(DESARROLLADOR)).isFalse();
        assertThatThrownBy(() -> borrador.verificarEliminar(ANALISTA, 0))
                .isInstanceOf(AccesoDenegadoException.class);
        assertThatThrownBy(() -> historia("EN_VALIDACION", List.of()).verificarEliminar(LIDER, 0))
                .isInstanceOf(ReglaNegocioException.class);
    }

    @Test
    void alMarcarlaListaConUnaActividadListaPasaSolaComoCuandoSeMarcaLaActividad() {
        // Estaba Lista para desarrollo, se regresó a planeación y se vuelve a marcar Lista.
        List<TicketDeHistoria> conActividadLista = List.of(ticket(41, "ACTV", "POR_HACER"));

        CambioHistoria faseSinIniciar = historia("EN_PLANEACION", "PLANEADA", true, conActividadLista)
                .ejecutar(ANALISTA, AccionHistoria.MARCAR_LISTA, datos("EN_PLANEACION"));
        assertThat(faseSinIniciar.estadoNuevo()).isEqualTo("LISTA");
        assertThat(faseSinIniciar.avanzaSolaA()).isEqualTo("LISTA_DESARROLLO");
        assertThat(faseSinIniciar.estadoFinal()).isEqualTo("LISTA_DESARROLLO");

        assertThat(historia("EN_PLANEACION", "EN_CURSO", true, conActividadLista)
                .ejecutar(ANALISTA, AccionHistoria.MARCAR_LISTA, datos("EN_PLANEACION")).estadoFinal())
                .isEqualTo("EN_DESARROLLO");
    }

    @Test
    void sinActividadListaSeQuedaEnLista() {
        for (List<TicketDeHistoria> tickets : List.of(List.<TicketDeHistoria>of(),
                List.of(ticket(41, "ACTV", "BORRADOR")), List.of(ticket(42, "ACTV", "CANCELADA")))) {
            CambioHistoria cambio = historia("EN_PLANEACION", "PLANEADA", true, tickets)
                    .ejecutar(ANALISTA, AccionHistoria.MARCAR_LISTA, datos("EN_PLANEACION"));
            assertThat(cambio.avanzaSolaA()).isNull();
            assertThat(cambio.estadoFinal()).isEqualTo("LISTA");
        }
    }
}
