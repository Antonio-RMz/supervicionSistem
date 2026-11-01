package com.team.gestorproyectos.ticket.application;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.team.gestorproyectos.shared.application.ServicioAplicacion;
import com.team.gestorproyectos.shared.application.Transaccional;
import com.team.gestorproyectos.shared.domain.exception.NoEncontradoException;
import com.team.gestorproyectos.shared.domain.model.Persona;
import com.team.gestorproyectos.ticket.domain.model.FaseTablero;
import com.team.gestorproyectos.ticket.domain.model.Tablero;
import com.team.gestorproyectos.ticket.domain.model.VistaTablero;
import com.team.gestorproyectos.ticket.domain.port.in.ConsultarTableroUseCase;
import com.team.gestorproyectos.ticket.domain.port.out.TicketConsultaPort;

import lombok.RequiredArgsConstructor;

@ServicioAplicacion
@RequiredArgsConstructor
public class ConsultarTableroService implements ConsultarTableroUseCase {

    private final TicketConsultaPort consulta;

    @Override
    @Transaccional(soloLectura = true)
    public VistaTablero consultar(Long proyectoId, Long faseId, Long usuarioId, boolean esAdmin) {
        if (!consulta.proyectoVisible(proyectoId, usuarioId, esAdmin)) {
            throw new NoEncontradoException("No encontramos ese proyecto");
        }
        List<FaseTablero> fases = consulta.fases(proyectoId);
        FaseTablero fase = faseId == null
                ? Tablero.faseInicial(fases).orElse(null)
                : fases.stream().filter(f -> f.id().equals(faseId)).findFirst()
                        .orElseThrow(() -> new NoEncontradoException("Esa fase no es de este proyecto"));
        if (fase == null) {
            return new VistaTablero(fases, null, List.of());
        }

        Map<Long, Persona> personas = consulta.personasDelEquipo().stream()
                .collect(Collectors.toMap(Persona::id, Function.identity()));
        return new VistaTablero(fases, fase.id(),
                Tablero.armar(usuarioId, consulta.deLaFase(proyectoId, fase.id()), personas));
    }
}
