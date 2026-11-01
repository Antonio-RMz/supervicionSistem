package com.team.gestorproyectos.ticket.infrastructure.api.dto;

import java.time.LocalDateTime;

/** JSON de Mis tickets. Las pestañas se arman en el front con meToca, asignado y creadoPorMi. */
public final class MisTicketsDtos {

    private MisTicketsDtos() {
    }

    /**
     * @param historiaCodigo "HU-0012" si el ticket cuelga de una historia; si no, null y se muestra la fase
     * @param accion         solo si la siguiente acción es mía
     */
    public record TicketMioResponse(
            Long id,
            String tipo,
            String codigo,
            String titulo,
            String estado,
            String prioridad,
            Long faseId,
            Integer faseNumero,
            Long historiaId,
            String historiaCodigo,
            Long responsableId,
            String responsableNombre,
            String responsableIniciales,
            String accion,
            boolean rechazadaEnQa,
            boolean meToca,
            boolean asignado,
            boolean creadoPorMi,
            boolean ejecutable,
            LocalDateTime actualizadoEn) {
    }
}
