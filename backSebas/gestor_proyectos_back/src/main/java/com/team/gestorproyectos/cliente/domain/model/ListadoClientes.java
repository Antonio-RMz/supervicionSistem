package com.team.gestorproyectos.cliente.domain.model;

import java.util.List;

/** @param puedeGestionar si quien consulta puede crear y editar clientes (si no, solo los ve) */
public record ListadoClientes(List<ClienteConProyectos> clientes, boolean puedeGestionar) {
}
