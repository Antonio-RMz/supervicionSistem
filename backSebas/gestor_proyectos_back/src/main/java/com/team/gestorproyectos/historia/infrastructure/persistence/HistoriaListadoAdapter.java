package com.team.gestorproyectos.historia.infrastructure.persistence;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.team.gestorproyectos.historia.domain.model.HistoriasDeFase;
import com.team.gestorproyectos.historia.domain.model.ListadoHistorias.HistoriaDeLista;
import com.team.gestorproyectos.historia.domain.port.out.HistoriaListadoPort;
import com.team.gestorproyectos.historia.infrastructure.persistence.repository.HistoriaListadoJpaRepository;
import com.team.gestorproyectos.historia.infrastructure.persistence.repository.HistoriaListadoJpaRepository.CriterioFila;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class HistoriaListadoAdapter implements HistoriaListadoPort {

    private final HistoriaListadoJpaRepository repository;

    @Override
    public List<HistoriaDeLista> historias(Long proyectoId) {
        return repository.historias(proyectoId).stream()
                .map(h -> new HistoriaDeLista(h.getId(), h.getNumero(), h.getTitulo(), h.getEstado(), h.getPrioridad(),
                        h.getPuntos(), h.getFaseId(), h.getFaseNumero(), h.getFaseNombre(), h.getAnalistaNombre(),
                        h.getTicketsTerminados().intValue(), h.getTicketsTotal().intValue(), h.getActualizadoEn()))
                .toList();
    }

    @Override
    public Optional<HistoriasDeFase> deFase(Long proyectoId, Long faseId) {
        return repository.fase(proyectoId, faseId).map(f -> {
            Map<Long, List<String>> criterios = repository.criteriosDeFase(faseId).stream()
                    .collect(Collectors.groupingBy(CriterioFila::getHistoriaId,
                            Collectors.mapping(CriterioFila::getDescripcion, Collectors.toList())));
            return new HistoriasDeFase(f.getProyectoNombre(), f.getNumero(), f.getNombre(),
                    repository.historiasDeFase(faseId).stream()
                            .map(h -> new HistoriasDeFase.Historia(h.getNumero(), h.getEstado(), h.getTitulo(),
                                    h.getComo(), h.getQuiero(), h.getPara(), h.getPrioridad(), h.getPuntos(),
                                    h.getFigmaUrl(), criterios.getOrDefault(h.getId(), List.of())))
                            .toList());
        });
    }
}
