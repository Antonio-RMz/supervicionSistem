package com.team.gestorproyectos.ticket.infrastructure.persistence;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.team.gestorproyectos.proyecto.infrastructure.persistence.entity.TicketEntity;
import com.team.gestorproyectos.proyecto.infrastructure.persistence.repository.TicketJpaRepository;
import com.team.gestorproyectos.ticket.domain.model.NuevoTicket;
import com.team.gestorproyectos.ticket.domain.model.PreparacionTicket.CriterioDeHistoria;
import com.team.gestorproyectos.ticket.domain.model.PreparacionTicket.FaseDeTicket;
import com.team.gestorproyectos.ticket.domain.model.PreparacionTicket.HistoriaDeTicket;
import com.team.gestorproyectos.ticket.domain.model.PreparacionTicket.ModuloDeTicket;
import com.team.gestorproyectos.ticket.domain.model.PreparacionTicket.PersonaDeEquipo;
import com.team.gestorproyectos.ticket.domain.model.PreparacionTicket.TicketAbierto;
import com.team.gestorproyectos.ticket.domain.model.TicketEditable;
import com.team.gestorproyectos.ticket.domain.model.TipoTicket;
import com.team.gestorproyectos.ticket.domain.port.out.TicketEscrituraPort;
import com.team.gestorproyectos.ticket.infrastructure.persistence.repository.TicketEscrituraJpaRepository;
import com.team.gestorproyectos.ticket.infrastructure.persistence.repository.TicketEscrituraJpaRepository.CriterioFila;
import com.team.gestorproyectos.ticket.infrastructure.persistence.repository.TicketEscrituraJpaRepository.HistoriaFila;
import com.team.gestorproyectos.ticket.infrastructure.persistence.repository.TicketEscrituraJpaRepository.ModuloFila;

import lombok.RequiredArgsConstructor;
import com.team.gestorproyectos.shared.domain.model.CodigoItem;

@Component
@RequiredArgsConstructor
public class TicketEscrituraAdapter implements TicketEscrituraPort {

    private final TicketEscrituraJpaRepository repository;
    private final TicketJpaRepository tickets;

    @Override
    public Optional<ProyectoDeTicket> proyectoVisible(Long proyectoId, Long usuarioId, boolean esAdmin) {
        return repository.proyectoVisible(proyectoId, usuarioId, esAdmin)
                .map(f -> new ProyectoDeTicket(f.getId(), f.getNombre(), f.getLiderId()));
    }

    @Override
    public List<FaseDeTicket> fasesConHistorias(Long proyectoId) {
        Map<Long, List<ModuloDeTicket>> modulos = repository.modulos(proyectoId).stream()
                .collect(Collectors.groupingBy(ModuloFila::getFaseId, Collectors.mapping(
                        m -> new ModuloDeTicket(m.getId(), m.getTipo(), m.getNombre(), m.getTecnologia(),
                                m.getRepositorio(), m.getRamaBase()),
                        Collectors.toList())));
        Map<Long, List<CriterioDeHistoria>> criterios = repository.criterios(proyectoId).stream()
                .collect(Collectors.groupingBy(CriterioFila::getHistoriaId, Collectors.mapping(
                        c -> new CriterioDeHistoria(c.getId(), c.getDescripcion(), c.getCubiertoPor()),
                        Collectors.toList())));
        Map<Long, List<HistoriaDeTicket>> historias = repository.historias(proyectoId).stream()
                .collect(Collectors.groupingBy(HistoriaFila::getFaseId, Collectors.mapping(
                        h -> new HistoriaDeTicket(h.getId(), h.getNumero(), h.getTitulo(), h.getEstado(), h.getComo(),
                                h.getQuiero(), h.getPara(), h.getAnalistaNombre(), ids(h.getModulos()), h.getTickets(),
                                criterios.getOrDefault(h.getId(), List.of())),
                        Collectors.toList())));
        return repository.fases(proyectoId).stream()
                .map(f -> new FaseDeTicket(f.getId(), f.getNumero(), f.getNombre(),
                        modulos.getOrDefault(f.getId(), List.of()), historias.getOrDefault(f.getId(), List.of())))
                .toList();
    }

    @Override
    public List<PersonaDeEquipo> equipo(Long proyectoId) {
        return repository.equipo(proyectoId).stream()
                .map(f -> {
                    String apellidos = f.getApellidos() == null ? "" : f.getApellidos().trim();
                    return new PersonaDeEquipo(f.getId(),
                            apellidos.isEmpty() ? f.getNombre() : f.getNombre() + " " + apellidos,
                            f.getRoles() == null ? List.of() : List.of(f.getRoles().split(",")));
                })
                .toList();
    }

