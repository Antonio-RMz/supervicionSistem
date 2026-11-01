package com.team.gestorproyectos.ticket.application;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.team.gestorproyectos.shared.application.ServicioAplicacion;
import com.team.gestorproyectos.shared.application.Transaccional;
import com.team.gestorproyectos.shared.domain.exception.NoEncontradoException;
import com.team.gestorproyectos.shared.domain.model.Persona;
import com.team.gestorproyectos.ticket.domain.model.MisTickets;
import com.team.gestorproyectos.ticket.domain.model.TicketMio;
import com.team.gestorproyectos.ticket.domain.port.in.ListarMisTicketsUseCase;
import com.team.gestorproyectos.ticket.domain.port.out.TicketConsultaPort;

import lombok.RequiredArgsConstructor;

@ServicioAplicacion
@RequiredArgsConstructor
public class ListarMisTicketsService implements ListarMisTicketsUseCase {

    private final TicketConsultaPort consulta;

    @Override
    @Transaccional(soloLectura = true)
    public List<TicketMio> listar(Long proyectoId, Long usuarioId, boolean esAdmin) {
        if (!consulta.proyectoVisible(proyectoId, usuarioId, esAdmin)) {
            throw new NoEncontradoException("No encontramos ese proyecto");
        }
        Map<Long, Persona> personas = consulta.personasDelEquipo().stream()
                .collect(Collectors.toMap(Persona::id, Function.identity()));
        return MisTickets.armar(usuarioId, consulta.abiertos(proyectoId), personas);
    }
}
