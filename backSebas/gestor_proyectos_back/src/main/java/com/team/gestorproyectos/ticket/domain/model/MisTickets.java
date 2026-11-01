package com.team.gestorproyectos.ticket.domain.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.team.gestorproyectos.shared.domain.model.AccionPendiente;
import com.team.gestorproyectos.shared.domain.model.FlujoTrabajo;
import com.team.gestorproyectos.shared.domain.model.ItemDeProyecto;
import com.team.gestorproyectos.shared.domain.model.ItemTrabajo;
import com.team.gestorproyectos.shared.domain.model.Persona;

/**
 * Mis tickets de un proyecto (PDF pág. 11). Las cuatro pestañas salen de la misma lista:
 * <ul>
 *   <li>Me toca: la siguiente acción es mía.</li>
 *   <li>Asignados a mí: lo trabajo, lo pruebo o analizo la historia.</li>
 *   <li>Creados por mí.</li>
 *   <li>Siguiendo: todo lo anterior; y si soy el Líder del proyecto, todo lo abierto.</li>
 * </ul>
 */
public final class MisTickets {

    private static final String CORREGIR = "Corregir";

    private static final Comparator<TicketMio> MAS_URGENTE_PRIMERO = Comparator
            .comparing(TicketMio::meToca).reversed()
            .thenComparingInt(t -> t.fila().item().item().ordenPrioridad())
            .thenComparing(t -> t.fila().item().item().actualizadoEn(), Comparator.nullsLast(Comparator.reverseOrder()));

    private MisTickets() {
    }

    public static List<TicketMio> armar(Long usuarioId, List<FilaTicket> filas, Map<Long, Persona> personas) {
        List<TicketMio> mios = new ArrayList<>();
        for (FilaTicket fila : filas) {
            ItemDeProyecto deProyecto = fila.item();
            ItemTrabajo item = deProyecto.item();
            boolean soyLider = usuarioId.equals(deProyecto.liderId());
            if (!item.participa(usuarioId) && !soyLider) {
                continue;
            }

            Optional<AccionPendiente> accion = FlujoTrabajo.accionPendiente(item);
            boolean meToca = accion
                    .map(a -> usuarioId.equals(FlujoTrabajo.quienActua(item, a.actor(), deProyecto.liderId())))
                    .orElse(false);
            boolean rechazada = deProyecto.rechazadoEnQa();
            String boton = meToca ? (rechazada ? CORREGIR : accion.get().accion()) : null;

            mios.add(new TicketMio(
                    fila,
                    item.responsableId() == null ? null : personas.get(item.responsableId()),
                    boton,
                    rechazada,
                    meToca,
                    usuarioId.equals(item.responsableId()) || usuarioId.equals(item.testerId()),
                    usuarioId.equals(item.creadoPorId()),
                    meToca && !rechazada && item.esTicket() && accion.get().ejecutable()));
        }
        mios.sort(MAS_URGENTE_PRIMERO);
        return List.copyOf(mios);
    }
}
