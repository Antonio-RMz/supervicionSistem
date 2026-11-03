package com.team.gestorproyectos.ticket.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.team.gestorproyectos.shared.domain.exception.AccesoDenegadoException;
import com.team.gestorproyectos.shared.domain.exception.ReglaNegocioException;
import com.team.gestorproyectos.shared.domain.model.ItemTrabajo;

class TicketParaMoverTest {

    private static final Long JORGE = 1L; // Líder
    private static final Long ANA = 2L;   // Analista: prueba los hotfix
    private static final Long PEDRO = 3L; // Desarrollador
    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 7, 10, 12, 0);

    private static TicketParaMover ticket(String tipo, String estado, Long tester, List<String> dependencias,
                                          boolean autovalidacion) {
        return ticket(tipo, estado, tester, dependencias, autovalidacion, "EN_CURSO");
    }

    private static TicketParaMover ticket(String tipo, String estado, Long tester, List<String> dependencias,
                                          boolean autovalidacion, String faseEstado) {
        var item = new ItemTrabajo(35L, tipo, 35, "Recuperar contraseña por correo", estado, "ALTA",
                PEDRO, tester, JORGE, "EN_DESARROLLO", AHORA, AHORA, true);
        return new TicketParaMover(item, 10L, JORGE, "HU-0016", faseEstado, false, null, null, autovalidacion, true, true, dependencias);
    }

    private static TicketParaMover ticket(String tipo, String estado) {
        return ticket(tipo, estado, ANA, List.of(), false);
    }

    /** Una Actividad de un proyecto en la forma Normal que no pide PR. */
    private static TicketParaMover sinPr(String estado) {
        var item = new ItemTrabajo(35L, "ACTV", 35, "Recuperar contraseña por correo", estado, "ALTA",
                PEDRO, ANA, JORGE, "EN_DESARROLLO", AHORA, AHORA, false);
        return new TicketParaMover(item, 10L, JORGE, "HU-0016", "EN_CURSO", false, null, null, false, true, true,
                List.of());
    }

    @Test
    void laActividadNaceEnBorradorYElLiderLaMarcaLista() {
        // Aunque su historia aún no arranca, el Líder la puede marcar Lista; el Desarrollador todavía no la ve suya.
        assertThat(ticket("ACTV", "BORRADOR").opciones(PEDRO)).isEmpty();
        Transicion t = ticket("ACTV", "BORRADOR").mover(JORGE, TipoMovimiento.AVANZAR, "BORRADOR", null);
        assertThat(t.estadoNuevo()).isEqualTo("POR_HACER");
        assertThat(t.ramaNueva()).isNull();
    }

    @Test
    void iniciarCreaLaRama() {
        Transicion t = ticket("ACTV", "POR_HACER").mover(PEDRO, TipoMovimiento.AVANZAR, "POR_HACER", null);

        assertThat(t.estadoNuevo()).isEqualTo("EN_PROGRESO");
        assertThat(t.ramaNueva()).isEqualTo("feature/actv-35-recuperar-contrasena-por-correo");
        assertThat(ticket("BUGS", "CONFIRMADO").mover(PEDRO, TipoMovimiento.AVANZAR, "CONFIRMADO", null).ramaNueva())
                .startsWith("fix/bugs-35-");
    }

    @Test
    void soloQuienLeTocaPuedeMoverlo() {
        assertThatThrownBy(() -> ticket("ACTV", "EN_REVISION").mover(PEDRO, TipoMovimiento.AVANZAR, "EN_REVISION", null))
                .isInstanceOf(AccesoDenegadoException.class)
                .hasMessageContaining("Líder");
        assertThat(ticket("ACTV", "EN_REVISION").mover(JORGE, TipoMovimiento.AVANZAR, "EN_REVISION", null).estadoNuevo())
                .isEqualTo("APROBADA");
    }

    @Test
    void elDesarrolladorEnviaSuPrEnLugarDeMandarloAQa() {
        Transicion t = ticket("ACTV", "EN_PROGRESO").mover(PEDRO, TipoMovimiento.AVANZAR, "EN_PROGRESO", null);
        assertThat(t.accion()).isEqualTo("Enviar PR");
        assertThat(t.estadoNuevo()).isEqualTo("EN_REVISION");
        assertThat(ticket("MEJR", "EN_PROGRESO").mover(PEDRO, TipoMovimiento.AVANZAR, "EN_PROGRESO", null).estadoNuevo())
                .isEqualTo("EN_REVISION");
    }

    @Test
    void siAlguienMasYaLoMovioNoSePisa() {
        assertThatThrownBy(() -> ticket("ACTV", "EN_REVISION").mover(JORGE, TipoMovimiento.AVANZAR, "EN_PROGRESO", null))
                .isInstanceOf(ReglaNegocioException.class);
    }

    @Test
    void rechazarEnQaPideMotivoYRegresaElTrabajo() {
        // Pedir cambios al PR siempre pide motivo.
        assertThatThrownBy(() -> ticket("ACTV", "EN_REVISION").mover(JORGE, TipoMovimiento.RECHAZAR, "EN_REVISION", " "))
                .isInstanceOf(ReglaNegocioException.class).hasMessageContaining("PR");
        assertThat(ticket("ACTV", "EN_REVISION").mover(JORGE, TipoMovimiento.RECHAZAR, "EN_REVISION", "Falta la paginación")
                .estadoNuevo()).isEqualTo("EN_PROGRESO");

        Transicion t = ticket("BUGS", "EN_QA").mover(ANA, TipoMovimiento.RECHAZAR, "EN_QA", " Sigue fallando ");
        assertThat(t.estadoNuevo()).isEqualTo("EN_CORRECCION");
        assertThat(t.motivo()).isEqualTo("Sigue fallando");
    }

    @Test
    void noSeIniciaSiDependeDeAlgoAbierto() {
        assertThatThrownBy(() -> ticket("ACTV", "POR_HACER", ANA, List.of("ACTV-29"), false)
                .mover(PEDRO, TipoMovimiento.AVANZAR, "POR_HACER", null))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("ACTV-29");
    }

    @Test
    void nadieApruebaSuPropioPrSalvoQueElProyectoLoPermita() {
        assertThatThrownBy(() -> delLider(false).mover(JORGE, TipoMovimiento.AVANZAR, "EN_REVISION", null))
                .isInstanceOf(ReglaNegocioException.class).hasMessageContaining("su propio PR");
        assertThat(delLider(true).mover(JORGE, TipoMovimiento.AVANZAR, "EN_REVISION", null).estadoNuevo())
                .isEqualTo("APROBADA");
    }

    /** Una Actividad que hizo el propio Líder, con su PR en revisión. */
    private static TicketParaMover delLider(boolean autovalidacion) {
        var item = new ItemTrabajo(35L, "ACTV", 35, "Recuperar contraseña", "EN_REVISION", "ALTA",
                JORGE, null, JORGE, "EN_DESARROLLO", AHORA, AHORA, true);
        return new TicketParaMover(item, 10L, JORGE, "HU-0016", "EN_CURSO", false, null, null, autovalidacion, true,
                true, List.of());
    }

    @Test
    void lasAccionesConSuPropioProcesoNoSeHacenDesdeAqui() {
        assertThatThrownBy(() -> ticket("BUGS", "APROBADO").mover(JORGE, TipoMovimiento.AVANZAR, "APROBADO", null))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("Publicar parche");
    }

    @Test
    void conLaFasePausadaNadieIniciaTicketsPeroSiSigueLoEmpezado() {
        assertThatThrownBy(() -> ticket("ACTV", "POR_HACER", ANA, List.of(), false, "PAUSADA")
                .mover(PEDRO, TipoMovimiento.AVANZAR, "POR_HACER", null))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("pausada");
        assertThat(ticket("ACTV", "EN_PROGRESO", ANA, List.of(), false, "PAUSADA")
                .mover(PEDRO, TipoMovimiento.AVANZAR, "EN_PROGRESO", null).estadoNuevo()).isEqualTo("EN_REVISION");
    }

    @Test
    void cadaQuienVeSusBotones() {
        assertThat(ticket("ACTV", "EN_PROGRESO").opciones(PEDRO)).extracting(TicketParaMover.Opcion::tipo)
                .containsExactly(TipoMovimiento.AVANZAR, TipoMovimiento.BLOQUEAR);
        assertThat(ticket("ACTV", "EN_REVISION").opciones(JORGE)).extracting(TicketParaMover.Opcion::texto)
                .containsExactly("Aprobar PR", "Pedir cambios", "Cancelar ticket");
        assertThat(ticket("ACTV", "EN_REVISION").opciones(ANA)).isEmpty();
        assertThat(ticket("ACTV", "EN_REVISION").opciones(PEDRO)).extracting(TicketParaMover.Opcion::texto)
                .containsExactly("Retirar PR");
        assertThat(ticket("ACTV", "POR_HACER").opciones(JORGE)).extracting(TicketParaMover.Opcion::tipo)
                .containsExactly(TipoMovimiento.CANCELAR);
        assertThat(ticket("ACTV", "INTEGRADA").opciones(JORGE)).isEmpty();
    }

    @Test
    void bloquearPideMotivoYSoloMientrasSeTrabaja() {
        assertThatThrownBy(() -> ticket("ACTV", "EN_PROGRESO").mover(PEDRO, TipoMovimiento.BLOQUEAR, "EN_PROGRESO", ""))
                .hasMessageContaining("motivo");
        assertThatThrownBy(() -> ticket("ACTV", "POR_HACER").mover(PEDRO, TipoMovimiento.BLOQUEAR, "POR_HACER", "x"))
                .isInstanceOf(ReglaNegocioException.class);
        assertThatThrownBy(() -> ticket("ACTV", "EN_PROGRESO").mover(ANA, TipoMovimiento.BLOQUEAR, "EN_PROGRESO", "x"))
                .isInstanceOf(AccesoDenegadoException.class);

        Transicion t = ticket("ACTV", "EN_PROGRESO").mover(PEDRO, new DatosMovimiento(TipoMovimiento.BLOQUEAR,
                "EN_PROGRESO", "Esperamos credenciales", 107L, null, null, null));
        assertThat(t.estadoNuevo()).isEqualTo("BLOQUEADA");
        assertThat(t.bloqueadoPorId()).isEqualTo(107L);
        assertThat(ticket("BUGS", "BLOQUEADA").mover(PEDRO, TipoMovimiento.AVANZAR, "BLOQUEADA", null).estadoNuevo())
                .isEqualTo("EN_CORRECCION");
    }

    @Test
    void retirarElPrYCancelar() {
        assertThat(ticket("ACTV", "EN_REVISION").mover(PEDRO, TipoMovimiento.RETIRAR, "EN_REVISION", null).estadoNuevo())
                .isEqualTo("EN_PROGRESO");
        assertThatThrownBy(() -> ticket("ACTV", "EN_PROGRESO").mover(PEDRO, TipoMovimiento.CANCELAR, "EN_PROGRESO", "x"))
                .isInstanceOf(AccesoDenegadoException.class)
                .hasMessageContaining("Líder");
        assertThat(ticket("ACTV", "EN_PROGRESO").mover(JORGE, TipoMovimiento.CANCELAR, "EN_PROGRESO", "Se cubre con ACTV-55")
                .estadoNuevo()).isEqualTo("CANCELADA");
    }

    @Test
    void conLaHistoriaSinIniciarSeExplicaPorQueNo() {
        var item = new ItemTrabajo(35L, "ACTV", 35, "Recuperar contraseña", "POR_HACER", "ALTA",
                PEDRO, ANA, JORGE, "LISTA", AHORA, AHORA, true);
        var ticket = new TicketParaMover(item, 10L, JORGE, "HU-0016", "EN_CURSO", false, null, null, false, true, true, List.of());
        assertThat(ticket.opciones(PEDRO).get(0).impedimentos()).containsExactly("Su historia HU-0016 aún no se inicia");
        assertThatThrownBy(() -> ticket.mover(PEDRO, TipoMovimiento.AVANZAR, "POR_HACER", null))
                .hasMessageContaining("HU-0016");
    }

    /** Un bug de la fase Mantenimiento con la severidad dada. */
    private static TicketParaMover bug(String estado, String severidad, Long responsable) {
        var item = new ItemTrabajo(50L, "BUGS", 50, "Error 500 en facturas", estado, "ALTA",
                responsable, ANA, ANA, null, AHORA, AHORA, true);
        return new TicketParaMover(item, 10L, JORGE, null, "EN_CURSO", true, severidad, null, false, true, true,
                List.of());
    }

    @Test
    void confirmarPideSeveridadYDesarrollador() {
        assertThatThrownBy(() -> bug("REPORTADO", null, null).mover(JORGE, TipoMovimiento.AVANZAR, "REPORTADO", null))
                .hasMessageContaining("severidad");
        assertThatThrownBy(() -> bug("REPORTADO", null, null).mover(JORGE,
                new DatosMovimiento(TipoMovimiento.AVANZAR, "REPORTADO", null, null, null, "CRITICA", null)))
                .hasMessageContaining("Desarrollador");

        Transicion t = bug("REPORTADO", null, null).mover(JORGE,
                new DatosMovimiento(TipoMovimiento.AVANZAR, "REPORTADO", null, null, null, "critica", PEDRO));
        assertThat(t.estadoNuevo()).isEqualTo("CONFIRMADO");
        assertThat(t.severidad()).isEqualTo("CRITICA");
        assertThat(t.responsableId()).isEqualTo(PEDRO);
    }

    @Test
    void elHotfixPasaPorQaYElBugNormalPorPr() {
        Transicion hotfix = bug("EN_CORRECCION", "CRITICA", PEDRO).mover(PEDRO, TipoMovimiento.AVANZAR, "EN_CORRECCION", null);
        assertThat(hotfix.accion()).isEqualTo("Enviar a QA");
        assertThat(hotfix.estadoNuevo()).isEqualTo("EN_QA");
        assertThat(bug("EN_CORRECCION", "MEDIA", PEDRO).mover(PEDRO, TipoMovimiento.AVANZAR, "EN_CORRECCION", null)
                .estadoNuevo()).isEqualTo("EN_REVISION");
    }

    @Test
    void unHotfixSaleDeMainYSePublicaEnParche() {
        assertThat(bug("CONFIRMADO", "CRITICA", PEDRO).mover(PEDRO, TipoMovimiento.AVANZAR, "CONFIRMADO", null).ramaNueva())
                .startsWith("hotfix/bugs-50-");
        assertThat(bug("CONFIRMADO", "MEDIA", PEDRO).mover(PEDRO, TipoMovimiento.AVANZAR, "CONFIRMADO", null).ramaNueva())
                .startsWith("fix/bugs-50-");

        assertThat(bug("APROBADO", "ALTA", PEDRO).opciones(JORGE)).extracting(TicketParaMover.Opcion::tipo)
                .contains(TipoMovimiento.PUBLICAR_PARCHE);
        // Uno de severidad Media sale con su fase: no tiene botón de parche.
        assertThat(bug("APROBADO", "MEDIA", PEDRO).opciones(JORGE)).extracting(TicketParaMover.Opcion::tipo)
                .doesNotContain(TipoMovimiento.PUBLICAR_PARCHE);
        assertThat(Parche.siguienteVersion("v1.3.0")).isEqualTo("v1.3.1");
        assertThat(Parche.siguienteVersion("v1.3.1")).isEqualTo("v1.3.2");
        assertThat(Parche.siguienteVersion(null)).isEqualTo("v1.0.1");
    }

    @Test
    void enUnaCriticaElLiderApruebaSinAnalistaYQuedaMarcado() {
        assertThat(bug("EN_QA", "CRITICA", PEDRO).opciones(JORGE)).extracting(TicketParaMover.Opcion::texto)
                .contains("Aprobar QA sin Analista");
        Transicion t = bug("EN_QA", "CRITICA", PEDRO).mover(JORGE, TipoMovimiento.AVANZAR, "EN_QA", null);
        assertThat(t.estadoNuevo()).isEqualTo("APROBADO");
        assertThat(t.motivo()).isEqualTo(TicketParaMover.APROBADO_POR_LIDER);
        assertThatThrownBy(() -> bug("EN_QA", "ALTA", PEDRO).mover(JORGE, TipoMovimiento.AVANZAR, "EN_QA", null))
                .isInstanceOf(AccesoDenegadoException.class);
    }

    @Test
    void noReproducibleSigueFallandoYReabrir() {
        assertThatThrownBy(() -> bug("REPORTADO", null, null).mover(JORGE, TipoMovimiento.NO_REPRODUCIBLE, "REPORTADO", null))
                .hasMessageContaining("motivo");
        assertThat(bug("REPORTADO", null, null)
                .mover(JORGE, TipoMovimiento.NO_REPRODUCIBLE, "REPORTADO", "No pasa con el cliente 12").estadoNuevo())
                .isEqualTo("NO_REPRODUCIBLE");
        assertThat(bug("NO_REPRODUCIBLE", null, null).mover(JORGE, TipoMovimiento.REABRIR, "NO_REPRODUCIBLE", null)
                .estadoNuevo()).isEqualTo("REPORTADO");

        assertThatThrownBy(() -> bug("PUBLICADO", "CRITICA", PEDRO).mover(ANA, TipoMovimiento.RECHAZAR, "PUBLICADO", null))
                .hasMessageContaining("sigue fallando");
        assertThat(bug("PUBLICADO", "CRITICA", PEDRO)
                .mover(ANA, TipoMovimiento.RECHAZAR, "PUBLICADO", "Sigue el 500 con facturas de mayo").estadoNuevo())
                .isEqualTo("EN_CORRECCION");
        assertThat(bug("PUBLICADO", "CRITICA", PEDRO).mover(ANA, TipoMovimiento.AVANZAR, "PUBLICADO", null).estadoNuevo())
                .isEqualTo("CERRADO");
    }

    // ─── Mejoras (PDF pág. 30) ────────────────────────────────

    private static TicketParaMover mejora(String estado, Long responsable, Long tester) {
        var item = new ItemTrabajo(36L, "MEJR", 36, "Filtro por fecha en pedidos", estado, "ALTA",
                responsable, tester, JORGE, "EN_DESARROLLO", AHORA, AHORA, true);
        return new TicketParaMover(item, 10L, JORGE, "HU-0016", "EN_CURSO", false, null, null, false, false, true,
                List.of());
    }

    @Test
    void laMejoraSeApruebaConDesarrolladorOSeRechazaConMotivo() {
        TicketParaMover propuesta = mejora("PROPUESTA", null, ANA);
        assertThat(propuesta.opciones(JORGE)).extracting(TicketParaMover.Opcion::texto)
                .containsExactly("Aprobar mejora", "Rechazar", "Cancelar ticket");
        assertThatThrownBy(() -> propuesta.mover(JORGE, TipoMovimiento.AVANZAR, "PROPUESTA", null))
                .hasMessageContaining("Desarrollador");
        Transicion aprobada = propuesta.mover(JORGE, new DatosMovimiento(TipoMovimiento.AVANZAR, "PROPUESTA", null, null, null, null, PEDRO));
        assertThat(aprobada.estadoNuevo()).isEqualTo("APROBADA");
        assertThat(aprobada.responsableId()).isEqualTo(PEDRO);

        // Aunque el proyecto no pida comentario al rechazar en QA, rechazar una mejora siempre lo pide.
        assertThatThrownBy(() -> propuesta.mover(JORGE, TipoMovimiento.RECHAZAR, "PROPUESTA", null))
                .hasMessageContaining("por qué se rechaza");
        assertThat(propuesta.mover(JORGE, TipoMovimiento.RECHAZAR, "PROPUESTA", "No entra en esta fase").estadoNuevo())
                .isEqualTo("RECHAZADA");
    }

    @Test
    void sinPrElDesarrolladorTerminaLaActividad() {
        assertThat(sinPr("EN_PROGRESO").opciones(PEDRO)).extracting(TicketParaMover.Opcion::texto)
                .contains("Terminar").doesNotContain("Enviar PR");
        Transicion t = sinPr("EN_PROGRESO").mover(PEDRO, TipoMovimiento.AVANZAR, "EN_PROGRESO", null);
        assertThat(t.estadoNuevo()).isEqualTo("APROBADA");
        assertThat(t.accion()).isEqualTo("Terminar");
        assertThatThrownBy(() -> sinPr("EN_PROGRESO").mover(JORGE, TipoMovimiento.AVANZAR, "EN_PROGRESO", null))
                .isInstanceOf(AccesoDenegadoException.class);
        // Con PR sigue igual.
        assertThat(ticket("ACTV", "EN_PROGRESO").mover(PEDRO, TipoMovimiento.AVANZAR, "EN_PROGRESO", null)
                .estadoNuevo()).isEqualTo("EN_REVISION");
    }
}
