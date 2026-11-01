package com.team.gestorproyectos.proyecto.infrastructure.persistence;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.team.gestorproyectos.proyecto.domain.model.ModuloConCi;
import com.team.gestorproyectos.proyecto.domain.model.ModuloProyecto;
import com.team.gestorproyectos.proyecto.domain.model.ModulosProyecto.Ambiente;
import com.team.gestorproyectos.proyecto.domain.model.ModulosProyecto.DatosModulo;
import com.team.gestorproyectos.proyecto.domain.model.ModulosProyecto.Responsable;
import com.team.gestorproyectos.proyecto.domain.model.TipoModulo;
import com.team.gestorproyectos.proyecto.domain.port.out.ModulosProyectoPort;
import com.team.gestorproyectos.proyecto.infrastructure.persistence.entity.ModuloEntity;
import com.team.gestorproyectos.proyecto.infrastructure.persistence.repository.ModuloJpaRepository;
import com.team.gestorproyectos.proyecto.infrastructure.persistence.repository.ModulosProyectoJpaRepository;
import com.team.gestorproyectos.proyecto.infrastructure.persistence.repository.ModulosProyectoJpaRepository.ConteoFila;
import com.team.gestorproyectos.proyecto.infrastructure.persistence.repository.ModulosProyectoJpaRepository.IntegracionFila;
import com.team.gestorproyectos.shared.domain.model.ConteoTickets;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ModulosProyectoAdapter implements ModulosProyectoPort {

    private final ModulosProyectoJpaRepository repository;
    private final ModuloJpaRepository modulos;

    @Override
    public List<DatosModulo> modulos(Long proyectoId) {
        List<ConteoFila> conteos = repository.conteos(proyectoId);
        Map<Long, IntegracionFila> integraciones = repository.faseEnCurso(proyectoId)
                .map(f -> repository.integraciones(f.getId())).orElse(List.of()).stream()
                .collect(Collectors.toMap(IntegracionFila::getModuloId, Function.identity()));
        return repository.modulos(proyectoId).stream().map(m -> {
            IntegracionFila integracion = integraciones.get(m.getId());
            return new DatosModulo(
                    new ModuloConCi(new ModuloProyecto(m.getId(), TipoModulo.valueOf(m.getTipo()), m.getNombre(),
                            m.getTecnologia(), m.getRepositorio(), m.getRamaBase()), m.getCiEstado(), m.getCiFecha()),
                    m.getResponsableId() == null ? null : new Responsable(m.getResponsableId(), m.getResponsableNombre()),
                    conteosDe(conteos, m.getId(), false),
                    conteosDe(conteos, m.getId(), true),
                    integracion == null ? 0 : integracion.getIntegradas().intValue(),
                    integracion == null ? null : integracion.getUltima(),
                    m.getConTrabajo() != null && m.getConTrabajo() > 0);
        }).toList();
    }

    @Override
    public Optional<String> versionFaseEnCurso(Long proyectoId) {
        return repository.faseEnCurso(proyectoId).map(ModulosProyectoJpaRepository.FaseFila::getVersion);
    }

    @Override
    public Optional<Ambiente> produccion(Long proyectoId) {
        return repository.produccion(proyectoId).map(p -> new Ambiente("producción", p.getVersion(), p.getFecha()));
    }

    @Override
    public Long guardar(Long proyectoId, ModuloProyecto m, Long responsableId) {
        ModuloEntity entity = m.id() == null ? new ModuloEntity() : modulos.findById(m.id()).orElseThrow();
        entity.setProyectoId(proyectoId);
        entity.setTipo(m.tipo().name());
        entity.setNombre(m.nombre());
        entity.setTecnologia(m.tecnologia());
        entity.setRepositorio(m.repositorio());
        entity.setRamaBase(m.ramaBase());
        entity.setResponsableId(responsableId);
        return modulos.save(entity).getId();
    }

    @Override
    public void quitar(Long proyectoId, Long moduloId) {
        modulos.findById(moduloId).filter(m -> m.getProyectoId().equals(proyectoId)).ifPresent(modulos::delete);
    }

    private static List<ConteoTickets> conteosDe(List<ConteoFila> conteos, Long moduloId, boolean mantenimiento) {
        return conteos.stream()
                .filter(c -> moduloId.equals(c.getModuloId()) && mantenimiento == Boolean.TRUE.equals(c.getMantenimiento()))
                .map(c -> new ConteoTickets(null, moduloId, c.getEstado(), c.getCantidad()))
                .toList();
    }
}
