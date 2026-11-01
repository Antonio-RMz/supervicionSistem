package com.team.gestorproyectos.fase.infrastructure.persistence;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.team.gestorproyectos.fase.domain.model.CriterioAutomatico;
import com.team.gestorproyectos.fase.domain.model.DisenoFase;
import com.team.gestorproyectos.fase.domain.model.FaseExistente;
import com.team.gestorproyectos.fase.domain.model.FaseGuardada;
import com.team.gestorproyectos.fase.domain.model.ModuloOpcion;
import com.team.gestorproyectos.fase.domain.model.NuevaFase;
import com.team.gestorproyectos.fase.domain.model.Responsable;
import com.team.gestorproyectos.fase.domain.port.out.FaseRepositoryPort;
import com.team.gestorproyectos.fase.infrastructure.persistence.repository.FaseEscrituraJpaRepository;
import com.team.gestorproyectos.fase.infrastructure.persistence.repository.FaseEscrituraJpaRepository.PersonaFila;
import com.team.gestorproyectos.proyecto.infrastructure.persistence.entity.FaseEntity;
import com.team.gestorproyectos.proyecto.infrastructure.persistence.repository.FaseJpaRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class FaseRepositoryAdapter implements FaseRepositoryPort {

    private static final String PLANEADA = "PLANEADA";

    private final FaseEscrituraJpaRepository repository;
    private final FaseJpaRepository fases;

    @Override
    public Optional<ProyectoDeFase> proyectoVisible(Long proyectoId, Long usuarioId, boolean esAdmin) {
        return repository.proyectoVisible(proyectoId, usuarioId, esAdmin)
                .map(f -> new ProyectoDeFase(f.getId(), f.getNombre(), f.getLiderId()));
    }

    @Override
    public List<FaseExistente> fases(Long proyectoId) {
        return repository.fases(proyectoId).stream()
                .map(f -> new FaseExistente(f.getNumero(), f.getNombre(), f.getEstado(), f.getFechaFin(),
                        f.getVersionRelease(), Boolean.TRUE.equals(f.getMantenimiento())))
                .toList();
    }

    @Override
    public List<ModuloOpcion> modulos(Long proyectoId) {
        return repository.modulos(proyectoId).stream()
                .map(f -> new ModuloOpcion(f.getId(), f.getTipo(), f.getNombre(), f.getTecnologia()))
                .toList();
    }

    @Override
    public List<Responsable> equipo(Long proyectoId) {
        return repository.equipo(proyectoId).stream().map(FaseRepositoryAdapter::aResponsable).toList();
    }

    @Override
    public Long crear(Long proyectoId, int numero, NuevaFase nueva, Long usuarioId) {
        FaseEntity fase = new FaseEntity();
        fase.setProyectoId(proyectoId);
        fase.setNumero(numero);
        fase.setNombre(nueva.nombre());
        fase.setObjetivo(nueva.objetivo());
        fase.setEstado(PLANEADA);
        fase.setPrioridad(nueva.prioridad());
        fase.setFechaInicio(nueva.fechaInicio());
        fase.setFechaFin(nueva.fechaFin());
        fase.setResponsableId(nueva.responsableId());
        fase.setVersionRelease(nueva.versionRelease());
        Long id = fases.saveAndFlush(fase).getId();

        nueva.moduloIds().forEach(moduloId -> repository.agregarModulo(id, moduloId));
        for (CriterioAutomatico criterio : CriterioAutomatico.values()) {
            repository.agregarCriterio(id, criterio.descripcion(), "AUTOMATICO", criterio.name());
        }
        nueva.criteriosManuales().forEach(texto -> repository.agregarCriterio(id, texto, "MANUAL", null));
        repository.registrarCreacion(id, PLANEADA, usuarioId, LocalDateTime.now());
        return id;
    }

    @Override
    public Optional<FaseGuardada> guardada(Long proyectoId, Long faseId) {
        return repository.faseGuardada(proyectoId, faseId)
                .map(f -> new FaseGuardada(f.getId(), f.getNumero(), f.getEstado(), f.getNombre(), f.getObjetivo(),
                        f.getFechaInicio(), f.getFechaFin(), f.getResponsableId(), f.getVersionRelease(),
                        f.getPrioridad(), new HashSet<>(repository.modulosDeFase(faseId)),
                        repository.criteriosManuales(faseId), new HashSet<>(repository.modulosEnUso(faseId))));
    }

    @Override
    public void guardarDiseno(Long faseId, DisenoFase diseno) {
        repository.guardarDiseno(faseId, diseno.figmaUrl(), diseno.paraTodas());
    }

    @Override
    public void actualizar(Long faseId, NuevaFase cambios, FaseGuardada antes) {
        FaseEntity fase = fases.findById(faseId).orElseThrow();
        fase.setNombre(cambios.nombre());
        fase.setObjetivo(cambios.objetivo());
        fase.setPrioridad(cambios.prioridad());
        fase.setFechaInicio(cambios.fechaInicio());
        fase.setFechaFin(cambios.fechaFin());
        fase.setResponsableId(cambios.responsableId());
        fase.setVersionRelease(cambios.versionRelease());
        fases.save(fase);

        antes.moduloIds().stream().filter(id -> !cambios.moduloIds().contains(id))
                .forEach(id -> repository.quitarModulo(faseId, id));
        cambios.moduloIds().stream().filter(id -> !antes.moduloIds().contains(id))
                .forEach(id -> repository.agregarModulo(faseId, id));
        // Un criterio que se queda conserva si ya estaba cumplido.
        antes.criteriosManuales().stream().filter(t -> !cambios.criteriosManuales().contains(t))
                .forEach(t -> repository.quitarCriterioManual(faseId, t));
        cambios.criteriosManuales().stream().filter(t -> !antes.criteriosManuales().contains(t))
                .forEach(t -> repository.agregarCriterio(faseId, t, "MANUAL", null));
    }

    private static Responsable aResponsable(PersonaFila f) {
        String apellidos = f.getApellidos() == null ? "" : f.getApellidos().trim();
        String iniciales = (f.getNombre().substring(0, 1) + (apellidos.isEmpty() ? "" : apellidos.substring(0, 1)))
                .toUpperCase();
        return new Responsable(f.getId(), apellidos.isEmpty() ? f.getNombre() : f.getNombre() + " " + apellidos,
                iniciales, f.getRoles() == null ? List.of() : List.of(f.getRoles().split(",")));
    }
}
