package com.team.gestorproyectos.portalcliente.application;

import java.util.List;

import com.team.gestorproyectos.portalcliente.domain.model.ProyectoBase;
import com.team.gestorproyectos.portalcliente.domain.model.ProyectoCliente;
import com.team.gestorproyectos.portalcliente.domain.port.in.ListarMisProyectosUseCase;
import com.team.gestorproyectos.portalcliente.domain.port.out.PortalClienteConsultaPort;
import com.team.gestorproyectos.shared.application.ServicioAplicacion;
import com.team.gestorproyectos.shared.application.Transaccional;

import lombok.RequiredArgsConstructor;

@ServicioAplicacion
@RequiredArgsConstructor
public class ListarMisProyectosService implements ListarMisProyectosUseCase {

    private final PortalClienteConsultaPort consulta;

    @Override
    @Transaccional(soloLectura = true)
    public List<ProyectoCliente> listar(Long clienteId) {
        return consulta.proyectosDelCliente(clienteId).stream()
                .map(this::conAvance)
                .toList();
    }

    private ProyectoCliente conAvance(ProyectoBase proyecto) {
        return ProyectoCliente.armar(
                proyecto,
                consulta.fasesDelProyecto(proyecto.id()),
                consulta.conteoDeTickets(proyecto.id()),
                consulta.demosPendientes(proyecto.id()).size());
    }
}
