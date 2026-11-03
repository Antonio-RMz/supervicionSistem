package com.team.gestorproyectos.notificacion.domain.port.out;

import java.util.List;

import com.team.gestorproyectos.notificacion.domain.model.Notificacion;

public interface NotificacionesPort {

    List<Notificacion> recientes(Long usuarioId, int limite);

    long noLeidas(Long usuarioId);

    /** Solo si es de esa persona. */
    void marcarLeida(Long usuarioId, Long notificacionId);

    void marcarTodasLeidas(Long usuarioId);
}
