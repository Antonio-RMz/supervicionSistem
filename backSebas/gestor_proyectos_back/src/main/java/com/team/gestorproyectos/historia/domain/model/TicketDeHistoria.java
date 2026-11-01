package com.team.gestorproyectos.historia.domain.model;

import java.util.Map;
import java.util.Set;

import com.team.gestorproyectos.shared.domain.model.ConteoTickets;
import com.team.gestorproyectos.shared.domain.model.CodigoItem;

/** Un ticket de la historia, con lo que importa para decidir sobre ella. */
public record TicketDeHistoria(Long id, String tipo, int numero, String titulo, String estado, Long responsableId,
                               Long testerId) {

    /** Mientras un ticket está en estos estados nadie ha empezado a trabajarlo. */
    private static final Set<String> SIN_EMPEZAR = Set.of("BORRADOR", "POR_HACER", "REPORTADO", "CONFIRMADO", "PROPUESTA");
    private static final Set<String> FUERA = Set.of("CANCELADA", "NO_REPRODUCIBLE", "RECHAZADA");

    /** A dónde regresa cada tipo cuando el Analista lo reabre al devolver la historia. */
    private static final Map<String, String> REABRIR_EN = Map.of(
            "ACTV", "EN_PROGRESO",
            "MEJR", "EN_PROGRESO",
            "BUGS", "EN_CORRECCION");

    /** Ya terminado, listo para que se integre: APROBADA, APROBADO, APROBADA_QA. */
    private static final Map<String, String> AL_INTEGRAR = Map.of(
            "APROBADA", "INTEGRADA",
            "APROBADO", "INTEGRADO",
            "APROBADA_QA", "INTEGRADA");

    public String codigo() {
        return CodigoItem.de(tipo, numero);
    }

    public boolean cuenta() {
        return !FUERA.contains(estado);
    }

    /** En una mejora "Aprobada" es la propuesta aceptada, aún sin empezar. */
    public boolean empezado() {
        return cuenta() && !SIN_EMPEZAR.contains(estado) && !("MEJR".equals(tipo) && "APROBADA".equals(estado));
    }

    public boolean terminado() {
        return new ConteoTickets(null, null, estado, 1).terminado() && !("MEJR".equals(tipo) && "APROBADA".equals(estado));
    }

    /** Se puede reabrir al devolver: ya pasó QA o revisión pero aún no se integra. */
    public boolean reabrible() {
        if ("MEJR".equals(tipo) && "APROBADA".equals(estado)) {
            return false;
        }
        return AL_INTEGRAR.containsKey(estado);
    }

    public String estadoAlReabrir() {
        return REABRIR_EN.get(tipo);
    }

    public static Map<String, String> alIntegrar() {
        return AL_INTEGRAR;
    }
}
