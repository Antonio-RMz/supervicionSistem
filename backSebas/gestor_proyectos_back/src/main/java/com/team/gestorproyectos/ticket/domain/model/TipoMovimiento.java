package com.team.gestorproyectos.ticket.domain.model;

public enum TipoMovimiento {
    /** La acción principal del estado: Iniciar, Enviar a QA, Aprobar QA... */
    AVANZAR,
    /** Quien revisa regresa el trabajo: Rechazar en QA, Pedir cambios... */
    RECHAZAR,
    /** Quien lo trabaja lo detiene por algo que no depende de él (PDF pág. 26). Pide motivo. */
    BLOQUEAR,
    /** Quien lo hizo lo saca de QA o revisión antes de que lo revisen. */
    RETIRAR,
    /** El Líder lo cancela. Pide motivo. */
    CANCELAR,
    /** El Líder no logra reproducir un bug reportado (PDF pág. 23). Pide motivo. */
    NO_REPRODUCIBLE,
    /** El Líder reabre un bug que no se pudo reproducir: vuelve a Reportado. */
    REABRIR,
    /** Un hotfix aprobado sale a producción en un parche; se hace con PublicarParcheUseCase. */
    PUBLICAR_PARCHE
}
