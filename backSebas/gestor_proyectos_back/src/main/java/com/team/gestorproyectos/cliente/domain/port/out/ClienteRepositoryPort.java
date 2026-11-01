package com.team.gestorproyectos.cliente.domain.port.out;

import java.util.List;
import java.util.Optional;

import com.team.gestorproyectos.cliente.domain.model.ClienteConProyectos;
import com.team.gestorproyectos.cliente.domain.model.DatosCliente;

public interface ClienteRepositoryPort {

    /** Activos primero, luego por nombre corto. */
    List<ClienteConProyectos> listar();

    Optional<ClienteConProyectos> buscar(Long clienteId);

    /** @param exceptoId el propio cliente al editarlo; null al crear */
    boolean existeNombreCorto(String nombreCorto, Long exceptoId);

    Long crear(DatosCliente datos);

    void actualizar(Long clienteId, DatosCliente datos);

    void cambiarActivo(Long clienteId, boolean activo);
}
