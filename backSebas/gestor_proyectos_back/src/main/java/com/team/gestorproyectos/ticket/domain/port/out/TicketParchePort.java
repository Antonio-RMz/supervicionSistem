package com.team.gestorproyectos.ticket.domain.port.out;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.team.gestorproyectos.ticket.domain.model.Parche;

public interface TicketParchePort {

    /** La versión publicada más reciente del proyecto (de fase o parche); vacío si nunca se ha publicado. */
    Optional<String> versionEnProduccion(Long proyectoId);

    /** Bugs Críticos o Altos de Mantenimiento ya aprobados en QA, listos para el parche. */
    List<Parche.Incluido> hotfixAprobados(Long proyectoId);

    /** Pasa los bugs a Publicado (con historial) y registra el release del parche como publicado. */
    void publicar(Long proyectoId, Parche parche, Long usuarioId, LocalDateTime ahora);
}
