package com.team.gestorproyectos.shared.domain.model;

import static com.team.gestorproyectos.shared.domain.model.Actor.LIDER;
import static com.team.gestorproyectos.shared.domain.model.Actor.RESPONSABLE;
import static com.team.gestorproyectos.shared.domain.model.Actor.TESTER;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * La acción principal de cada tipo de ticket en cada estado (PDF: "Acciones por estado" y
 * "Otros tipos de ticket · Flujos"), y el rechazo de quien revisa (QA o revisión), que regresa
 * el trabajo a quien lo hizo. Bloquear, Cancelar y demás viven en el detalle del ticket.
 */
public final class FlujoTrabajo {

    private static final Map<String, AccionPendiente> ACCIONES = Map.ofEntries(
            // Historia de usuario
            Map.entry("HIST:BORRADOR", new AccionPendiente("Iniciar planeación", RESPONSABLE, null)),
            Map.entry("HIST:EN_PLANEACION", new AccionPendiente("Marcar como Lista", RESPONSABLE, null)),
            Map.entry("HIST:EN_VALIDACION", new AccionPendiente("Aceptar", RESPONSABLE, null)),
            // Historia con Desarrolladores: el Líder revisa el código antes de que la valide su Analista.
            Map.entry("HIST:EN_REVISION", new AccionPendiente("Aprobar revisión", LIDER, null)),
            Map.entry("HIST:ACEPTADA", new AccionPendiente("Integrar", LIDER, null)),
            // Actividad: el Líder revisa su PR; QA se hace a la historia completa.
            Map.entry("ACTV:BORRADOR", new AccionPendiente("Marcar como Lista", LIDER, "POR_HACER")),
            Map.entry("ACTV:POR_HACER", new AccionPendiente("Iniciar", RESPONSABLE, "EN_PROGRESO")),
            Map.entry("ACTV:EN_PROGRESO", new AccionPendiente("Enviar PR", RESPONSABLE, "EN_REVISION")),
            Map.entry("ACTV:BLOQUEADA", new AccionPendiente("Desbloquear", RESPONSABLE, "EN_PROGRESO")),
            Map.entry("ACTV:EN_REVISION", new AccionPendiente("Aprobar PR", LIDER, "APROBADA")),
            // Bug: como una Actividad; el hotfix va a QA en lugar de PR (TicketParaMover).
            Map.entry("BUGS:REPORTADO", new AccionPendiente("Confirmar", LIDER, "CONFIRMADO")),
            Map.entry("BUGS:CONFIRMADO", new AccionPendiente("Iniciar corrección", RESPONSABLE, "EN_CORRECCION")),
            Map.entry("BUGS:EN_CORRECCION", new AccionPendiente("Enviar PR", RESPONSABLE, "EN_REVISION")),
            Map.entry("BUGS:BLOQUEADA", new AccionPendiente("Desbloquear", RESPONSABLE, "EN_CORRECCION")),
            Map.entry("BUGS:EN_REVISION", new AccionPendiente("Aprobar PR", LIDER, "APROBADO")),
            Map.entry("BUGS:EN_QA", new AccionPendiente("Aprobar QA", TESTER, "APROBADO")),
            Map.entry("BUGS:APROBADO", new AccionPendiente("Publicar parche", LIDER, null)),
            Map.entry("BUGS:PUBLICADO", new AccionPendiente("Verificar", TESTER, "CERRADO")),
            // Mejora
            Map.entry("MEJR:PROPUESTA", new AccionPendiente("Aprobar mejora", LIDER, "APROBADA")),
            Map.entry("MEJR:APROBADA", new AccionPendiente("Iniciar", RESPONSABLE, "EN_PROGRESO")),
            Map.entry("MEJR:EN_PROGRESO", new AccionPendiente("Enviar PR", RESPONSABLE, "EN_REVISION")),
            Map.entry("MEJR:BLOQUEADA", new AccionPendiente("Desbloquear", RESPONSABLE, "EN_PROGRESO")),
            Map.entry("MEJR:EN_REVISION", new AccionPendiente("Aprobar PR", LIDER, "APROBADA_QA")));

    /**
     * Quien revisa puede regresar el trabajo; lo hace el mismo actor que aprobaría. Rechazar una mejora y
     * Pedir cambios en un PR siempre piden motivo; Rechazar en QA, si el proyecto lo pide.
     */
    private static final Map<String, AccionPendiente> RECHAZOS = Map.ofEntries(
            // El Líder decide si la mejora entra; rechazarla la deja fuera (PDF pág. 30)
            Map.entry("MEJR:PROPUESTA", new AccionPendiente("Rechazar", LIDER, "RECHAZADA")),
            Map.entry("ACTV:EN_REVISION", new AccionPendiente("Pedir cambios", LIDER, "EN_PROGRESO")),
            Map.entry("BUGS:EN_REVISION", new AccionPendiente("Pedir cambios", LIDER, "EN_CORRECCION")),
            Map.entry("BUGS:EN_QA", new AccionPendiente("Rechazar en QA", TESTER, "EN_CORRECCION")),
            // Ya en producción, el Analista comprueba que la falla desapareció (PDF pág. 23)
            Map.entry("BUGS:PUBLICADO", new AccionPendiente("Sigue fallando", TESTER, "EN_CORRECCION")),
            Map.entry("MEJR:EN_REVISION", new AccionPendiente("Pedir cambios", LIDER, "EN_PROGRESO")));

