package com.team.gestorproyectos.fase.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.team.gestorproyectos.fase.domain.model.FaseParaGestionar.BugReversion;
import com.team.gestorproyectos.shared.domain.exception.AccesoDenegadoException;
import com.team.gestorproyectos.shared.domain.exception.ReglaNegocioException;
import com.team.gestorproyectos.shared.domain.model.FormaTrabajo;
import com.team.gestorproyectos.shared.domain.port.PermisosPort;

class FaseParaGestionarTest {

    private static final Long JORGE = 1L; // Líder del proyecto
    private static final Long ANA = 2L;   // Analista
    private static final Long PEDRO = 3L; // Desarrollador
    private static final Long ADMIN = 9L;
    private static final LocalDate HOY = LocalDate.of(2026, 6, 20);

    private static final QuienGestiona LIDER = new QuienGestiona(JORGE, false, Set.of(
            PermisosPort.GESTIONAR_FASES, PermisosPort.MARCAR_CRITERIOS, PermisosPort.PUBLICAR_RELEASE,
            PermisosPort.REVERTIR_RELEASE));
    private static final QuienGestiona ANALISTA = new QuienGestiona(ANA, false, Set.of());
    private static final QuienGestiona EL_ADMIN = new QuienGestiona(ADMIN, true, Set.of(
            PermisosPort.GESTIONAR_FASES, PermisosPort.REABRIR_FASES, PermisosPort.MARCAR_CRITERIOS,
            PermisosPort.PUBLICAR_RELEASE, PermisosPort.REVERTIR_RELEASE));

    private static final CriterioCierre CUMPLIDO = new CriterioCierre(1L, "Sin bugs de prioridad alta abiertos",
            false, true, "Automático");
    private static final CriterioCierre VISTO_BUENO = new CriterioCierre(4L, "Visto bueno del cliente en la demo",
            true, false, null);

    private static FaseParaGestionar fase(String estado) {
        return fase(estado, List.of(CUMPLIDO), null, null);
    }

    private static FaseParaGestionar fase(String estado, List<CriterioCierre> criterios, String otraActiva,
                                          BugReversion bug) {
        return fase(estado, criterios, otraActiva, bug, FormaTrabajo.GITFLOW);
    }

    private static FaseParaGestionar fase(String estado, List<CriterioCierre> criterios, String otraActiva,
                                          BugReversion bug, FormaTrabajo forma) {
        return new FaseParaGestionar(1004L, 1001L, 4, "Portal móvil", estado, false, JORGE, JORGE, "v1.3.0",
                "v1.2.0", true, otraActiva, 3, 3, 0, criterios, bug, Set.of(JORGE, ANA, PEDRO), forma);
    }

    private static DatosAccionFase datos(String estado) {
        return new DatosAccionFase(estado, null, null, null, null);
    }

    @Test
    void cadaEstadoTieneSusBotonesYSoloLosVeElLider() {
        assertThat(fase("PLANEADA").acciones(LIDER)).containsExactly(AccionFase.INICIAR, AccionFase.CANCELAR);
        assertThat(fase("EN_CURSO").acciones(LIDER)).containsExactly(AccionFase.PAUSAR, AccionFase.CERRAR);
        assertThat(fase("PAUSADA").acciones(LIDER)).containsExactly(AccionFase.REANUDAR, AccionFase.CANCELAR);
        assertThat(fase("LISTA_PARA_RELEASE").acciones(LIDER)).containsExactly(AccionFase.SUBIR_A_MAIN);
        assertThat(fase("PUBLICADA").acciones(LIDER)).containsExactly(AccionFase.REVERTIR);
        assertThat(fase("EN_CURSO").acciones(ANALISTA)).isEmpty();
    }

    @Test
    void laFaseLaIniciaElLiderNoElAdmin() {
        assertThat(fase("PLANEADA").acciones(LIDER)).contains(AccionFase.INICIAR);
        assertThat(fase("PLANEADA").acciones(EL_ADMIN)).isEmpty();
        assertThatThrownBy(() -> fase("PLANEADA").ejecutar(EL_ADMIN, AccionFase.INICIAR, datos("PLANEADA"), HOY))
                .isInstanceOf(AccesoDenegadoException.class);
    }

