package com.team.gestorproyectos.portalcliente.domain.model;

import com.team.gestorproyectos.shared.domain.model.ConteoTickets;
import com.team.gestorproyectos.shared.domain.model.Progreso;

import java.util.List;

public record AvanceFase(Fase fase, int porcentaje) {

    /** Una fase publicada siempre va al 100%; si no, cuenta sus tickets terminados. */
    public static AvanceFase calcular(Fase fase, List<ConteoTickets> conteosDelProyecto) {
        if (fase.estado() == EstadoFase.PUBLICADA) {
            return new AvanceFase(fase, 100);
        }
        List<ConteoTickets> deLaFase = conteosDelProyecto.stream()
                .filter(c -> fase.id().equals(c.faseId()))
                .toList();
        return new AvanceFase(fase, Progreso.de(deLaFase).porcentaje());
    }

    public EstadoFaseCliente estadoCliente() {
        return fase.estado().paraCliente();
    }
}
