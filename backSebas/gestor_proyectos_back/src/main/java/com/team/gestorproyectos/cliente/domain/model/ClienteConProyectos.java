package com.team.gestorproyectos.cliente.domain.model;

import java.util.List;

/** Una fila de la tabla de Clientes. */
public record ClienteConProyectos(Cliente cliente, List<ProyectoDeCliente> proyectos) {
}
