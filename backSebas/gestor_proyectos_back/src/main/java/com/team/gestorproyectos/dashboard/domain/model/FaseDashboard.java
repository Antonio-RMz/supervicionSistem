package com.team.gestorproyectos.dashboard.domain.model;

import java.util.List;

import com.team.gestorproyectos.shared.domain.model.ConteoTickets;
import com.team.gestorproyectos.shared.domain.model.Progreso;

/** Una fila de "Avance por fase". */
public record FaseDashboard(Long id, int numero, String nombre, String estado, int porcentaje) {

    public static FaseDashboard calcular(Long id, int numero, String nombre, String estado,
                                         List<ConteoTickets> conteosDelProyecto) {
        return new FaseDashboard(id, numero, nombre, estado, Progreso.deFase(id, estado, conteosDelProyecto));
    }
}
