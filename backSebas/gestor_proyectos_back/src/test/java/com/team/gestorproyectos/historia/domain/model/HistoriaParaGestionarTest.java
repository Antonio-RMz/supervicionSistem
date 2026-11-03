package com.team.gestorproyectos.historia.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.team.gestorproyectos.historia.domain.model.HistoriaParaGestionar.FaseDestino;
import com.team.gestorproyectos.shared.domain.exception.AccesoDenegadoException;
import com.team.gestorproyectos.shared.domain.exception.ReglaNegocioException;
import com.team.gestorproyectos.shared.domain.model.AvanceHistoria.TicketEstado;
import com.team.gestorproyectos.shared.domain.model.AvanceHistoria;
import com.team.gestorproyectos.shared.domain.model.FormaTrabajo;
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
        return historia(estado, faseEstado, completa, tickets, FormaTrabajo.GITFLOW);
    }

    private static HistoriaParaGestionar historia(String estado, String faseEstado, boolean completa,
                                                  List<TicketDeHistoria> tickets, FormaTrabajo forma) {
        return new HistoriaParaGestionar(1016L, 1001L, 16, "Consultar mis pedidos", estado, ANA, JORGE, 1004L,
                faseEstado, completa, completa ? 4 : 0, 2, tickets, List.of(new FaseDestino(1005L, "Fase 5 · Pagos")),
                forma, false, List.of(), 0);
    }

    /** Historia nueva: le llega directo a PEDRO y a MARCO (sin Actividades). */
    private static final Long MARCO = 4L;

    private static HistoriaParaGestionar conDesarrolladores(String estado, String faseEstado, boolean pedroTermino,
                                                           boolean requierePr, int trabajoPendiente) {
        return new HistoriaParaGestionar(1016L, 1001L, 16, "Consultar mis pedidos", estado, ANA, JORGE, 1004L,
                faseEstado, true, 4, 2, List.of(), List.of(), FormaTrabajo.NORMAL, requierePr,
                List.of(new DesarrolladorDeHistoria(PEDRO, "Pedro", null, null, pedroTermino),
                        new DesarrolladorDeHistoria(MARCO, "Marco", null, null, true)),
                trabajoPendiente);
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
        // Sin Desarrolladores (y sin Actividades de antes) no se puede marcar Lista.
        assertThat(historia("EN_PLANEACION", List.of()).impedimentos(AccionHistoria.MARCAR_LISTA))
                .containsExactly("Falta asignar al menos un Desarrollador");
        // Con sus Desarrolladores, queda Lista para desarrollo: ellos la inician.
        CambioHistoria lista = conDesarrolladores("EN_PLANEACION", "EN_CURSO", false, false, 0)
                .ejecutar(ANALISTA, AccionHistoria.MARCAR_LISTA, datos("EN_PLANEACION"));
        assertThat(lista.estadoNuevo()).isEqualTo("LISTA");
        assertThat(lista.estadoFinal()).isEqualTo("LISTA_DESARROLLO");
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
    void deUnaFaseTerminadaOEnReleaseNoSeSacanHistorias() {
        for (String faseEstado : List.of("LISTA_PARA_RELEASE", "EN_PUBLICACION", "PUBLICADA", "CANCELADA")) {
            HistoriaParaGestionar enValidacion = historia("EN_VALIDACION", faseEstado, true, List.of());
            assertThat(enValidacion.impedimentos(AccionHistoria.MOVER_DE_FASE)).containsExactly(
                    "Su fase ya terminó o está en release: sus historias ya no se mueven");
            assertThatThrownBy(() -> enValidacion.ejecutar(LIDER, AccionHistoria.MOVER_DE_FASE,
                    new DatosAccionHistoria("EN_VALIDACION", null, null, false, 1005L)))
                    .hasMessageContaining("ya no se mueven");
        }
        // Planeada, en curso o pausada: se mueve cuando sea.
        for (String faseEstado : List.of("PLANEADA", "EN_CURSO", "PAUSADA")) {
            assertThat(historia("BORRADOR", faseEstado, true, List.of()).impedimentos(AccionHistoria.MOVER_DE_FASE))
                    .isEmpty();
        }
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
    void enLaFormaNormalAceptarLaDejaIntegrada() {
        var enValidacion = historia("EN_VALIDACION", "EN_CURSO", true, List.of(ticket(41, "ACTV", "APROBADA")),
                FormaTrabajo.NORMAL);
        CambioHistoria cambio = enValidacion.ejecutar(ANALISTA, AccionHistoria.ACEPTAR, datos("EN_VALIDACION"));
        assertThat(cambio.estadoNuevo()).isEqualTo("ACEPTADA");
        assertThat(cambio.estadoFinal()).isEqualTo("INTEGRADA");
    }

    @Test
    void enLaFormaNormalNoSeIntegraADevelop() {
        var aceptada = historia("ACEPTADA", "EN_CURSO", true, List.of(), FormaTrabajo.NORMAL);
        assertThat(aceptada.acciones(LIDER)).doesNotContain(AccionHistoria.INTEGRAR);
        assertThatThrownBy(() -> aceptada.ejecutar(LIDER, AccionHistoria.INTEGRAR, datos("ACEPTADA")))
                .isInstanceOf(ReglaNegocioException.class);
        // En Gitflow sigue igual: el Líder integra.
        assertThat(historia("ACEPTADA", List.of()).acciones(LIDER)).contains(AccionHistoria.INTEGRAR);
        assertThat(historia("EN_VALIDACION", List.of(ticket(41, "ACTV", "APROBADA")))
                .ejecutar(ANALISTA, AccionHistoria.ACEPTAR, datos("EN_VALIDACION")).estadoFinal()).isEqualTo("ACEPTADA");
    }

    // ─── Historia con Desarrolladores (sin Actividades) ───

    @Test
    void susDesarrolladoresLaInicianCuandoSuFaseCorre() {
        var lista = conDesarrolladores("LISTA_DESARROLLO", "EN_CURSO", false, false, 0);
        assertThat(lista.actor()).isEqualTo("Desarrollador");
        assertThat(lista.acciones(DESARROLLADOR)).contains(AccionHistoria.INICIAR_DESARROLLO);
        assertThat(lista.acciones(ANALISTA)).doesNotContain(AccionHistoria.INICIAR_DESARROLLO);
        assertThat(lista.ejecutar(DESARROLLADOR, AccionHistoria.INICIAR_DESARROLLO, datos("LISTA_DESARROLLO"))
                .estadoNuevo()).isEqualTo("EN_DESARROLLO");
        // Con la fase sin iniciar, espera.
        assertThat(conDesarrolladores("LISTA_DESARROLLO", "PLANEADA", false, false, 0)
                .impedimentos(AccionHistoria.INICIAR_DESARROLLO)).containsExactly("Su fase aún no está en curso");
    }

    @Test
    void terminarPideQueTodosTerminenSuParteYLoQueHiceMarcado() {
        var falta = conDesarrolladores("EN_DESARROLLO", "EN_CURSO", false, false, 2);
        assertThat(falta.impedimentos(AccionHistoria.TERMINAR_DESARROLLO)).containsExactly(
                "Falta que Pedro marque su parte como terminada", "Hay 2 puntos de \"Lo que hice\" sin marcar");
        assertThatThrownBy(() -> falta.ejecutar(DESARROLLADOR, AccionHistoria.TERMINAR_DESARROLLO,
                datos("EN_DESARROLLO"))).isInstanceOf(ReglaNegocioException.class);

        // Normal sin PR: directo a validación con su Analista.
        assertThat(conDesarrolladores("EN_DESARROLLO", "EN_CURSO", true, false, 0)
                .ejecutar(DESARROLLADOR, AccionHistoria.TERMINAR_DESARROLLO, datos("EN_DESARROLLO")).estadoNuevo())
                .isEqualTo("EN_VALIDACION");
        // Con PR: primero la revisa el Líder.
        assertThat(conDesarrolladores("EN_DESARROLLO", "EN_CURSO", true, true, 0)
                .ejecutar(DESARROLLADOR, AccionHistoria.TERMINAR_DESARROLLO, datos("EN_DESARROLLO")).estadoNuevo())
                .isEqualTo("EN_REVISION");
    }

    @Test
    void elLiderApruebaLaRevisionOPideCambios() {
        var enRevision = conDesarrolladores("EN_REVISION", "EN_CURSO", true, true, 0);
        assertThat(enRevision.actor()).isEqualTo("Líder");
        assertThat(enRevision.acciones(LIDER)).contains(AccionHistoria.APROBAR_REVISION, AccionHistoria.PEDIR_CAMBIOS);
        assertThat(enRevision.acciones(DESARROLLADOR)).isEmpty();
        assertThat(enRevision.ejecutar(LIDER, AccionHistoria.APROBAR_REVISION, datos("EN_REVISION")).estadoNuevo())
                .isEqualTo("EN_VALIDACION");
        assertThatThrownBy(() -> enRevision.ejecutar(LIDER, AccionHistoria.PEDIR_CAMBIOS, datos("EN_REVISION")))
                .hasMessage("Escribe el motivo");
    }

    @Test
    void devolverlaNoPideTicketNiBug() {
        var enValidacion = conDesarrolladores("EN_VALIDACION", "EN_CURSO", true, false, 0);
        CambioHistoria cambio = enValidacion.ejecutar(ANALISTA, AccionHistoria.DEVOLVER,
                new DatosAccionHistoria("EN_VALIDACION", "El login no valida el CCT", null, false, null));
        assertThat(cambio.estadoNuevo()).isEqualTo("EN_DESARROLLO");
        assertThat(cambio.ticketAReabrir()).isNull();
        assertThat(cambio.bug()).isNull();
    }

    @Test
    void lasAccionesDeDesarrolladoresNoExistenEnHistoriasConActividades() {
        var conActividades = historia("EN_DESARROLLO", List.of(ticket(41, "ACTV", "EN_PROGRESO")));
        assertThat(conActividades.acciones(DESARROLLADOR)).isEmpty();
        assertThat(conActividades.trabajoDirecto()).isFalse();
    }

    @Test
    void soloSusDesarrolladoresMarcanSuParte() {
        var enDesarrollo = conDesarrolladores("EN_DESARROLLO", "EN_CURSO", false, false, 0);
        enDesarrollo.verificarMiParte(DESARROLLADOR);
        assertThatThrownBy(() -> enDesarrollo.verificarMiParte(ANALISTA)).isInstanceOf(AccesoDenegadoException.class);
        assertThat(enDesarrollo.puedeMarcarTrabajo(DESARROLLADOR)).isTrue();
        assertThat(enDesarrollo.puedeMarcarTrabajo(ANALISTA)).isFalse();
        assertThat(enDesarrollo.puedeAnotarTrabajo(ANALISTA)).isTrue();
    }
}
