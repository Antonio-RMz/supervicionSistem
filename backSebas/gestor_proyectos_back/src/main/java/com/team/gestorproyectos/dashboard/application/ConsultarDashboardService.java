package com.team.gestorproyectos.dashboard.application;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.team.gestorproyectos.dashboard.domain.model.Dashboard;
import com.team.gestorproyectos.dashboard.domain.model.FaseDashboard;
import com.team.gestorproyectos.dashboard.domain.model.ProyectoDashboard;
import com.team.gestorproyectos.dashboard.domain.port.in.ConsultarDashboardUseCase;
import com.team.gestorproyectos.dashboard.domain.port.out.DashboardConsultaPort;
import com.team.gestorproyectos.shared.application.ServicioAplicacion;
import com.team.gestorproyectos.shared.application.Transaccional;
import com.team.gestorproyectos.shared.domain.exception.NoEncontradoException;
import com.team.gestorproyectos.shared.domain.model.ConteoTickets;
import com.team.gestorproyectos.shared.domain.model.Persona;

import lombok.RequiredArgsConstructor;

@ServicioAplicacion
@RequiredArgsConstructor
public class ConsultarDashboardService implements ConsultarDashboardUseCase {

    private static final int MOVIMIENTOS_RECIENTES = 5;

    private final DashboardConsultaPort consulta;

    @Override
    @Transaccional(soloLectura = true)
    public Dashboard consultar(Long proyectoId, Long usuarioId, boolean esAdmin) {
        ProyectoDashboard proyecto = consulta.proyectoVisible(proyectoId, usuarioId, esAdmin)
                .orElseThrow(() -> new NoEncontradoException("No encontramos ese proyecto"));

        LocalDateTime ahora = LocalDateTime.now();
        List<ConteoTickets> conteos = consulta.conteoDeTickets(proyectoId);
        List<FaseDashboard> fases = consulta.fases(proyectoId).stream()
                .map(f -> FaseDashboard.calcular(f.id(), f.numero(), f.nombre(), f.estado(), conteos))
                .toList();
        Map<Long, Persona> personas = consulta.personasDelProyecto(proyectoId).stream()
                .collect(Collectors.toMap(Persona::id, Function.identity(), (a, b) -> a));

        return Dashboard.armar(
                usuarioId,
                proyecto,
                consulta.itemsAbiertos(proyectoId),
                personas,
                consulta.cierresDesde(proyectoId, ahora.minusDays(7)),
                fases,
                consulta.ultimosMovimientos(proyectoId, MOVIMIENTOS_RECIENTES),
                ahora);
    }
}
