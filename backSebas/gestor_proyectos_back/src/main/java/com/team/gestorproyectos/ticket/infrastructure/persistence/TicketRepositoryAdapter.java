package com.team.gestorproyectos.ticket.infrastructure.persistence;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.team.gestorproyectos.shared.domain.exception.ReglaNegocioException;
import com.team.gestorproyectos.shared.domain.model.AvanceHistoria.TicketEstado;
import com.team.gestorproyectos.shared.domain.model.FlujoTrabajo;
import com.team.gestorproyectos.shared.domain.model.ItemTrabajo;
import com.team.gestorproyectos.ticket.domain.model.TicketParaMover;
import com.team.gestorproyectos.ticket.domain.model.Transicion;
import com.team.gestorproyectos.ticket.domain.port.out.TicketRepositoryPort;
import com.team.gestorproyectos.ticket.infrastructure.persistence.repository.TicketMovimientoJpaRepository;
import com.team.gestorproyectos.ticket.infrastructure.persistence.repository.TicketMovimientoJpaRepository.TicketParaMoverFila;

import lombok.RequiredArgsConstructor;
import com.team.gestorproyectos.shared.domain.model.CodigoItem;

@Component
@RequiredArgsConstructor
public class TicketRepositoryAdapter implements TicketRepositoryPort {

    private final TicketMovimientoJpaRepository repository;

    @Override
    public Optional<TicketParaMover> buscarParaMover(Long ticketId) {
        return repository.buscar(ticketId).map(fila -> new TicketParaMover(
                aItem(fila),
                fila.getProyectoId(),
                fila.getLiderId(),
                fila.getHistoriaNumero() == null ? null : CodigoItem.historia(fila.getHistoriaNumero()),
                fila.getFaseEstado(),
                Boolean.TRUE.equals(fila.getFaseMantenimiento()),
                fila.getSeveridad(),
                fila.getRama(),
                Boolean.TRUE.equals(fila.getPermitirAutovalidacion()),
                Boolean.TRUE.equals(fila.getComentarioAlRechazar()),
                Boolean.TRUE.equals(fila.getRamaAlIniciar()),
                repository.dependenciasAbiertas(ticketId)));
    }

    @Override
    public void guardar(Transicion t, Long usuarioId) {
        int cambiados = repository.cambiarEstado(t.ticketId(), t.estadoAnterior(), t.estadoNuevo(), t.ramaNueva(),
                t.bloqueadoPorId(), t.severidad(), t.responsableId());
        if (cambiados == 0) {
            throw new ReglaNegocioException("Alguien más movió este ticket al mismo tiempo. Recarga para ver cómo está.");
        }
        repository.registrarHistorial(t.ticketId(), t.estadoAnterior(), t.estadoNuevo(), t.motivo(), usuarioId,
                LocalDateTime.now());
    }

    @Override
    public void historiaConActividadLista(Long historiaId, Long usuarioId) {
        repository.registrarHistoriaConActividadLista(historiaId, usuarioId, LocalDateTime.now());
        repository.historiaConActividadLista(historiaId);
    }

    @Override
    public Optional<String> estadoDelTicket(Long proyectoId, Long ticketId) {
        return repository.estadoDelTicket(proyectoId, ticketId);
    }

    @Override
    public Optional<String> nombreDe(Long usuarioId) {
        return repository.nombreDe(usuarioId);
    }

    @Override
    public boolean esDesarrolladorDelEquipo(Long proyectoId, Long usuarioId) {
        return repository.esDesarrolladorDelEquipo(proyectoId, usuarioId) > 0;
    }

    @Override
    public void desbloquearLosQueEspera(Long ticketId, String codigo) {
        LocalDateTime ahora = LocalDateTime.now();
        repository.bloqueadosPor(ticketId).forEach(b -> {
            String nuevo = FlujoTrabajo.estadoDeTrabajo(b.getTipo());
            if (repository.cambiarEstado(b.getId(), "BLOQUEADA", nuevo, null, null, null, null) > 0) {
                repository.registrarDesbloqueo(b.getId(), nuevo, "Desbloqueado: " + codigo + " se terminó", ahora);
            }
        });
    }

    @Override
    public Optional<HistoriaDelTicket> historiaDe(Long ticketId) {
        return repository.historiaDe(ticketId).map(h -> new HistoriaDelTicket(h.getId(), h.getEstado(),
                repository.ticketsDeHistoria(h.getId()).stream()
                        .map(t -> new TicketEstado(t.getTipo(), t.getEstado())).toList()));
    }

    @Override
    public void avanzarHistoria(Long historiaId, String anterior, String nuevo, String motivo) {
        if (repository.cambiarEstadoHistoria(historiaId, anterior, nuevo) > 0) {
            repository.registrarHistorialHistoria(historiaId, anterior, nuevo, motivo, LocalDateTime.now());
        }
    }

    private static ItemTrabajo aItem(TicketParaMoverFila f) {
        return new ItemTrabajo(f.getId(), f.getTipo(), f.getNumero(), f.getTitulo(), f.getEstado(), f.getPrioridad(),
                f.getResponsableId(), f.getTesterId(), f.getCreadoPorId(), f.getHistoriaEstado(),
                f.getCreadoEn(), f.getActualizadoEn());
    }
}
