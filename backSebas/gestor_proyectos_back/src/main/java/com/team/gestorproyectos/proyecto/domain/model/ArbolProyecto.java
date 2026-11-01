package com.team.gestorproyectos.proyecto.domain.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.team.gestorproyectos.proyecto.domain.port.out.ProyectoConsultaPort.HistoriaBase;
import com.team.gestorproyectos.proyecto.domain.port.out.ProyectoConsultaPort.TicketBase;
import com.team.gestorproyectos.shared.domain.model.AccionPendiente;
import com.team.gestorproyectos.shared.domain.model.ConteoTickets;
import com.team.gestorproyectos.shared.domain.model.FlujoTrabajo;
import com.team.gestorproyectos.shared.domain.model.ItemTrabajo;
import com.team.gestorproyectos.shared.domain.model.Persona;
import com.team.gestorproyectos.shared.domain.model.CodigoItem;

/** Fases, sus historias y los tickets de cada historia (PDF pág. 15, "Fases e historias"). */
public final class ArbolProyecto {

    private ArbolProyecto() {
    }

    public static List<FaseArbol> armar(Long usuarioId, List<FaseResumen> fases, List<HistoriaBase> historias,
                                        List<TicketBase> tickets, Map<Long, Persona> personas) {
        Map<Long, List<TicketBase>> porHistoria = tickets.stream()
                .filter(t -> t.historiaId() != null)
                .collect(Collectors.groupingBy(TicketBase::historiaId));

        List<FaseArbol> arbol = new ArrayList<>();
        for (FaseResumen fase : fases) {
            List<HistoriaArbol> deLaFase = historias.stream()
                    .filter(h -> fase.id().equals(h.faseId()))
                    .sorted(Comparator.comparingInt(HistoriaBase::numero))
                    .map(h -> historia(usuarioId, h, porHistoria.getOrDefault(h.id(), List.of()), personas))
                    .toList();
            List<TicketArbol> sueltos = tickets.stream()
                    .filter(t -> t.historiaId() == null && fase.id().equals(t.faseId()))
                    .map(t -> ticket(usuarioId, t, personas))
                    .toList();
            arbol.add(new FaseArbol(fase, deLaFase, sueltos));
        }
        return List.copyOf(arbol);
    }

    private static HistoriaArbol historia(Long usuarioId, HistoriaBase h, List<TicketBase> tickets,
                                          Map<Long, Persona> personas) {
        List<ConteoTickets> conteos = tickets.stream()
                .map(t -> new ConteoTickets(t.faseId(), null, t.item().item().estado(), 1))
                .toList();
        long total = conteos.stream().filter(ConteoTickets::cuenta).count();
        long listos = conteos.stream().filter(c -> c.cuenta() && c.terminado()).count();
        return new HistoriaArbol(h.id(), CodigoItem.historia(h.numero()), h.titulo(), h.estado(),
                h.analistaId(), h.analistaIniciales(), listos, total,
                tickets.stream().sorted(Comparator.comparing((TicketBase t) -> t.item().item().tipo())
                                .thenComparingInt(t -> t.item().item().numero()))
                        .map(t -> ticket(usuarioId, t, personas)).toList());
    }

    /** Si la siguiente acción es mía, el botón; si es de alguien más, quién ("Ana · Analista"). */
    private static TicketArbol ticket(Long usuarioId, TicketBase t, Map<Long, Persona> personas) {
        ItemTrabajo item = t.item().item();
        Optional<AccionPendiente> accion = FlujoTrabajo.accionPendiente(item);
        Long actorId = accion.map(a -> FlujoTrabajo.quienActua(item, a.actor(), t.item().liderId())).orElse(null);

        String boton = null;
        String quien = null;
        if (accion.isPresent() && actorId != null) {
            if (usuarioId.equals(actorId)) {
                boton = accion.get().ejecutable() && !t.item().rechazadoEnQa() ? accion.get().accion() : null;
            } else {
                quien = Optional.ofNullable(personas.get(actorId)).map(Persona::nombre).orElse("?")
                        + " · " + FlujoTrabajo.nombreDelRol(item, accion.get().actor());
            }
        }
        return new TicketArbol(item.id(), item.tipo(), item.codigo(), item.titulo(), item.estado(), t.modulo(),
                item.responsableId(), t.responsableIniciales(),
                Optional.ofNullable(item.responsableId()).map(personas::get).map(Persona::nombre).orElse(null),
                boton, quien);
    }
}
