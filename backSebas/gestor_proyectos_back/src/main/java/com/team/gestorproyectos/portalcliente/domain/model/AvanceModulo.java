package com.team.gestorproyectos.portalcliente.domain.model;

import com.team.gestorproyectos.shared.domain.model.ConteoTickets;
import com.team.gestorproyectos.shared.domain.model.Progreso;

import java.util.ArrayList;
import java.util.List;

/** Avance de un módulo (API, App móvil...) dentro de una fase. */
public record AvanceModulo(Modulo modulo, int porcentaje) {

    /** Solo aparecen los módulos que tienen tickets en esa fase. */
    public static List<AvanceModulo> calcular(List<Modulo> modulos, Long faseId, List<ConteoTickets> conteosDelProyecto) {
        List<AvanceModulo> avances = new ArrayList<>();
        for (Modulo modulo : modulos) {
            Progreso progreso = Progreso.de(conteosDelProyecto.stream()
                    .filter(c -> faseId.equals(c.faseId()) && modulo.id().equals(c.moduloId()))
                    .toList());
            if (progreso.total() > 0) {
                avances.add(new AvanceModulo(modulo, progreso.porcentaje()));
            }
        }
        return avances;
    }
}
