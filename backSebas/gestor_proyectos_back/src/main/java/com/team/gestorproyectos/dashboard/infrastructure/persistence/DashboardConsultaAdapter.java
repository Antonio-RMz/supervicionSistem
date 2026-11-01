package com.team.gestorproyectos.dashboard.infrastructure.persistence;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.team.gestorproyectos.dashboard.domain.model.ConteoCierres;
import com.team.gestorproyectos.shared.domain.model.ItemTrabajo;
import com.team.gestorproyectos.shared.domain.model.Movimiento;
import com.team.gestorproyectos.shared.domain.model.Persona;
import com.team.gestorproyectos.dashboard.domain.model.ProyectoDashboard;
import com.team.gestorproyectos.dashboard.domain.port.out.DashboardConsultaPort;
import com.team.gestorproyectos.dashboard.infrastructure.persistence.mapper.DashboardPersistenceMapper;
import com.team.gestorproyectos.dashboard.infrastructure.persistence.repository.DashboardJpaRepository;
import com.team.gestorproyectos.proyecto.infrastructure.persistence.repository.FaseJpaRepository;
import com.team.gestorproyectos.proyecto.infrastructure.persistence.repository.TicketJpaRepository;
import com.team.gestorproyectos.shared.domain.model.ConteoTickets;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DashboardConsultaAdapter implements DashboardConsultaPort {

    private final DashboardJpaRepository repository;
    private final FaseJpaRepository fases;
    private final TicketJpaRepository tickets;
    private final DashboardPersistenceMapper mapper;

    @Override
    public Optional<ProyectoDashboard> proyectoVisible(Long proyectoId, Long usuarioId, boolean esAdmin) {
        return repository.buscarVisible(proyectoId, usuarioId, esAdmin).map(mapper::aProyecto);
    }

    @Override
    public List<ItemTrabajo> itemsAbiertos(Long proyectoId) {
        return mapper.aItems(repository.itemsAbiertos(proyectoId));
    }

    @Override
    public List<Persona> personasDelProyecto(Long proyectoId) {
        return mapper.aPersonas(repository.personas(proyectoId));
    }

    @Override
    public List<FaseBasica> fases(Long proyectoId) {
        return mapper.aFases(fases.findByProyectoIdAndEsMantenimientoFalseOrderByNumero(proyectoId)).stream()
                .filter(fase -> !"CANCELADA".equals(fase.estado()))
                .toList();
    }

    @Override
    public List<ConteoTickets> conteoDeTickets(Long proyectoId) {
        return tickets.contarPorFaseModuloYEstado(proyectoId).stream()
                .map(fila -> new ConteoTickets(fila.faseId(), fila.moduloId(), fila.estado(), fila.cantidad()))
                .toList();
    }

    @Override
    public ConteoCierres cierresDesde(Long proyectoId, LocalDateTime desde) {
        return mapper.aCierres(repository.cierresDesde(proyectoId, desde));
    }

    @Override
    public List<Movimiento> ultimosMovimientos(Long proyectoId, int limite) {
        return mapper.aMovimientos(repository.ultimosMovimientos(proyectoId, limite));
    }
}
