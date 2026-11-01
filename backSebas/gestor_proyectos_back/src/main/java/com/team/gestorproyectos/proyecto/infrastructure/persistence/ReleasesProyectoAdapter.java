package com.team.gestorproyectos.proyecto.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.team.gestorproyectos.proyecto.domain.model.ReleasesProyecto.BugDelParche;
import com.team.gestorproyectos.proyecto.domain.model.ReleasesProyecto.HistoriaLista;
import com.team.gestorproyectos.proyecto.domain.model.ReleasesProyecto.Publicado;
import com.team.gestorproyectos.proyecto.domain.port.out.ReleasesProyectoPort;
import com.team.gestorproyectos.proyecto.infrastructure.persistence.repository.ReleasesProyectoJpaRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ReleasesProyectoAdapter implements ReleasesProyectoPort {

    private final ReleasesProyectoJpaRepository repository;

    @Override
    public List<HistoriaLista> historiasAceptadas(Long proyectoId) {
        return repository.historiasAceptadas(proyectoId).stream()
                .map(h -> new HistoriaLista(h.getId(), h.getNumero(), h.getTitulo())).toList();
    }

    @Override
    public Optional<Long> proximaFase(Long proyectoId) {
        return repository.proximaFase(proyectoId);
    }

    @Override
    public Optional<Long> faseMantenimiento(Long proyectoId) {
        return repository.faseMantenimiento(proyectoId);
    }

    @Override
    public HotfixAbiertos hotfixAbiertos(Long proyectoId) {
        var fila = repository.hotfixAbiertos(proyectoId);
        return new HotfixAbiertos(fila.getAprobados().intValue(), fila.getEnCamino().intValue());
    }

    @Override
    public List<Publicado> publicados(Long proyectoId) {
        return repository.publicados(proyectoId).stream().map(r -> {
            boolean parche = "PARCHE".equals(r.getTipo());
            List<BugDelParche> bugs = parche
                    ? repository.bugsDelParche(proyectoId, r.getVersion()).stream()
                            .map(b -> new BugDelParche(b.getId(), b.getCodigo(), b.getTitulo())).toList()
                    : List.of();
            return new Publicado(r.getId(), r.getVersion(), r.getTipo(), r.getEstado(), r.getFaseId(), r.getFaseNumero(),
                    r.getFaseNombre(), r.getFecha(), parche ? 0 : r.getHistorias().intValue(),
                    parche ? 0 : r.getTickets().intValue(), bugs, r.getPublicadoPor(), false);
        }).toList();
    }
}