    /** El número siguiente de cada tipo; un tipo sin tickets empieza en 1. */
    @Override
    public Map<TipoTicket, Integer> siguientesNumeros(Long proyectoId) {
        Map<TipoTicket, Integer> siguientes = new EnumMap<>(TipoTicket.class);
        for (TipoTicket tipo : TipoTicket.values()) {
            siguientes.put(tipo, 1);
        }
        repository.maximos(proyectoId).forEach(f -> siguientes.put(TipoTicket.valueOf(f.getTipo()), f.getMaximo() + 1));
        return siguientes;
    }

    @Override
    public List<TicketAbierto> abiertos(Long proyectoId) {
        return repository.abiertos(proyectoId).stream()
                .map(f -> new TicketAbierto(f.getId(), CodigoItem.de(f.getTipo(), f.getNumero()),
                        f.getTitulo(), f.getEstado()))
                .toList();
    }

    @Override
    public Long crear(Long proyectoId, int numero, NuevoTicket nuevo, Long usuarioId) {
        TicketEntity ticket = new TicketEntity();
        ticket.setProyectoId(proyectoId);
        ticket.setFaseId(nuevo.faseId());
        ticket.setHistoriaId(nuevo.historiaId());
        ticket.setTipo(nuevo.tipo().name());
        ticket.setNumero(numero);
        ticket.setTitulo(nuevo.titulo());
        ticket.setDescripcion(nuevo.descripcion());
        ticket.setEstado(nuevo.tipo().estadoInicial());
        ticket.setPrioridad(nuevo.prioridad());
        ticket.setModuloId(nuevo.moduloId());
        ticket.setResponsableId(nuevo.responsableId());
        ticket.setTesterId(nuevo.testerId());
        ticket.setCreadoPor(usuarioId);
        ticket.setEstimacionHoras(nuevo.estimacionHoras());
        ticket.setHorasRegistradas(BigDecimal.ZERO);
        ticket.setFechaLimite(nuevo.fechaLimite());
        ticket.setCapas(nuevo.capasComoTexto());
        Long id = tickets.saveAndFlush(ticket).getId();

        nuevo.criterioIds().forEach(criterioId -> repository.agregarCriterio(id, criterioId));
        for (int i = 0; i < nuevo.subtareas().size(); i++) {
            repository.agregarSubtarea(id, nuevo.subtareas().get(i), i + 1);
        }
        nuevo.dependeDe().forEach(dependeDe -> repository.agregarDependencia(id, dependeDe));
        repository.registrarCreacion(id, nuevo.tipo().estadoInicial(), usuarioId, LocalDateTime.now());
        return id;
    }

    @Override
    public Optional<TicketEditable> editable(Long proyectoId, Long ticketId) {
        return repository.editable(proyectoId, ticketId).map(f -> {
            TipoTicket tipo = TipoTicket.valueOf(f.getTipo());
            Set<String> capas = f.getCapas() == null || f.getCapas().isBlank() ? Set.of()
                    : Arrays.stream(f.getCapas().split(",")).map(String::trim).collect(Collectors.toSet());
            NuevoTicket datos = new NuevoTicket(f.getFaseId(), f.getHistoriaId(), tipo, f.getTitulo(),
                    f.getDescripcion(), f.getModuloId(), capas, ids(f.getCriterioIds()), List.of(),
                    ids(f.getDependeDe()), f.getResponsableId(), f.getTesterId(), f.getPrioridad(),
                    f.getEstimacionHoras(), f.getFechaLimite());
            return new TicketEditable(f.getId(), CodigoItem.de(f.getTipo(), f.getNumero()), f.getEstado(),
                    f.getCreadoPorId(), f.getLiderId(), datos);
        });
    }

    @Override
    public void actualizar(Long ticketId, NuevoTicket datos) {
        repository.actualizar(ticketId, datos.titulo(), datos.descripcion(), datos.moduloId(), datos.capasComoTexto(),
                datos.responsableId(), datos.testerId(), datos.prioridad(), datos.estimacionHoras(),
                datos.fechaLimite());
        repository.quitarCriterios(ticketId);
        datos.criterioIds().forEach(criterioId -> repository.agregarCriterio(ticketId, criterioId));
        repository.quitarDependencias(ticketId);
        datos.dependeDe().forEach(dependeDe -> repository.agregarDependencia(ticketId, dependeDe));
    }

    /** "1001,1003" -> {1001, 1003} */
    private static Set<Long> ids(String texto) {
        return texto == null || texto.isBlank() ? Set.of()
                : Arrays.stream(texto.split(",")).map(Long::valueOf).collect(Collectors.toSet());
    }
}
