package com.team.gestorproyectos.dashboard.domain.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.team.gestorproyectos.shared.domain.model.FlujoTrabajo;
import com.team.gestorproyectos.shared.domain.model.ItemPendiente;
import com.team.gestorproyectos.shared.domain.model.ItemTrabajo;
import com.team.gestorproyectos.shared.domain.model.Movimiento;
import com.team.gestorproyectos.shared.domain.model.Persona;

/**
 * Todo lo que muestra el Dashboard de una persona en un proyecto.
 *
 * @param pendientesMios total de items que esperan su acción (el número del saludo y del menú)
 */
public record Dashboard(
        ProyectoDashboard proyecto,
        Metricas metricas,
        int pendientesMios,
        List<ItemPendiente> teToca,
        List<ItemPendiente> esperandoAOtros,
        List<FaseDashboard> fases,
        List<Movimiento> actividad) {

    /** Cuántas filas se muestran en cada lista. */
    public static final int MAXIMO_POR_LISTA = 6;

    private static final Comparator<ItemPendiente> MAS_URGENTE_PRIMERO = Comparator
            .comparingInt((ItemPendiente p) -> p.item().ordenPrioridad())
            .thenComparing(p -> p.item().actualizadoEn(), Comparator.nullsLast(Comparator.reverseOrder()));

    /**
     * Reparte los items abiertos entre "Te toca a ti" (la acción es mía) y "Esperando a otros"
     * (participo, pero la acción es de alguien más).
     */
    public static Dashboard armar(Long usuarioId, ProyectoDashboard proyecto, List<ItemTrabajo> itemsAbiertos,
                                  Map<Long, Persona> personas, ConteoCierres cierres, List<FaseDashboard> fases,
                                  List<Movimiento> actividad, LocalDateTime ahora) {
        List<ItemPendiente> mios = new ArrayList<>();
        List<ItemPendiente> deOtros = new ArrayList<>();

        for (ItemTrabajo item : itemsAbiertos) {
            FlujoTrabajo.accionPendiente(item).ifPresent(accion -> {
                Long actorId = FlujoTrabajo.quienActua(item, accion.actor(), proyecto.liderId());
                ItemPendiente pendiente = new ItemPendiente(item, accion.accion(),
                        actorId == null ? null : personas.get(actorId),
                        FlujoTrabajo.nombreDelRol(item, accion.actor()));

                if (usuarioId.equals(actorId)) {
                    mios.add(pendiente);
                } else if (actorId != null && item.participa(usuarioId)) {
                    deOtros.add(pendiente);
                }
            });
        }
        mios.sort(MAS_URGENTE_PRIMERO);
        deOtros.sort(MAS_URGENTE_PRIMERO);

        return new Dashboard(
                proyecto,
                Metricas.calcular(itemsAbiertos, cierres, ahora),
                mios.size(),
                primeros(mios),
                primeros(deOtros),
                fases,
                actividad);
    }

    private static List<ItemPendiente> primeros(List<ItemPendiente> lista) {
        return List.copyOf(lista.subList(0, Math.min(MAXIMO_POR_LISTA, lista.size())));
    }
}