    @Test
    void soloElAdminReabreUnaFaseCancelada() {
        assertThat(fase("CANCELADA").acciones(LIDER)).isEmpty();
        assertThat(fase("CANCELADA").acciones(EL_ADMIN)).containsExactly(AccionFase.REABRIR);
        assertThatThrownBy(() -> fase("CANCELADA").ejecutar(LIDER, AccionFase.REABRIR, datos("CANCELADA"), HOY))
                .isInstanceOf(AccesoDenegadoException.class)
                .hasMessageContaining("Admin");
        assertThat(fase("CANCELADA").ejecutar(EL_ADMIN, AccionFase.REABRIR, datos("CANCELADA"), HOY).estadoNuevo())
                .isEqualTo("PLANEADA");
    }

    @Test
    void unaFaseALaVez() {
        FaseParaGestionar planeada = fase("PLANEADA", List.of(), "Fase 4 · Portal móvil", null);
        assertThat(planeada.impedimentos(AccionFase.INICIAR)).containsExactly("Primero debe terminar la Fase 4 · Portal móvil");
        assertThatThrownBy(() -> planeada.ejecutar(LIDER, AccionFase.INICIAR, datos("PLANEADA"), HOY))
                .isInstanceOf(ReglaNegocioException.class);
    }

    @Test
    void pausarPideMotivoYUnaFechaFutura() {
        FaseParaGestionar enCurso = fase("EN_CURSO");
        assertThatThrownBy(() -> enCurso.ejecutar(LIDER, AccionFase.PAUSAR, datos("EN_CURSO"), HOY))
                .hasMessageContaining("motivo");
        assertThatThrownBy(() -> enCurso.ejecutar(LIDER, AccionFase.PAUSAR,
                new DatosAccionFase("EN_CURSO", "Textos legales", HOY, null, null), HOY))
                .hasMessageContaining("después de hoy");

        CambioFase cambio = enCurso.ejecutar(LIDER, AccionFase.PAUSAR,
                new DatosAccionFase("EN_CURSO", "  Esperamos los textos legales  ", HOY.plusDays(10), null, null), HOY);
        assertThat(cambio.estadoNuevo()).isEqualTo("PAUSADA");
        assertThat(cambio.motivo()).isEqualTo("Esperamos los textos legales");
        assertThat(cambio.reanudarEl()).isEqualTo(HOY.plusDays(10));
    }

    @Test
    void siOtroYaLaMovioNoSePisa() {
        assertThatThrownBy(() -> fase("PAUSADA").ejecutar(LIDER, AccionFase.REANUDAR, datos("EN_CURSO"), HOY))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("Recarga");
    }

    @Test
    void cerrarEsperaTodosLosCriterios() {
        FaseParaGestionar faltaUno = fase("EN_CURSO", List.of(CUMPLIDO, VISTO_BUENO), null, null);
        assertThat(faltaUno.impedimentos(AccionFase.CERRAR)).containsExactly("Para cerrar falta 1 criterio");
        assertThatThrownBy(() -> faltaUno.ejecutar(LIDER, AccionFase.CERRAR, datos("EN_CURSO"), HOY))
                .isInstanceOf(ReglaNegocioException.class);
        assertThat(fase("EN_CURSO").ejecutar(LIDER, AccionFase.CERRAR, datos("EN_CURSO"), HOY).estadoNuevo())
                .isEqualTo("LISTA_PARA_RELEASE");
    }

