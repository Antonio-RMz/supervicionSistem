package com.team.gestorproyectos.cliente.infrastructure.persistence;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.team.gestorproyectos.cliente.domain.model.ClienteConProyectos;
import com.team.gestorproyectos.cliente.domain.model.DatosCliente;
import com.team.gestorproyectos.cliente.domain.model.ProyectoDeCliente;
import com.team.gestorproyectos.cliente.domain.port.out.ClienteRepositoryPort;
import com.team.gestorproyectos.cliente.infrastructure.persistence.entity.ClienteEntity;
import com.team.gestorproyectos.cliente.infrastructure.persistence.mapper.ClientePersistenceMapper;
import com.team.gestorproyectos.cliente.infrastructure.persistence.repository.ClienteJpaRepository;
import com.team.gestorproyectos.cliente.infrastructure.persistence.repository.ClienteJpaRepository.ProyectoFila;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ClienteRepositoryAdapter implements ClienteRepositoryPort {

    private final ClienteJpaRepository repository;
    private final ClientePersistenceMapper mapper;

    @Override
    public List<ClienteConProyectos> listar() {
        return conProyectos(repository.findAllByOrderByActivoDescNombreCortoAsc());
    }

    @Override
    public Optional<ClienteConProyectos> buscar(Long clienteId) {
        return repository.findById(clienteId).map(entity -> conProyectos(List.of(entity)).get(0));
    }

    @Override
    public boolean existeNombreCorto(String nombreCorto, Long exceptoId) {
        return repository.existeNombreCorto(nombreCorto, exceptoId);
    }

    @Override
    public Long crear(DatosCliente datos) {
        ClienteEntity entity = new ClienteEntity();
        mapper.copiar(datos, entity);
        return repository.saveAndFlush(entity).getId();
    }

    @Override
    public void actualizar(Long clienteId, DatosCliente datos) {
        ClienteEntity entity = repository.findById(clienteId).orElseThrow();
        mapper.copiar(datos, entity);
        repository.flush();
    }

    @Override
    public void cambiarActivo(Long clienteId, boolean activo) {
        repository.actualizarActivo(clienteId, activo);
    }

    /** Una sola consulta para los proyectos de todos los clientes de la lista. */
    private List<ClienteConProyectos> conProyectos(List<ClienteEntity> entities) {
        if (entities.isEmpty()) {
            return List.of();
        }
        Map<Long, List<ProyectoDeCliente>> proyectos = repository
                .proyectosDe(entities.stream().map(ClienteEntity::getId).toList()).stream()
                .collect(Collectors.groupingBy(ProyectoFila::getClienteId,
                        Collectors.mapping(mapper::aProyecto, Collectors.toList())));
        return entities.stream()
                .map(e -> new ClienteConProyectos(mapper.aDominio(e), proyectos.getOrDefault(e.getId(), List.of())))
                .toList();
    }
}
