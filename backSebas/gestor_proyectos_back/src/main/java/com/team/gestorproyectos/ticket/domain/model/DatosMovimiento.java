package com.team.gestorproyectos.ticket.domain.model;

/**
 * Lo que llega con un movimiento del ticket (PDF pág. 26).
 *
 * @param estadoEsperado el estado que vio quien lo mueve; si ya cambió, se rechaza
 * @param motivo         rechazar, bloquear y cancelar lo piden; en "Enviar a QA" es cómo probarlo
 * @param bloqueadoPorId al bloquear: el ticket que lo detiene; null si es algo externo
 * @param destrabadorId  al bloquear: quién puede destrabarlo (opcional, queda en el motivo)
 * @param severidad      al confirmar un bug: CRITICA, ALTA, MEDIA o BAJA (Crítica y Alta van por hotfix)
 * @param responsableId  al confirmar un bug o aprobar una mejora: el Desarrollador que lo hará
 */
public record DatosMovimiento(TipoMovimiento tipo, String estadoEsperado, String motivo, Long bloqueadoPorId,
                              Long destrabadorId, String severidad, Long responsableId) {

    public DatosMovimiento(TipoMovimiento tipo, String estadoEsperado, String motivo) {
        this(tipo, estadoEsperado, motivo, null, null, null, null);
    }
}