    @Test
    void elUltimoCriterioManualDejaLaFaseLista() {
        FaseParaGestionar faltaUno = fase("EN_CURSO", List.of(CUMPLIDO, VISTO_BUENO), null, null);
        assertThat(faltaUno.marcarCriterio(LIDER, 4L)).isTrue();
        assertThat(fase("PAUSADA", List.of(CUMPLIDO, VISTO_BUENO), null, null).marcarCriterio(LIDER, 4L)).isFalse();
        assertThatThrownBy(() -> faltaUno.marcarCriterio(LIDER, 1L)).hasMessageContaining("sistema");
        assertThatThrownBy(() -> faltaUno.marcarCriterio(ANALISTA, 4L)).isInstanceOf(AccesoDenegadoException.class);
    }

    @Test
    void subirAMainPideEscribirLaVersion() {
        FaseParaGestionar lista = fase("LISTA_PARA_RELEASE");
        assertThatThrownBy(() -> lista.ejecutar(LIDER, AccionFase.SUBIR_A_MAIN,
                new DatosAccionFase("LISTA_PARA_RELEASE", null, null, "v1.3", null), HOY))
                .hasMessageContaining("Escribe v1.3.0");
        assertThat(lista.ejecutar(LIDER, AccionFase.SUBIR_A_MAIN,
                new DatosAccionFase("LISTA_PARA_RELEASE", null, null, " v1.3.0 ", null), HOY).estadoNuevo())
                .isEqualTo("EN_PUBLICACION");
    }

    @Test
    void revertirPideQueFallaYAQuienAsignarElBug() {
        FaseParaGestionar publicada = fase("PUBLICADA");
        assertThatThrownBy(() -> publicada.ejecutar(LIDER, AccionFase.REVERTIR,
                new DatosAccionFase("PUBLICADA", null, null, null, PEDRO), HOY))
                .hasMessageContaining("qué falló");
        assertThatThrownBy(() -> publicada.ejecutar(LIDER, AccionFase.REVERTIR,
                new DatosAccionFase("PUBLICADA", "Error 500", null, null, 77L), HOY))
                .hasMessageContaining("equipo");

        CambioFase cambio = publicada.ejecutar(LIDER, AccionFase.REVERTIR,
                new DatosAccionFase("PUBLICADA", "Error 500 al descargar facturas", null, null, PEDRO), HOY);
        assertThat(cambio.estadoNuevo()).isEqualTo("REVERTIDA");
        assertThat(cambio.asignarBugA()).isEqualTo(PEDRO);
    }

    @Test
    void publicarDeNuevoEsperaAQueSeResuelvaElBug() {
        FaseParaGestionar abierta = fase("REVERTIDA", List.of(), null, new BugReversion(50L, "BUGS-50", true));
        assertThat(abierta.impedimentos(AccionFase.PUBLICAR_DE_NUEVO)).containsExactly("BUGS-50 sigue abierto");
        FaseParaGestionar resuelta = fase("REVERTIDA", List.of(), null, new BugReversion(50L, "BUGS-50", false));
        assertThat(resuelta.ejecutar(LIDER, AccionFase.PUBLICAR_DE_NUEVO, datos("REVERTIDA"), HOY).estadoNuevo())
                .isEqualTo("EN_PUBLICACION");
    }

    @Test
    void sinHistoriasNoSeInicia() {
        var vacia = new FaseParaGestionar(1007L, 1001L, 6, "Pagos", "PLANEADA", false, JORGE, JORGE, "v1.5.0",
                "v1.2.0", false, null, 0, 0, 0, List.of(), null, Set.of(JORGE), FormaTrabajo.GITFLOW);
        assertThat(vacia.impedimentos(AccionFase.INICIAR)).containsExactly(
                "Agrega al menos una historia de usuario antes de iniciarla");
        assertThatThrownBy(() -> vacia.ejecutar(LIDER, AccionFase.INICIAR, datos("PLANEADA"), HOY))
                .isInstanceOf(ReglaNegocioException.class);
        assertThat(fase("PLANEADA").impedimentos(AccionFase.INICIAR)).isEmpty();
    }

