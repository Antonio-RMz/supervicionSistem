package com.team.gestorproyectos.ticket.domain.port.in;

import com.team.gestorproyectos.ticket.domain.model.VistaTablero;

public interface ConsultarTableroUseCase {

    /** @param faseId null = la fase en curso */
    VistaTablero consultar(Long proyectoId, Long faseId, Long usuarioId, boolean esAdmin);
}
