package com.team.gestorproyectos.ticket.domain.port.in;

import java.util.List;

import com.team.gestorproyectos.ticket.domain.model.EdicionTicket;
import com.team.gestorproyectos.ticket.domain.model.NuevoTicket;
import com.team.gestorproyectos.ticket.domain.model.PreparacionTicket;
import com.team.gestorproyectos.ticket.domain.model.TicketImportado;

/** Nuevo ticket de trabajo (PDF pág. 19). Pide "ticket.crear" y ser miembro del proyecto. */
public interface CrearTicketUseCase {

    PreparacionTicket preparar(Long proyectoId, Long usuarioId, boolean esAdmin);

    /** Devuelve el id del ticket nuevo. */
    Long crear(Long proyectoId, Long usuarioId, boolean esAdmin, NuevoTicket nuevo);

    /** Mientras no empiece, lo editan quien lo creó o el Líder. */
    EdicionTicket edicion(Long proyectoId, Long ticketId, Long usuarioId, boolean esAdmin);

    void editar(Long proyectoId, Long ticketId, Long usuarioId, boolean esAdmin, NuevoTicket pedido);

    /**
     * Importar tickets: crea varias Actividades de un JSON dentro de la historia, todas con el mismo responsable.
     * Si una no es válida no se crea ninguna. Devuelve cuántas se crearon.
     */
    int importar(Long proyectoId, Long usuarioId, boolean esAdmin, Long historiaId, Long responsableId,
                 List<TicketImportado> importados);
}
