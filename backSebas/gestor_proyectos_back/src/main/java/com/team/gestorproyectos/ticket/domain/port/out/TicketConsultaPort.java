package com.team.gestorproyectos.ticket.domain.port.out;

import java.util.List;

import com.team.gestorproyectos.shared.domain.model.Persona;
import com.team.gestorproyectos.ticket.domain.model.FaseTablero;
import com.team.gestorproyectos.ticket.domain.model.FilaTicket;

public interface TicketConsultaPort {

    /** Si el proyecto existe y el usuario es miembro (o Admin). */
    boolean proyectoVisible(Long proyectoId, Long usuarioId, boolean esAdmin);

    /** Tickets e historias que no están cerrados, cancelados ni descartados. */
    List<FilaTicket> abiertos(Long proyectoId);

    List<Persona> personasDelEquipo();

    /** En orden de número; Mantenimiento (número 0) al final. */
    List<FaseTablero> fases(Long proyectoId);

    /** Tickets de la fase en cualquier estado, sin historias. */
    List<FilaTicket> deLaFase(Long proyectoId, Long faseId);
}
