package com.team.gestorproyectos.proyecto.infrastructure.persistence;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.team.gestorproyectos.proyecto.domain.model.Detenido;
import com.team.gestorproyectos.proyecto.domain.model.InfoProyecto;
import com.team.gestorproyectos.proyecto.domain.model.Miembro;
import com.team.gestorproyectos.proyecto.domain.model.ModuloConCi;
import com.team.gestorproyectos.proyecto.domain.model.ModuloProyecto;
import com.team.gestorproyectos.proyecto.domain.model.ReglasFlujo;
import com.team.gestorproyectos.proyecto.domain.model.Salud;
import com.team.gestorproyectos.proyecto.domain.model.TipoModulo;
import com.team.gestorproyectos.proyecto.domain.port.out.ProyectoConsultaPort;
import com.team.gestorproyectos.proyecto.infrastructure.persistence.repository.ProyectoConsultaJpaRepository;
import com.team.gestorproyectos.proyecto.infrastructure.persistence.repository.ProyectoConsultaJpaRepository.MiembroFila;
import com.team.gestorproyectos.proyecto.infrastructure.persistence.repository.ProyectoConsultaJpaRepository.ModuloFila;
import com.team.gestorproyectos.proyecto.infrastructure.persistence.repository.ProyectoConsultaJpaRepository.TicketFila;
import com.team.gestorproyectos.shared.domain.model.ConteoTickets;
import com.team.gestorproyectos.shared.domain.model.ItemDeProyecto;
import com.team.gestorproyectos.shared.domain.model.ItemTrabajo;
import com.team.gestorproyectos.shared.domain.model.Persona;

import lombok.RequiredArgsConstructor;
import com.team.gestorproyectos.shared.domain.model.CodigoItem;

@Component
@RequiredArgsConstructor
public class ProyectoConsultaAdapter implements ProyectoConsultaPort {

    /** Estados que ya no cuentan como abiertos (igual que el Dashboard). */
    private static final List<String> CERRADOS = List.of(
            "CERRADA", "CERRADO", "CANCELADA", "NO_REPRODUCIBLE", "RECHAZADA", "PUBLICADA", "REVISADA");

    private final ProyectoConsultaJpaRepository repository;

    @Override
    public boolean visible(Long proyectoId, Long usuarioId, boolean esAdmin) {
        return repository.visible(proyectoId, usuarioId, esAdmin) > 0;
    }

    @Override
    public List<ProyectoBase> proyectos(Long usuarioId) {
        return repository.proyectos(usuarioId).stream()
                .map(f -> new ProyectoBase(f.getId(), f.getCodigo(), f.getNombre(), f.getEstado(), f.getPrioridad(),
                        f.getClienteId(), f.getClienteNombre(), f.getLiderNombre(), f.getFases(), f.getHistorias()))
                .toList();
    }

    @Override
    public Map<Long, List<ConteoTickets>> conteos(Collection<Long> proyectoIds) {
        if (proyectoIds.isEmpty()) {
            return Map.of();
        }
        return repository.conteos(proyectoIds).stream().collect(Collectors.groupingBy(
                ProyectoConsultaJpaRepository.ConteoFila::getProyectoId,
                Collectors.mapping(f -> new ConteoTickets(f.getFaseId(), f.getModuloId(), f.getEstado(), f.getCantidad()),
                        Collectors.toList())));
    }

    @Override
    public Map<Long, List<ModuloConCi>> modulos(Collection<Long> proyectoIds) {
        if (proyectoIds.isEmpty()) {
            return Map.of();
        }
        return repository.modulos(proyectoIds).stream().collect(Collectors.groupingBy(
                ModuloFila::getProyectoId,
                Collectors.mapping(ProyectoConsultaAdapter::aModulo, Collectors.toList())));
    }

    @Override
    public Map<Long, List<Miembro>> equipos(Collection<Long> proyectoIds) {
        if (proyectoIds.isEmpty()) {
            return Map.of();
        }
        return repository.equipos(proyectoIds).stream().collect(Collectors.groupingBy(
                MiembroFila::getProyectoId,
                Collectors.mapping(ProyectoConsultaAdapter::aMiembro, Collectors.toList())));
    }

    @Override
    public Optional<InfoProyecto> info(Long proyectoId) {
        return repository.info(proyectoId).map(f -> new InfoProyecto(f.getId(), f.getCodigo(), f.getNombre(),
                f.getDescripcion(), f.getEstado(), f.getClienteId(), f.getClienteNombre(), f.getPrioridad(),
                f.getFechaInicio(), f.getFechaEntrega(), f.getLiderId(), f.getLiderNombre(),
                new ReglasFlujo(si(f.getPermitirAutovalidacion()), si(f.getComentarioAlRechazar()),
                        si(f.getRamaAlIniciar()), si(f.getNotificarClienteFase()))));
    }

