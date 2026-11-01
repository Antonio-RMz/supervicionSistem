package com.team.gestorproyectos.cliente.application;

import com.team.gestorproyectos.cliente.domain.model.ClienteConProyectos;
import com.team.gestorproyectos.cliente.domain.model.DatosCliente;
import com.team.gestorproyectos.cliente.domain.port.in.GestionarClientesUseCase;
import com.team.gestorproyectos.cliente.domain.port.out.ClienteRepositoryPort;
import com.team.gestorproyectos.shared.application.ServicioAplicacion;
import com.team.gestorproyectos.shared.application.Transaccional;
import com.team.gestorproyectos.shared.domain.exception.AccesoDenegadoException;
import com.team.gestorproyectos.shared.domain.exception.NoEncontradoException;
import com.team.gestorproyectos.shared.domain.exception.ReglaNegocioException;
import com.team.gestorproyectos.shared.domain.port.PermisosPort;

import lombok.RequiredArgsConstructor;

@ServicioAplicacion
@RequiredArgsConstructor
public class GestionarClientesService implements GestionarClientesUseCase {

    private final ClienteRepositoryPort clientes;
    private final PermisosPort permisos;

    @Override
    @Transaccional
    public ClienteConProyectos crear(Long usuarioId, DatosCliente datos) {
        verificarPermiso(usuarioId);
        nombreCortoLibre(datos.nombreCorto(), null);
        return clientes.buscar(clientes.crear(datos)).orElseThrow();
    }

    @Override
    @Transaccional
    public ClienteConProyectos actualizar(Long usuarioId, Long clienteId, DatosCliente datos) {
        verificarPermiso(usuarioId);
        existe(clienteId);
        nombreCortoLibre(datos.nombreCorto(), clienteId);
        clientes.actualizar(clienteId, datos);
        return clientes.buscar(clienteId).orElseThrow();
    }

    @Override
    @Transaccional
    public ClienteConProyectos cambiarEstado(Long usuarioId, Long clienteId, boolean activo) {
        verificarPermiso(usuarioId);
        existe(clienteId);
        clientes.cambiarActivo(clienteId, activo);
        return clientes.buscar(clienteId).orElseThrow();
    }

    private void verificarPermiso(Long usuarioId) {
        if (!permisos.puede(usuarioId, PermisosPort.GESTIONAR_CLIENTES)) {
            throw new AccesoDenegadoException("Solo Administrador y Líder pueden crear o editar clientes");
        }
    }

    private void existe(Long clienteId) {
        clientes.buscar(clienteId).orElseThrow(() -> new NoEncontradoException("No encontramos ese cliente"));
    }

    /** El nombre corto identifica al cliente en listas y proyectos: no puede repetirse. */
    private void nombreCortoLibre(String nombreCorto, Long exceptoId) {
        if (clientes.existeNombreCorto(nombreCorto, exceptoId)) {
            throw new ReglaNegocioException("Ya hay un cliente llamado " + nombreCorto);
        }
    }
}
