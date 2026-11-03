package com.team.gestorproyectos.notificacion.application;

import com.team.gestorproyectos.notificacion.domain.model.Bandeja;
import com.team.gestorproyectos.notificacion.domain.port.in.NotificacionesUseCase;
import com.team.gestorproyectos.notificacion.domain.port.out.NotificacionesPort;
import com.team.gestorproyectos.shared.application.ServicioAplicacion;
import com.team.gestorproyectos.shared.application.Transaccional;

import lombok.RequiredArgsConstructor;

@ServicioAplicacion
@RequiredArgsConstructor
public class NotificacionesService implements NotificacionesUseCase {

    /** Las que caben en el panel de la campana. */
    private static final int RECIENTES = 30;

    private final NotificacionesPort notificaciones;

    @Override
    @Transaccional(soloLectura = true)
    public Bandeja bandeja(Long usuarioId) {
        return new Bandeja(notificaciones.noLeidas(usuarioId), notificaciones.recientes(usuarioId, RECIENTES));
    }

    @Override
    @Transaccional
    public void marcarLeida(Long usuarioId, Long notificacionId) {
        notificaciones.marcarLeida(usuarioId, notificacionId);
    }

    @Override
    @Transaccional
    public void marcarTodasLeidas(Long usuarioId) {
        notificaciones.marcarTodasLeidas(usuarioId);
    }
}