    @Test
    void conUnaHistoriaListaParaDesarrolloYaSeInicia() {
        var planeandose = new FaseParaGestionar(1007L, 1001L, 6, "Pagos", "PLANEADA", false, JORGE, JORGE, "v1.5.0",
                "v1.2.0", false, null, 2, 0, 0, List.of(), null, Set.of(JORGE), FormaTrabajo.GITFLOW);
        assertThat(planeandose.impedimentos(AccionFase.INICIAR))
                .containsExactly("Al menos una historia debe estar Lista para desarrollo (con sus Actividades)");
        assertThatThrownBy(() -> planeandose.ejecutar(LIDER, AccionFase.INICIAR, datos("PLANEADA"), HOY))
                .isInstanceOf(ReglaNegocioException.class);

        var unaLista = new FaseParaGestionar(1007L, 1001L, 6, "Pagos", "PLANEADA", false, JORGE, JORGE, "v1.5.0",
                "v1.2.0", false, null, 5, 1, 0, List.of(), null, Set.of(JORGE), FormaTrabajo.GITFLOW);
        assertThat(unaLista.impedimentos(AccionFase.INICIAR)).isEmpty();
    }

    @Test
    void laFaseSeEditaHastaElRelease() {
        assertThat(fase("PLANEADA").puedeEditar(LIDER)).isTrue();
        assertThat(fase("PAUSADA").puedeEditar(LIDER)).isTrue();
        assertThat(fase("PLANEADA").puedeEditar(ANALISTA)).isFalse();
        assertThat(fase("LISTA_PARA_RELEASE").puedeEditar(LIDER)).isFalse();
        assertThat(fase("PUBLICADA").motivoSinEdicion()).contains("ya no se editan");
        var conTickets = new FaseParaGestionar(1004L, 1001L, 4, "Portal móvil", "EN_CURSO", false, JORGE, JORGE,
                "v1.3.0", "v1.2.0", true, null, 3, 0, 12, List.of(), null, Set.of(JORGE), FormaTrabajo.GITFLOW);
        assertThat(conTickets.puedeEditar(LIDER)).isFalse();
        assertThat(conTickets.motivoSinEdicion()).contains("ya tiene tickets");
    }

    @Test
    void mantenimientoNoCambiaDeEstado() {
        var mantenimiento = new FaseParaGestionar(1006L, 1001L, 0, "Mantenimiento", "EN_CURSO", true, JORGE, JORGE,
                null, null, false, null, 0, 0, 0, List.of(), null, Set.of(JORGE), FormaTrabajo.GITFLOW);
        assertThat(mantenimiento.acciones(LIDER)).isEmpty();
        assertThatThrownBy(() -> mantenimiento.ejecutar(LIDER, AccionFase.PAUSAR, datos("EN_CURSO"), HOY))
                .hasMessageContaining("permanente");
    }

    @Test
    void criteriosAutomaticosSeCalculan() {
        var conteos = new CriterioCierre.Conteos(3, 1, 0);
        CriterioCierre historias = CriterioCierre.evaluar(1L, "Todas las historias aceptadas", "HISTORIAS_ACEPTADAS",
                false, null, null, conteos);
        assertThat(historias.cumplido()).isFalse();
        assertThat(historias.detalle()).isEqualTo("1 de 3 aceptadas");
        assertThat(CriterioCierre.evaluar(2L, "Sin bugs", "SIN_BUGS_ALTA", false, null, null, conteos).cumplido()).isTrue();
        assertThat(CriterioCierre.evaluar(4L, "Visto bueno", null, false, null, null, conteos).manual()).isTrue();
    }

    @Test
    void ticketsPorEstadoJuntaLosTipos() {
        assertThat(GrupoTickets.de("ACTV", "APROBADA")).isEqualTo(GrupoTickets.APROBADOS);
        assertThat(GrupoTickets.de("MEJR", "APROBADA")).isEqualTo(GrupoTickets.POR_HACER);
        assertThat(GrupoTickets.de("MEJR", "EN_QA")).isEqualTo(GrupoTickets.EN_QA);
        assertThat(GrupoTickets.de("BUGS", "PUBLICADO")).isEqualTo(GrupoTickets.CERRADOS);
        assertThat(GrupoTickets.de("ACTV", "CANCELADA")).isNull();
    }

