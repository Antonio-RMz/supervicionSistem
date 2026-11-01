package com.team.gestorproyectos.ticket.infrastructure.persistence;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.team.gestorproyectos.shared.domain.exception.ReglaNegocioException;
import com.team.gestorproyectos.ticket.domain.model.Parche;
import com.team.gestorproyectos.ticket.domain.port.out.TicketParchePort;
import com.team.gestorproyectos.ticket.infrastructure.persistence.repository.TicketParcheJpaRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TicketParcheAdapter implements TicketParchePort {

    /** version_release.resumen es VARCHAR(200). */
    private static final int LARGO_RESUMEN = 200;

    private final TicketParcheJpaRepository repository;

    @Override
    public Optional<String> versionEnProduccion(Long proyectoId) {
        return repository.versionEnProduccion(proyectoId);
    }

    @Override
    public List<Parche.Incluido> hotfixAprobados(Long proyectoId) {
        return repository.hotfixAprobados(proyectoId).stream()
                .map(f -> new Parche.Incluido(f.getId(), f.getCodigo(), f.getTitulo()))
                .toList();
    }

    @Override
    public void publicar(Long proyectoId, Parche parche, Long usuarioId, LocalDateTime ahora) {
        String motivo = "Publicado en el parche " + parche.version();
        for (Parche.Incluido bug : parche.bugs()) {
            if (repository.publicarBug(bug.id()) == 0) {
                throw new ReglaNegocioException(bug.codigo() + " cambió de estado. Recarga para ver el parche de nuevo.");
            }
            repository.registrarHistorial(bug.id(), motivo, usuarioId, ahora);
        }
        String resumen = "Parche: " + parche.bugs().stream().map(Parche.Incluido::codigo)
                .collect(Collectors.joining(", "));
        repository.registrarRelease(proyectoId, parche.version(),
                resumen.length() > LARGO_RESUMEN ? resumen.substring(0, LARGO_RESUMEN - 1) + "…" : resumen,
                usuarioId, ahora);
    }
}
