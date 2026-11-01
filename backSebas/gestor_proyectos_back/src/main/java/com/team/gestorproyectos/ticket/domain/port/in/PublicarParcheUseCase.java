package com.team.gestorproyectos.ticket.domain.port.in;

import com.team.gestorproyectos.ticket.domain.model.Parche;

/** Publicar parche (PDF pág. 23): el Líder saca a producción los hotfix aprobados, sin esperar a ninguna fase. */
public interface PublicarParcheUseCase {

    /**
     * @param ticketId        el hotfix desde el que se publica; salen con él todos los aprobados del proyecto
     * @param versionEsperada la que vio en la confirmación; si ya cambió (otro parche salió), se rechaza
     */
    Parche publicar(Long proyectoId, Long ticketId, String versionEsperada, Long usuarioId, boolean esAdmin);
}
