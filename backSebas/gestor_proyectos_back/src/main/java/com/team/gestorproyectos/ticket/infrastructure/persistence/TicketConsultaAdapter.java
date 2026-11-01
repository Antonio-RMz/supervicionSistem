package com.team.gestorproyectos.ticket.infrastructure.persistence;

import java.util.List;

import org.springframework.stereotype.Component;

import com.team.gestorproyectos.shared.domain.model.Persona;
import com.team.gestorproyectos.ticket.domain.model.FaseTablero;
import com.team.gestorproyectos.ticket.domain.model.FilaTicket;
import com.team.gestorproyectos.ticket.domain.port.out.TicketConsultaPort;
import com.team.gestorproyectos.ticket.infrastructure.persistence.mapper.TicketPersistenceMapper;
import com.team.gestorproyectos.ticket.infrastructure.persistence.repository.TicketConsultaJpaRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TicketConsultaAdapter implements TicketConsultaPort {

    private final TicketConsultaJpaRepository repository;
    private final TicketPersistenceMapper mapper;

    @Override
    public boolean proyectoVisible(Long proyectoId, Long usuarioId, boolean esAdmin) {
        Long visible = repository.proyectoVisible(proyectoId, usuarioId, esAdmin);
        return visible != null && visible == 1;
    }

    @Override
    public List<FilaTicket> abiertos(Long proyectoId) {
        return mapper.aFilas(repository.abiertos(proyectoId));
    }

    @Override
    public List<Persona> personasDelEquipo() {
        return mapper.aPersonas(repository.personasDelEquipo());
    }

    @Override
    public List<FaseTablero> fases(Long proyectoId) {
        return mapper.aFases(repository.fases(proyectoId));
    }

    @Override
    public List<FilaTicket> deLaFase(Long proyectoId, Long faseId) {
        return mapper.aFilas(repository.deLaFase(proyectoId, faseId));
    }
}