    /** Lo que le toca a cada Desarrollador de una historia sin Actividades (su fila en Mis tickets). */
    private static final Map<String, AccionPendiente> DE_DESARROLLADOR = Map.of(
            "LISTA_DESARROLLO", new AccionPendiente("Iniciar desarrollo", RESPONSABLE, null),
            "EN_DESARROLLO", new AccionPendiente("Terminar desarrollo", RESPONSABLE, null));

    /** En un proyecto Normal sin PR, el Desarrollador termina la Actividad él mismo: ya la subió a develop. */
    private static final AccionPendiente TERMINAR = new AccionPendiente("Terminar", RESPONSABLE, "APROBADA");

    /** Mientras alguien lo trabaja: ahí se puede marcar como bloqueado (PDF pág. 22). */
    private static final Set<String> EN_TRABAJO = Set.of("EN_PROGRESO", "EN_CORRECCION");

    /** Ya integrado, publicado o fuera: no se cancela ni se mueve más. */
    private static final Set<String> FINALES = Set.of("INTEGRADA", "INTEGRADO", "PUBLICADO", "CERRADA", "CERRADO",
            "CANCELADA", "NO_REPRODUCIBLE", "RECHAZADA");

    /** Mientras su historia no se inicia, nadie puede iniciar sus tickets. */
    private static final Set<String> HISTORIA_SIN_INICIAR = Set.of("BORRADOR", "EN_PLANEACION", "LISTA",
            "LISTA_DESARROLLO");

    private FlujoTrabajo() {
    }

    public static Optional<AccionPendiente> accionPendiente(ItemTrabajo item) {
        boolean esperaHistoria = "POR_HACER".equals(item.estado()) && esperaSuHistoria(item);
        if (esperaHistoria) {
            return Optional.empty();
        }
        return accionDelEstado(item);
    }

    /** La acción del estado aunque todavía no se pueda hacer (ej. su historia aún no se inicia). */
    public static Optional<AccionPendiente> accionDelEstado(ItemTrabajo item) {
        if (item.esHistoriaDeDesarrollador()) {
            return Optional.ofNullable(DE_DESARROLLADOR.get(item.estado()));
        }
        if (!item.requierePr() && "ACTV".equals(item.tipo()) && "EN_PROGRESO".equals(item.estado())) {
            return Optional.of(TERMINAR);
        }
        return Optional.ofNullable(ACCIONES.get(item.tipo() + ":" + item.estado()));
    }

    /** Si su historia aún no se inicia, sus tickets esperan. */
    public static boolean esperaSuHistoria(ItemTrabajo item) {
        return item.esTicket() && item.historiaEstado() != null && HISTORIA_SIN_INICIAR.contains(item.historiaEstado());
    }

    public static boolean enTrabajo(String estado) {
        return EN_TRABAJO.contains(estado);
    }

    /** Mientras el Líder revisa su PR o el Analista prueba el hotfix: quien lo hizo aún puede retirarlo. */
    public static boolean enRevision(String estado) {
        return "EN_REVISION".equals(estado) || "EN_QA".equals(estado);
    }

    public static boolean terminado(String estado) {
        return FINALES.contains(estado);
    }

    /** A dónde regresa al desbloquearlo o retirarlo de revisión: el estado en que se trabaja. */
    public static String estadoDeTrabajo(String tipo) {
        return ACCIONES.get(tipo + ":BLOQUEADA").destino();
    }

    public static Optional<AccionPendiente> rechazo(ItemTrabajo item) {
        return Optional.ofNullable(RECHAZOS.get(item.tipo() + ":" + item.estado()));
    }

    /** Las acciones que empiezan el trabajo: ahí se crea la rama y se revisan las dependencias. */
    public static boolean iniciaTrabajo(String estadoAnterior, String estadoNuevo) {
        return Set.of("POR_HACER", "CONFIRMADO", "APROBADA").contains(estadoAnterior)
                && EN_TRABAJO.contains(estadoNuevo);
    }

    /** Id de la persona que debe actuar, o null si nadie está asignado. */
    public static Long quienActua(ItemTrabajo item, Actor actor, Long liderId) {
        return switch (actor) {
            case RESPONSABLE -> item.responsableId();
            case TESTER -> item.testerId();
            case LIDER -> liderId;
        };
    }

    /** "Desarrollador", "Líder", "Analista"... para "Esperando a otros". */
    public static String nombreDelRol(ItemTrabajo item, Actor actor) {
        return switch (actor) {
            // Quien prueba el hotfix es un Analista
            case TESTER -> "Analista";
            case LIDER -> "Líder";
            case RESPONSABLE -> "HIST".equals(item.tipo()) ? "Analista" : "Desarrollador";
        };
    }
}
