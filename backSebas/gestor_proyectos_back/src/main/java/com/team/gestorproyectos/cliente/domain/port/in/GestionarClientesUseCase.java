package com.team.gestorproyectos.cliente.domain.port.in;

import com.team.gestorproyectos.cliente.domain.model.ClienteConProyectos;
import com.team.gestorproyectos.cliente.domain.model.DatosCliente;

/** Crear, editar y activar/desactivar clientes. Pide el permiso "cliente.gestionar". */
public interface GestionarClientesUseCase {

    ClienteConProyectos crear(Long usuarioId, DatosCliente datos);

    ClienteConProyectos actualizar(Long usuarioId, Long clienteId, DatosCliente datos);

    /** Los inactivos no aparecen al crear proyectos nuevos; los que ya tienen siguen igual. */
    ClienteConProyectos cambiarEstado(Long usuarioId, Long clienteId, boolean activo);
}
