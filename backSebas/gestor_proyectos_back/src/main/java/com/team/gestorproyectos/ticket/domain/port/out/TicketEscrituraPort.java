package com.team.gestorproyectos.ticket.domain.port.out;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.team.gestorproyectos.ticket.domain.model.NuevoTicket;
import com.team.gestorproyectos.ticket.domain.model.PreparacionTicket.FaseDeTicket;
import com.team.gestorproyectos.ticket.domain.model.PreparacionTicket.PersonaDeEquipo;
import com.team.gestorproyectos.ticket.domain.model.PreparacionTicket.TicketAbierto;
import com.team.gestorproyectos.ticket.domain.model.TicketEditable;
import com.team.gestorproyectos.ticket.domain.model.TipoTicket;

public interface TicketEscrituraPort {

    /** Vacío si no existe o el usuario no es miembro, su Líder o Admin. */
    Optional<ProyectoDeTicket> proyectoVisible(Long proyectoId, Long usuarioId, boolean esAdmin);

    /** Fases que no están cerradas ni canceladas, con sus módulos y las historias que ya pueden tener tickets. */
    List<FaseDeTicket> fasesConHistorias(Long proyectoId);

    /** Miembros activos y el Líder. */
    List<PersonaDeEquipo> equipo(Long proyectoId);

    Map<TipoTicket, Integer> siguientesNumeros(Long proyectoId);

    List<TicketAbierto> abiertos(Long proyectoId);

    /** Guarda el ticket con sus criterios, subtareas y dependencias, y lo deja en el historial. */
    Long crear(Long proyectoId, int numero, NuevoTicket nuevo, Long usuarioId);

    /** Vacío si no existe, no es de ese proyecto o va suelto (sin historia). */
    Optional<TicketEditable> editable(Long proyectoId, Long ticketId);

    /** Reemplaza lo capturado con sus criterios y dependencias; tipo, fase, historia y subtareas no cambian. */
    void actualizar(Long ticketId, NuevoTicket datos);

    record ProyectoDeTicket(Long id, String nombre, Long liderId) {
    }
}