    @Override
    public List<FaseBase> fases(Long proyectoId) {
        return repository.fases(proyectoId).stream()
                .map(f -> new FaseBase(f.getId(), f.getNumero(), f.getNombre(), f.getEstado(), f.getFechaInicio(),
                        f.getFechaFin(), f.getVersionRelease(), si(f.getMantenimiento()), tipos(f.getModulos()),
                        f.getHistorias(), f.getTickets()))
                .toList();
    }

    @Override
    public MetricasTickets metricas(Long proyectoId) {
        var f = repository.metricas(proyectoId, CERRADOS);
        return new MetricasTickets(f.getTicketsAbiertos(), f.getBugsAbiertos(), f.getBugsEnProduccion(),
                f.getMantenimientoAbiertos());
    }

    @Override
    public Salud salud(Long proyectoId, LocalDateTime inicioDeMes, LocalDateTime hace30Dias) {
        var f = repository.salud(proyectoId, inicioDeMes, hace30Dias);
        return new Salud(f.getRechazosQaMes(), f.getDiasQa() == null ? null : f.getDiasQa().doubleValue(),
                f.getHorasEstimadas(), f.getHorasReales(), f.getHistoriasSinTickets());
    }

    @Override
    public List<Detenido> detenidos(Long proyectoId, LocalDateTime enQaDesdeAntesDe) {
        return repository.detenidos(proyectoId, enQaDesdeAntesDe).stream()
                .map(f -> new Detenido(f.getTicketId(), CodigoItem.de(f.getTipo(), f.getNumero()),
                        f.getTitulo(), f.getEstado(), f.getDesde(), f.getMotivo()))
                .toList();
    }

    @Override
    public List<HistoriaBase> historias(Long proyectoId) {
        return repository.historias(proyectoId).stream()
                .map(f -> new HistoriaBase(f.getId(), f.getFaseId(), f.getNumero(), f.getTitulo(), f.getEstado(),
                        f.getAnalistaId(), f.getAnalistaIniciales()))
                .toList();
    }

    @Override
    public List<TicketBase> tickets(Long proyectoId) {
        return repository.tickets(proyectoId).stream().map(ProyectoConsultaAdapter::aTicket).toList();
    }

    @Override
    public List<Persona> personasDelEquipo() {
        return repository.personasDelEquipo().stream().map(f -> new Persona(f.getId(), f.getNombre())).toList();
    }

    private static ModuloConCi aModulo(ModuloFila f) {
        return new ModuloConCi(
                new ModuloProyecto(f.getId(), TipoModulo.valueOf(f.getTipo()), f.getNombre(), f.getTecnologia(),
                        f.getRepositorio(), f.getRamaBase()),
                f.getCiEstado(), f.getCiFecha());
    }

    /** "JS" para Jorge Sebastián: inicial del nombre y del primer apellido (igual que en Usuarios). */
    private static Miembro aMiembro(MiembroFila f) {
        String apellidos = f.getApellidos() == null ? "" : f.getApellidos().trim();
        String nombreCompleto = apellidos.isEmpty() ? f.getNombre() : f.getNombre() + " " + apellidos;
        String iniciales = (f.getNombre().substring(0, 1) + (apellidos.isEmpty() ? "" : apellidos.substring(0, 1)))
                .toUpperCase();
        List<String> roles = f.getRoles() == null ? List.of() : List.of(f.getRoles().split(","));
        return new Miembro(f.getId(), nombreCompleto, iniciales, roles);
    }

    private static TicketBase aTicket(TicketFila f) {
        ItemTrabajo item = new ItemTrabajo(f.getId(), f.getTipo(), f.getNumero(), f.getTitulo(), f.getEstado(),
                f.getPrioridad(), f.getResponsableId(), f.getTesterId(), f.getCreadoPorId(), f.getHistoriaEstado(),
                f.getCreadoEn(), f.getActualizadoEn());
        return new TicketBase(new ItemDeProyecto(item, f.getLiderId(), f.getUltimoEstadoAnterior()),
                f.getFaseId(), f.getHistoriaId(),
                f.getModuloTipo() == null ? null : TipoModulo.valueOf(f.getModuloTipo()),
                f.getResponsableIniciales());
    }

    /** "API,WEB" -> [API, WEB] */
    private static List<TipoModulo> tipos(String texto) {
        return texto == null || texto.isBlank() ? List.of()
                : Arrays.stream(texto.split(",")).map(TipoModulo::valueOf).toList();
    }

    private static boolean si(Boolean valor) {
        return Boolean.TRUE.equals(valor);
    }
}
