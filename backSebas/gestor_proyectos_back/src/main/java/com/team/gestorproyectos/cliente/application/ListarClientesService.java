package com.team.gestorproyectos.cliente.application;

import com.team.gestorproyectos.cliente.domain.model.ListadoClientes;
import com.team.gestorproyectos.cliente.domain.port.in.ListarClientesUseCase;
import com.team.gestorproyectos.cliente.domain.port.out.ClienteRepositoryPort;
import com.team.gestorproyectos.shared.application.ServicioAplicacion;
import com.team.gestorproyectos.shared.application.Transaccional;
import com.team.gestorproyectos.shared.domain.port.PermisosPort;

import lombok.RequiredArgsConstructor;

@ServicioAplicacion
@RequiredArgsConstructor
public class ListarClientesService implements ListarClientesUseCase {

    private final ClienteRepositoryPort clientes;
    private final PermisosPort permisos;

    @Override
    @Transaccional(soloLectura = true)
    public ListadoClientes listar(Long usuarioId) {
        return new ListadoClientes(clientes.listar(), permisos.puede(usuarioId, PermisosPort.GESTIONAR_CLIENTES));
    }
}
