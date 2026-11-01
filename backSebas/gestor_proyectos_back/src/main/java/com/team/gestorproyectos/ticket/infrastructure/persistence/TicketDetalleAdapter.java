package com.team.gestorproyectos.ticket.infrastructure.persistence;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.team.gestorproyectos.ticket.domain.model.InfoTicket;
import com.team.gestorproyectos.ticket.domain.model.InfoTicket.CambioDeEstado;
import com.team.gestorproyectos.ticket.domain.model.InfoTicket.Comentario;
import com.team.gestorproyectos.ticket.domain.model.InfoTicket.Criterio;
import com.team.gestorproyectos.ticket.domain.model.InfoTicket.Dependencia;
import com.team.gestorproyectos.ticket.domain.model.InfoTicket.Historia;
import com.team.gestorproyectos.ticket.domain.model.InfoTicket.Modulo;
import com.team.gestorproyectos.ticket.domain.model.InfoTicket.Persona;
import com.team.gestorproyectos.ticket.domain.model.InfoTicket.PullRequest;
import com.team.gestorproyectos.ticket.domain.model.InfoTicket.Subtarea;
import com.team.gestorproyectos.ticket.domain.port.out.TicketDetallePort;
import com.team.gestorproyectos.ticket.infrastructure.persistence.repository.TicketDetalleJpaRepository;
import com.team.gestorproyectos.ticket.infrastructure.persistence.repository.TicketDetalleJpaRepository.TicketFila;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TicketDetalleAdapter implements TicketDetallePort {

    private final TicketDetalleJpaRepository repository;

    @Override
    public Optional<InfoTicket> info(Long proyectoId, Long ticketId) {
        return repository.ticket(proyectoId, ticketId).map(t -> aInfo(t, ticketId));
    }

    private InfoTicket aInfo(TicketFila t, Long ticketId) {
        return new InfoTicket(
                t.getId(), t.getProyectoId(), t.getProyectoNombre(), t.getFaseId(), t.getFaseNumero(),
                t.getFaseNombre(), historia(t), t.getTipo(), t.getNumero(), t.getTitulo(), t.getDescripcion(),
                t.getEstado(), t.getPrioridad(),
                t.getCapas() == null || t.getCapas().isBlank() ? List.of()
                        : Arrays.stream(t.getCapas().split(",")).map(String::trim).toList(),
                t.getModuloTipo() == null ? null : new Modulo(t.getModuloTipo(), t.getModuloNombre(),
                        t.getModuloTecnologia(), t.getModuloRepositorio(), t.getModuloRamaBase()),
                t.getResponsableId() == null ? null : new Persona(t.getResponsableId(), t.getResponsableNombre()),
                t.getTesterId() == null ? null : new Persona(t.getTesterId(), t.getTesterNombre()),
                new Persona(t.getCreadoPorId(), t.getCreadoPorNombre()),
                t.getEstimacionHoras(), t.getHorasRegistradas(), t.getFechaLimite(), t.getRama(), t.getBloqueadoPor(),
                t.getRechazosEnQa() == null ? 0 : t.getRechazosEnQa(),
                repository.dependencias(ticketId).stream()
                        .map(d -> new Dependencia(d.getCodigo(), d.getTitulo(), d.getEstado())).toList(),
                repository.subtareas(ticketId).stream()
                        .map(s -> new Subtarea(s.getId(), s.getDescripcion(), Boolean.TRUE.equals(s.getHecha()))).toList(),
                repository.pullRequests(ticketId).stream()
                        .map(p -> new PullRequest(p.getNumero(), p.getRamaOrigen(), p.getRamaDestino(), p.getEstado(),
                                p.getCiEstado(), p.getUrl()))
                        .toList(),
                repository.criterios(ticketId).stream()
                        .map(c -> new Criterio(c.getDescripcion(), Boolean.TRUE.equals(c.getVerificado()),
                                c.getSoloEste() != null && c.getSoloEste() > 0))
                        .toList(),
                repository.historial(ticketId).stream()
                        .map(c -> new CambioDeEstado(c.getEstadoAnterior(), c.getEstado(), c.getMotivo(),
                                Boolean.TRUE.equals(c.getAutomatico()), c.getUsuario(), c.getFecha()))
                        .toList(),
                repository.comentarios(ticketId).stream()
                        .map(c -> new Comentario(c.getUsuario(), c.getTexto(), c.getFecha())).toList());
    }

    @Override
    public boolean marcarSubtarea(Long ticketId, Long subtareaId, boolean hecha) {
        return repository.marcarSubtarea(ticketId, subtareaId, hecha) > 0;
    }

    @Override
    public void agregarSubtarea(Long ticketId, String descripcion) {
        repository.agregarSubtarea(ticketId, descripcion);
    }

    @Override
    public boolean quitarSubtarea(Long ticketId, Long subtareaId) {
        return repository.quitarSubtarea(ticketId, subtareaId) > 0;
    }

    @Override
    public void comentar(Long ticketId, Long usuarioId, String texto, LocalDateTime ahora) {
        repository.comentar(ticketId, usuarioId, texto, ahora);
    }

    private static Historia historia(TicketFila t) {
        return t.getHistoriaId() == null ? null
                : new Historia(t.getHistoriaId(), t.getHistoriaNumero(), t.getHistoriaTitulo(), t.getHistoriaEstado(),
                        t.getComo(), t.getQuiero(), t.getPara());
    }
}
