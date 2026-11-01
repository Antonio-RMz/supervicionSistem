package com.team.gestorproyectos.usuario.domain.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.team.gestorproyectos.shared.domain.model.FlujoTrabajo;
import com.team.gestorproyectos.shared.domain.model.ItemDeProyecto;
import com.team.gestorproyectos.shared.domain.model.ItemPendiente;
import com.team.gestorproyectos.shared.domain.model.Persona;

/**
 * En qué anda una persona en todos sus proyectos (PDF pág. 7, "Trabajo actual").
 *
 * @param trabajandoAhora lo que tiene en progreso
 * @param leToca          espera su acción: iniciar, revisar, corregir lo que QA rechazó...
 * @param esperandoAOtros participa, pero la acción es de alguien más
 */
public record TrabajoActual(List<Tarea> trabajandoAhora, List<Tarea> leToca, List<Tarea> esperandoAOtros) {

    private static final String CORREGIR = "Corregir";

    private static final Comparator<Tarea> MAS_URGENTE_PRIMERO = Comparator
            .comparing(Tarea::rechazadaEnQa).reversed()
            .thenComparingInt(t -> t.pendiente().item().ordenPrioridad())
            .thenComparing(t -> t.pendiente().item().actualizadoEn(), Comparator.nullsLast(Comparator.reverseOrder()));

    public static TrabajoActual armar(Long usuarioId, List<ItemDeProyecto> itemsAbiertos, Map<Long, Persona> personas) {
        List<Tarea> trabajando = new ArrayList<>();
        List<Tarea> leToca = new ArrayList<>();
        List<Tarea> esperando = new ArrayList<>();

        for (ItemDeProyecto deProyecto : itemsAbiertos) {
            var item = deProyecto.item();
            FlujoTrabajo.accionPendiente(item).ifPresent(accion -> {
                Long actorId = FlujoTrabajo.quienActua(item, accion.actor(), deProyecto.liderId());
                boolean rechazada = deProyecto.rechazadoEnQa();
                ItemPendiente pendiente = new ItemPendiente(item,
                        rechazada ? CORREGIR : accion.accion(),
                        actorId == null ? null : personas.get(actorId),
                        FlujoTrabajo.nombreDelRol(item, accion.actor()));
                Tarea tarea = new Tarea(pendiente, rechazada);

                if (usuarioId.equals(actorId)) {
                    (deProyecto.enTrabajo() && !rechazada ? trabajando : leToca).add(tarea);
                } else if (actorId != null && item.participa(usuarioId)) {
                    esperando.add(tarea);
                }
            });
        }
        trabajando.sort(MAS_URGENTE_PRIMERO);
        leToca.sort(MAS_URGENTE_PRIMERO);
        esperando.sort(MAS_URGENTE_PRIMERO);
        return new TrabajoActual(List.copyOf(trabajando), List.copyOf(leToca), List.copyOf(esperando));
    }
}
