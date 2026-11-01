package com.team.gestorproyectos.cliente.domain.port.in;

import com.team.gestorproyectos.cliente.domain.model.ListadoClientes;

/** Todo el equipo ve los clientes; solo quien tiene "cliente.gestionar" los cambia. */
public interface ListarClientesUseCase {

    ListadoClientes listar(Long usuarioId);
}
