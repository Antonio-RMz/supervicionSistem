package com.team.gestorproyectos.historia.infrastructure.persistence;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.team.gestorproyectos.historia.domain.model.Analista;
import com.team.gestorproyectos.historia.domain.model.FaseParaHistoria;
import com.team.gestorproyectos.historia.domain.model.FaseParaHistoria.Opcion;
import com.team.gestorproyectos.historia.domain.model.HistoriaGuardada;
import com.team.gestorproyectos.historia.domain.model.NuevaHistoria;
import com.team.gestorproyectos.historia.domain.model.PreparacionHistoria.UsuarioDelSistema;
import com.team.gestorproyectos.historia.domain.port.out.HistoriaRepositoryPort;
import com.team.gestorproyectos.historia.infrastructure.persistence.repository.HistoriaEscrituraJpaRepository;
import com.team.gestorproyectos.historia.infrastructure.persistence.repository.HistoriaEscrituraJpaRepository.OpcionFila;
import com.team.gestorproyectos.proyecto.infrastructure.persistence.entity.HistoriaEntity;
import com.team.gestorproyectos.proyecto.infrastructure.persistence.repository.HistoriaJpaRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class HistoriaRepositoryAdapter implements HistoriaRepositoryPort {

    private final HistoriaEscrituraJpaRepository repository;
    private final HistoriaJpaRepository historias;

    @Override
    public Optional<String> proyectoVisible(Long proyectoId, Long usuarioId, boolean esAdmin) {
        return repository.proyectoVisible(proyectoId, usuarioId, esAdmin);
    }

    @Override
    public int siguienteNumero(Long proyectoId) {
        return repository.siguienteNumero(proyectoId);
    }

    @Override
    public List<FaseParaHistoria> fasesAbiertas(Long proyectoId) {
        Map<Long, List<Opcion>> modulos = porFase(repository.modulos(proyectoId));
        return repository.fases(proyectoId).stream()
                .filter(f -> FaseParaHistoria.recibeHistorias(f.getEstado(), Boolean.TRUE.equals(f.getMantenimiento())))
                .map(f -> new FaseParaHistoria(f.getId(), f.getNumero(), f.getNombre(), f.getEstado(),
                        modulos.getOrDefault(f.getId(), List.of())))
                .toList();
    }

    @Override
    public List<Analista> equipo(Long proyectoId) {
        return repository.equipo(proyectoId).stream()
                .map(f -> {
                    String apellidos = f.getApellidos() == null ? "" : f.getApellidos().trim();
                    return new Analista(f.getId(), apellidos.isEmpty() ? f.getNombre() : f.getNombre() + " " + apellidos,
                            f.getRoles() == null ? List.of() : List.of(f.getRoles().split(",")));
                })
                .toList();
    }

    @Override
    public List<UsuarioDelSistema> usuariosDelSistema(Long proyectoId) {
        return repository.usuariosDelSistema(proyectoId).stream()
                .map(u -> new UsuarioDelSistema(u.getNombre(), u.getDescripcion()))
                .toList();
    }

    @Override
    public Long crear(Long proyectoId, int numero, NuevaHistoria nueva, Long usuarioId) {
        HistoriaEntity historia = new HistoriaEntity();
        historia.setProyectoId(proyectoId);
        historia.setFaseId(nueva.faseId());
        historia.setNumero(numero);
        historia.setTitulo(nueva.titulo());
        historia.setComo(nueva.como());
        historia.setQuiero(nueva.quiero());
        historia.setPara(nueva.para());
        historia.setEstado("BORRADOR");
        historia.setPrioridad(nueva.prioridad());
        historia.setPuntos(nueva.puntos());
        historia.setAnalistaId(nueva.analistaId());
        historia.setFigmaUrl(nueva.figmaUrl());
        historia.setCreadoPor(usuarioId);
        if (nueva.disenoAprobado()) {
            historia.setDisenoAprobado(true);
            historia.setDisenoAprobadoPor(usuarioId);
            historia.setDisenoAprobadoEn(LocalDateTime.now());
        }
        Long id = historias.saveAndFlush(historia).getId();
        guardarListas(id, nueva);
        repository.registrarCreacion(id, usuarioId, LocalDateTime.now());
        return id;
    }

    @Override
    public Optional<HistoriaGuardada> guardada(Long proyectoId, Long historiaId) {
        return historias.findById(historiaId)
                .filter(h -> h.getProyectoId().equals(proyectoId))
                .map(h -> new HistoriaGuardada(h.getId(), h.getNumero(), h.getEstado(), h.getFaseId(), h.getTitulo(),
                        h.getComo(), h.getQuiero(), h.getPara(), repository.criterios(historiaId),
                        h.getFigmaUrl(), h.isDisenoAprobado(),
                        h.getAnalistaId(), h.getPrioridad(), h.getPuntos(),
                        new HashSet<>(repository.modulosDeHistoria(historiaId))));
    }

    @Override
    public void actualizar(Long historiaId, NuevaHistoria cambios, HistoriaGuardada antes, Long usuarioId) {
        HistoriaEntity historia = historias.findById(historiaId).orElseThrow();
        historia.setTitulo(cambios.titulo());
        historia.setComo(cambios.como());
        historia.setQuiero(cambios.quiero());
        historia.setPara(cambios.para());
        historia.setPrioridad(cambios.prioridad());
        historia.setPuntos(cambios.puntos());
        historia.setAnalistaId(cambios.analistaId());
        historia.setFigmaUrl(cambios.figmaUrl());
        // La aprobación del diseño guarda quién y cuándo la dio; si ya estaba, se conserva.
        if (cambios.disenoAprobado() && !antes.disenoAprobado()) {
            historia.setDisenoAprobado(true);
            historia.setDisenoAprobadoPor(usuarioId);
            historia.setDisenoAprobadoEn(LocalDateTime.now());
        } else if (!cambios.disenoAprobado()) {
            historia.setDisenoAprobado(false);
            historia.setDisenoAprobadoPor(null);
            historia.setDisenoAprobadoEn(null);
        }
        historias.save(historia);

        repository.quitarCriterios(historiaId);
        repository.quitarModulos(historiaId);
        guardarListas(historiaId, cambios);
    }

    private void guardarListas(Long id, NuevaHistoria datos) {
        for (int i = 0; i < datos.criterios().size(); i++) {
            repository.agregarCriterio(id, datos.criterios().get(i), i + 1);
        }
        datos.moduloIds().forEach(moduloId -> repository.agregarModulo(id, moduloId));
    }

    private static Map<Long, List<Opcion>> porFase(List<OpcionFila> filas) {
        return filas.stream().collect(Collectors.groupingBy(OpcionFila::getFaseId,
                Collectors.mapping(f -> new Opcion(f.getId(), f.getTipo(), f.getNombre()), Collectors.toList())));
    }
}
