package com.team.gestorproyectos.notificacion.domain.port.in;

import com.team.gestorproyectos.notificacion.domain.model.Bandeja;

/** La campana: cada persona ve y marca solo sus propias notificaciones. */
public interface NotificacionesUseCase {

    Bandeja bandeja(Long usuarioId);

    void marcarLeida(Long usuarioId, Long notificacionId);

    void marcarTodasLeidas(Long usuarioId);
}
