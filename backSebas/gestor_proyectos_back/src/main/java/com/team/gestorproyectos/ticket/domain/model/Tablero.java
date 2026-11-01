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

/** Tablero de una fase (PDF pág. 12): tickets por columna y qué puede hacer con cada uno quien lo ve. */
public final class Tablero {

    private static final Comparator<Tarjeta> ORDEN = Comparator
            .comparingInt((Tarjeta t) -> t.fila().item().item().ordenPrioridad())
            .thenComparing(t -> t.fila().item().item().numero());

    private Tablero() {
    }

    public static List<Tarjeta> armar(Long usuarioId, List<FilaTicket> filas, Map<Long, Persona> personas) {
        List<Tarjeta> tarjetas = new ArrayList<>();
        for (FilaTicket fila : filas) {
            ItemDeProyecto deProyecto = fila.item();
            ItemTrabajo item = deProyecto.item();
            ColumnaTablero.de(item.tipo(), item.estado()).ifPresent(columna -> {
                Optional<AccionPendiente> accion = FlujoTrabajo.accionPendiente(item);
                boolean meToca = accion
                        .map(a -> usuarioId.equals(FlujoTrabajo.quienActua(item, a.actor(), deProyecto.liderId())))
                        .orElse(false);

                Tarjeta.Movimiento avance = accion.filter(a -> meToca && a.ejecutable())
                        .flatMap(a -> columnaDe(item, a)
                                .map(c -> new Tarjeta.Movimiento(TipoMovimiento.AVANZAR, a.accion(), c)))
                        .orElse(null);
                Tarjeta.Movimiento rechazo = FlujoTrabajo.rechazo(item)
                        .filter(r -> usuarioId.equals(FlujoTrabajo.quienActua(item, r.actor(), deProyecto.liderId())))
                        .flatMap(r -> columnaDe(item, r)
                                .map(c -> new Tarjeta.Movimiento(TipoMovimiento.RECHAZAR, r.accion(), c)))
                        .orElse(null);
                String bloqueadaPor = accion.filter(a -> !meToca)
                        .map(a -> FlujoTrabajo.nombreDelRol(item, a.actor()))
                        .orElse(null);

                tarjetas.add(new Tarjeta(fila, columna,
                        item.responsableId() == null ? null : personas.get(item.responsableId()),
                        avance, rechazo, bloqueadaPor,
                        item.participa(usuarioId)));
            });
        }
        tarjetas.sort(ORDEN);
        return List.copyOf(tarjetas);
    }

    /** La fase en curso más adelantada; si ninguna está en curso, la primera. Mantenimiento va al final. */
    public static Optional<FaseTablero> faseInicial(List<FaseTablero> fases) {
        return fases.stream()
                .filter(f -> "EN_CURSO".equals(f.estado()) && !f.mantenimiento())
                .max(Comparator.comparingInt(FaseTablero::numero))
                .or(() -> fases.stream().filter(f -> !f.mantenimiento()).findFirst())
                .or(() -> fases.stream().findFirst());
    }

    /**
     * La columna a la que se arrastra la tarjeta. Vacío si el destino no tiene columna (rechazar una mejora
     * la deja Rechazada, fuera del tablero): esa acción se hace solo desde el detalle (PDF pág. 30).
     */
    private static Optional<ColumnaTablero> columnaDe(ItemTrabajo item, AccionPendiente accion) {
        return ColumnaTablero.de(item.tipo(), accion.destino());
    }
}
