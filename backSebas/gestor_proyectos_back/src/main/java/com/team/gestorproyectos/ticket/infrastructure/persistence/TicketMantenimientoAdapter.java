package com.team.gestorproyectos.ticket.infrastructure.persistence;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.team.gestorproyectos.proyecto.infrastructure.persistence.entity.TicketEntity;
import com.team.gestorproyectos.proyecto.infrastructure.persistence.repository.TicketJpaRepository;
import com.team.gestorproyectos.ticket.domain.model.Mantenimiento.ParchePublicado;
import com.team.gestorproyectos.ticket.domain.model.Mantenimiento.TicketDeMantenimiento;
import com.team.gestorproyectos.ticket.domain.model.NuevoTicketMantenimiento;
import com.team.gestorproyectos.ticket.domain.model.PreparacionMantenimiento.HistoriaDeOrigen;
import com.team.gestorproyectos.ticket.domain.model.PreparacionTicket.ModuloDeTicket;
import com.team.gestorproyectos.ticket.domain.model.ReporteFalla;
import com.team.gestorproyectos.ticket.domain.model.TipoTicket;
import com.team.gestorproyectos.ticket.domain.port.out.TicketMantenimientoPort;
import com.team.gestorproyectos.ticket.infrastructure.persistence.repository.TicketEscrituraJpaRepository;
import com.team.gestorproyectos.ticket.infrastructure.persistence.repository.TicketMantenimientoJpaRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TicketMantenimientoAdapter implements TicketMantenimientoPort {

    private final TicketMantenimientoJpaRepository repository;
    private final TicketEscrituraJpaRepository escritura;
    private final TicketJpaRepository tickets;

    @Override
    public Optional<Long> faseMantenimiento(Long proyectoId) {
        return repository.faseMantenimiento(proyectoId);
    }

    @Override
    public List<TicketDeMantenimiento> tickets(Long faseId) {
        return repository.tickets(faseId).stream()
                .map(t -> new TicketDeMantenimiento(t.getId(), t.getTipo(), t.getNumero(), t.getTitulo(), t.getEstado(),
                        t.getSeveridad(), t.getRama(), t.getResponsableId(), t.getResponsableNombre()))
                .toList();
    }

    @Override
    public Optional<Integer> proximaFaseNumero(Long proyectoId) {
        return repository.proximaFaseNumero(proyectoId);
    }

    @Override
    public List<Long> minutosHotfix(Long proyectoId, LocalDateTime desde) {
        return repository.minutosHotfix(proyectoId, desde).stream().filter(Objects::nonNull).toList();
    }

    @Override
    public List<ParchePublicado> parches(Long proyectoId) {
        return repository.parches(proyectoId).stream()
                .map(p -> new ParchePublicado(p.getVersion(), p.getFecha(), p.getBugs().intValue())).toList();
    }

    @Override
    public List<ModuloDeTicket> modulos(Long proyectoId) {
        return repository.modulos(proyectoId).stream()
                .map(m -> new ModuloDeTicket(m.getId(), m.getTipo(), m.getNombre(), m.getTecnologia(),
                        m.getRepositorio(), m.getRamaBase()))
                .toList();
    }

    @Override
    public List<HistoriaDeOrigen> historias(Long proyectoId) {
        return repository.historias(proyectoId).stream()
                .map(h -> new HistoriaDeOrigen(h.getId(), h.getNumero(), h.getTitulo())).toList();
    }

    @Override
    public Long crear(Long proyectoId, Long faseId, int numero, NuevoTicketMantenimiento nuevo, Long usuarioId) {
        TicketEntity ticket = nuevoTicket(proyectoId, faseId, nuevo.tipo(), numero, usuarioId);
        ticket.setTitulo(nuevo.titulo());
        ticket.setDescripcion(nuevo.descripcion());
        ticket.setPrioridad(nuevo.prioridad());
        ticket.setSeveridad(nuevo.severidad());
        ticket.setModuloId(nuevo.moduloId());
        ticket.setResponsableId(nuevo.responsableId());
        return guardar(ticket, usuarioId);
    }

    @Override
    public Long reportar(Long proyectoId, Long faseId, int numero, ReporteFalla reporte, String versionReportada,
                         Long usuarioId) {
        TicketEntity ticket = nuevoTicket(proyectoId, faseId, TipoTicket.BUGS, numero, usuarioId);
        ticket.setTitulo(reporte.titulo());
        ticket.setDescripcion(reporte.descripcion());
        ticket.setPrioridad(reporte.vaPorHotfix() ? "ALTA" : "MEDIA");
        ticket.setSeveridad(reporte.severidad());
        ticket.setModuloId(reporte.moduloId());
        ticket.setHistoriaOrigenId(reporte.historiaOrigenId());
        ticket.setVersionReportada(versionReportada);
        return guardar(ticket, usuarioId);
    }

    private static TicketEntity nuevoTicket(Long proyectoId, Long faseId, TipoTicket tipo, int numero, Long usuarioId) {
        TicketEntity ticket = new TicketEntity();
        ticket.setProyectoId(proyectoId);
        ticket.setFaseId(faseId);
        ticket.setTipo(tipo.name());
        ticket.setNumero(numero);
        ticket.setEstado(tipo.estadoInicial());
        ticket.setCreadoPor(usuarioId);
        ticket.setHorasRegistradas(BigDecimal.ZERO);
        return ticket;
    }

    private Long guardar(TicketEntity ticket, Long usuarioId) {
        Long id = tickets.saveAndFlush(ticket).getId();
        escritura.registrarCreacion(id, ticket.getEstado(), usuarioId, LocalDateTime.now());
        return id;
    }
}
