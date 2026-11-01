package com.team.gestorproyectos.portalcliente.domain.port.in;

import java.util.List;

import com.team.gestorproyectos.portalcliente.domain.model.ProyectoCliente;

/** Pantalla "Mis proyectos": los proyectos de la empresa del cliente con su avance. */
public interface ListarMisProyectosUseCase {

    List<ProyectoCliente> listar(Long clienteId);
}