    @Test
    void lasNotasLasEditaQuienPublicaMientrasNoSePublique() {
        assertThat(fase("EN_CURSO").puedeEditarNotas(LIDER)).isTrue();
        assertThat(fase("EN_CURSO").puedeEditarNotas(ANALISTA)).isFalse();
        assertThat(fase("PUBLICADA").puedeEditarNotas(LIDER)).isFalse();
        assertThat(fase("EN_CURSO").notasParaGuardar(LIDER, "  Nuevo\n- Consulta de pedidos  ")).isEqualTo("Nuevo\n- Consulta de pedidos");
        assertThatThrownBy(() -> fase("EN_CURSO").notasParaGuardar(LIDER, " ")).hasMessageContaining("notas");
    }

    @Test
    void lasNotasSeArmanConLosTickets() {
        String notas = NotasRelease.sugeridas(List.of(
                new NotasRelease.Cambio("BUGS", "BUGS-31", "El token JWT expiraba antes de tiempo"),
                new NotasRelease.Cambio("ACTV", "ACTV-41", "Consulta de pedidos con filtro por estado"),
                new NotasRelease.Cambio("MEJR", "MEJR-08", "Exportar reporte a PDF")));
        assertThat(notas).isEqualTo("""
                Nuevo
                - Consulta de pedidos con filtro por estado

                Mejorado
                - Exportar reporte a PDF

                Corregido
                - El token JWT expiraba antes de tiempo""");
    }

    @Test
    void soloElLiderOElAdminEliminanYSoloSiNadaEmpezo() {
        FaseParaGestionar planeada = fase("PLANEADA");
        BloqueosEliminarFase nada = new BloqueosEliminarFase(0, 0, 0);
        assertThat(planeada.puedeEliminar(LIDER)).isTrue();
        assertThat(planeada.puedeEliminar(EL_ADMIN)).isTrue();
        assertThat(planeada.puedeEliminar(ANALISTA)).isFalse();
        assertThat(planeada.impedimentosParaEliminar(nada)).isEmpty();
        assertThatThrownBy(() -> planeada.verificarEliminar(ANALISTA, nada)).isInstanceOf(AccesoDenegadoException.class);

        assertThat(fase("EN_CURSO").impedimentosParaEliminar(nada))
                .containsExactly("La fase ya está en curso: cancélala en lugar de eliminarla");
        assertThat(planeada.impedimentosParaEliminar(new BloqueosEliminarFase(1, 2, 1))).containsExactly(
                "1 historia ya se inició", "Ya tiene 2 tickets", "Tiene solicitudes, demos o releases");
        assertThatThrownBy(() -> planeada.verificarEliminar(LIDER, new BloqueosEliminarFase(0, 1, 0)))
                .isInstanceOf(ReglaNegocioException.class).hasMessage("Ya tiene 1 ticket");
    }

    @Test
    void mantenimientoNoSeElimina() {
        var mantenimiento = new FaseParaGestionar(1008L, 1001L, 0, "Mantenimiento", "EN_CURSO", true, JORGE, JORGE,
                null, null, false, null, 0, 0, 0, List.of(), null, Set.of(JORGE), FormaTrabajo.GITFLOW);
        assertThat(mantenimiento.puedeEliminar(EL_ADMIN)).isFalse();
    }

    @Test
    void enLaFormaNormalLaFaseTambienSubeAMain() {
        var normal = fase("LISTA_PARA_RELEASE", List.of(CUMPLIDO), null, null, FormaTrabajo.NORMAL);
        assertThat(normal.acciones(LIDER)).containsExactly(AccionFase.SUBIR_A_MAIN);
        assertThat(normal.textoDe(AccionFase.SUBIR_A_MAIN)).isEqualTo("Subir a main");
        assertThat(fase("LISTA_PARA_RELEASE").textoDe(AccionFase.SUBIR_A_MAIN)).isEqualTo("Subir a main");
    }
}
