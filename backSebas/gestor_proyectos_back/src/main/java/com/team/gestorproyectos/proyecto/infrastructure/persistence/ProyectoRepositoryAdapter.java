package com.team.gestorproyectos.proyecto.infrastructure.persistence;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.team.gestorproyectos.proyecto.domain.model.ConfiguracionProyecto;
import com.team.gestorproyectos.proyecto.domain.model.DatosProyecto;
import com.team.gestorproyectos.proyecto.domain.model.EstadoProyecto;
import com.team.gestorproyectos.proyecto.domain.model.ModuloProyecto;
import com.team.gestorproyectos.proyecto.domain.model.ProyectoResumen;
import com.team.gestorproyectos.proyecto.domain.model.ReglasFlujo;
import com.team.gestorproyectos.proyecto.domain.model.TipoModulo;
import com.team.gestorproyectos.proyecto.domain.port.out.ProyectoRepositoryPort;
import com.team.gestorproyectos.proyecto.infrastructure.persistence.entity.FaseEntity;
import com.team.gestorproyectos.proyecto.infrastructure.persistence.entity.ModuloEntity;
import com.team.gestorproyectos.proyecto.infrastructure.persistence.entity.ProyectoEntity;
import com.team.gestorproyectos.proyecto.infrastructure.persistence.mapper.ProyectoPersistenceMapper;
import com.team.gestorproyectos.proyecto.infrastructure.persistence.repository.FaseJpaRepository;
import com.team.gestorproyectos.proyecto.infrastructure.persistence.repository.ModuloJpaRepository;
import com.team.gestorproyectos.proyecto.infrastructure.persistence.repository.ProyectoJpaRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ProyectoRepositoryAdapter implements ProyectoRepositoryPort {

    private final ProyectoJpaRepository repository;
    private final ModuloJpaRepository modulos;
    private final FaseJpaRepository fases;
    private final ProyectoPersistenceMapper mapper;

    @Override
    public List<ProyectoResumen> todos() {
        return mapper.aResumenes(repository.listarResumenes(null));
    }

    @Override
    public List<ProyectoResumen> dondeEsMiembro(Long usuarioId) {
        return mapper.aResumenes(repository.listarResumenes(usuarioId));
    }

    @Override
    public Set<String> codigos() {
        return repository.codigos();
    }

    @Override
    public boolean clienteActivo(Long clienteId) {
        return repository.clienteActivo(clienteId) > 0;
    }

    @Override
    public Set<Long> personasActivas(Collection<Long> usuarioIds) {
        return usuarioIds.isEmpty() ? Set.of() : repository.personasActivas(usuarioIds);
    }

    @Override
    public boolean esLider(Long usuarioId) {
        return repository.esLider(usuarioId) > 0;
    }

    @Override
    public Long crear(DatosProyecto datos, String codigo) {
        ProyectoEntity proyecto = new ProyectoEntity();
        proyecto.setCodigo(codigo);
        copiar(datos, proyecto);
        Long id = repository.saveAndFlush(proyecto).getId();

        datos.miembroIds().forEach(usuarioId -> repository.agregarMiembro(id, usuarioId));
        datos.modulos().forEach(m -> modulos.save(nuevoModulo(id, m)));

        // Fase permanente donde viven los hotfix de lo que ya está en producción.
        FaseEntity mantenimiento = new FaseEntity();
        mantenimiento.setProyectoId(id);
        mantenimiento.setNumero(0);
        mantenimiento.setNombre("Mantenimiento");
        mantenimiento.setObjetivo("Correcciones urgentes (hotfix) de lo que ya está en producción.");
        mantenimiento.setEstado("EN_CURSO");
        mantenimiento.setPrioridad("ALTA");
        mantenimiento.setResponsableId(datos.liderId());
        mantenimiento.setEsMantenimiento(true);
        fases.save(mantenimiento);
        return id;
    }

    @Override
    public Optional<ConfiguracionProyecto> configuracion(Long proyectoId) {
        return repository.findById(proyectoId).map(p -> new ConfiguracionProyecto(p.getId(), p.getCodigo(),
                new DatosProyecto(p.getNombre(), p.getClienteId(), p.getPrioridad(),
                        EstadoProyecto.valueOf(p.getEstado()), p.getFechaInicio(), p.getFechaEntrega(),
                        p.getDescripcion(), p.getLiderId(), repository.miembros(proyectoId),
                        modulos.findByProyectoIdOrderById(proyectoId).stream()
                                .map(m -> new ModuloProyecto(m.getId(), TipoModulo.valueOf(m.getTipo()), m.getNombre(),
                                        m.getTecnologia(), m.getRepositorio(), m.getRamaBase()))
                                .toList(),
                        new ReglasFlujo(p.isPermitirAutovalidacion(), p.isComentarioAlRechazar(), p.isRamaAlIniciar(),
                                p.isNotificarClienteFase()))));
    }

    @Override
    public Set<Long> modulosConTrabajo(Long proyectoId) {
        return repository.modulosConTrabajo(proyectoId);
    }

    @Override
    public void actualizar(Long proyectoId, DatosProyecto datos) {
        ProyectoEntity proyecto = repository.findById(proyectoId).orElseThrow();
        copiar(datos, proyecto);

        // Equipo: se quitan los que ya no están y se agregan los nuevos.
        Set<Long> actuales = repository.miembros(proyectoId);
        Set<Long> salen = new HashSet<>(actuales);
        salen.removeAll(datos.miembroIds());
        if (!salen.isEmpty()) {
            repository.quitarMiembros(proyectoId, salen);
        }
        datos.miembroIds().stream().filter(id -> !actuales.contains(id))
                .forEach(id -> repository.agregarMiembro(proyectoId, id));

        // Módulos: los que traen id se actualizan, los nuevos se crean y los que faltan se borran
        // (el servicio ya revisó que no tengan trabajo).
        Map<Long, ModuloEntity> existentes = modulos.findByProyectoIdOrderById(proyectoId).stream()
                .collect(Collectors.toMap(ModuloEntity::getId, Function.identity()));
        Set<Long> quedan = new HashSet<>();
        for (ModuloProyecto m : datos.modulos()) {
            if (m.id() == null) {
                modulos.save(nuevoModulo(proyectoId, m));
            } else {
                quedan.add(m.id());
                copiar(m, existentes.get(m.id()));
            }
        }
        existentes.values().stream().filter(e -> !quedan.contains(e.getId())).forEach(modulos::delete);
        repository.flush();
    }

    private static void copiar(DatosProyecto datos, ProyectoEntity proyecto) {
        proyecto.setNombre(datos.nombre());
        proyecto.setDescripcion(datos.descripcion());
        proyecto.setClienteId(datos.clienteId());
        proyecto.setPrioridad(datos.prioridad());
        proyecto.setEstado(datos.estado().name());
        proyecto.setFechaInicio(datos.fechaInicio());
        proyecto.setFechaEntrega(datos.fechaEntrega());
        proyecto.setLiderId(datos.liderId());
        ReglasFlujo reglas = datos.reglas();
        proyecto.setPermitirAutovalidacion(reglas.permitirAutovalidacion());
        proyecto.setComentarioAlRechazar(reglas.comentarioAlRechazar());
        proyecto.setRamaAlIniciar(reglas.ramaAlIniciar());
        proyecto.setNotificarClienteFase(reglas.notificarClienteFase());
    }

    private static ModuloEntity nuevoModulo(Long proyectoId, ModuloProyecto m) {
        ModuloEntity entity = new ModuloEntity();
        entity.setProyectoId(proyectoId);
        copiar(m, entity);
        return entity;
    }

    private static void copiar(ModuloProyecto m, ModuloEntity entity) {
        entity.setTipo(m.tipo().name());
        entity.setNombre(m.nombre());
        entity.setTecnologia(m.tecnologia());
        entity.setRepositorio(m.repositorio());
        entity.setRamaBase(m.ramaBase());
    }
}
