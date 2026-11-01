package com.team.gestorproyectos.portalcliente.domain.model;

import com.team.gestorproyectos.shared.domain.model.ConteoTickets;

import java.util.List;
import java.util.Optional;

/** Un proyecto como lo ve el cliente: sus fases con avance y lo que tiene pendiente de revisar. */
public record ProyectoCliente(
        Long id,
        String codigo,
        String nombre,
        EstadoProyecto estado,
        List<Modulo> modulos,
        List<AvanceFase> fases,
        int demosPendientes) {

    public static ProyectoCliente armar(ProyectoBase base, List<Fase> fases, List<ConteoTickets> conteos,
                                        int demosPendientes) {
        List<AvanceFase> avances = fases.stream()
                .filter(fase -> fase.estado() != EstadoFase.CANCELADA)
                .map(fase -> AvanceFase.calcular(fase, conteos))
                .toList();
        return new ProyectoCliente(base.id(), base.codigo(), base.nombre(), base.estado(), base.modulos(),
                avances, demosPendientes);
    }

    /** Promedio del avance de sus fases. */
    public int avance() {
        return (int) Math.round(fases.stream().mapToInt(AvanceFase::porcentaje).average().orElse(0));
    }

    /** La primera fase sin entregar: la que se trabaja ahora o la que sigue. */
    public Optional<AvanceFase> faseActual() {
        return fases.stream()
                .filter(avance -> avance.fase().estado() != EstadoFase.PUBLICADA)
                .findFirst();
    }
}
