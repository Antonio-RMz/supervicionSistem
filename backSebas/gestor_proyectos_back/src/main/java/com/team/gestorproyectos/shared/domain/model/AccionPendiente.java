package com.team.gestorproyectos.shared.domain.model;

/**
 * El botón que cambia el estado ("Confirmar", "Aprobar QA"...), quién puede presionarlo y a dónde lleva.
 *
 * @param destino estado al que pasa; null si todavía no se puede ejecutar desde una lista
 *                (publicar parche, integrar una historia: llevan su propio proceso)
 */
public record AccionPendiente(String accion, Actor actor, String destino) {

    public boolean ejecutable() {
        return destino != null;
    }
}
