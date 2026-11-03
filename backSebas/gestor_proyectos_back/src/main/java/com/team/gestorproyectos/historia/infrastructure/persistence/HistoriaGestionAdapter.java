package com.team.gestorproyectos.historia.infrastructure.persistence;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.team.gestorproyectos.historia.domain.model.CambioHistoria;
import com.team.gestorproyectos.historia.domain.model.DesarrolladorDeHistoria;
import com.team.gestorproyectos.historia.domain.model.RepositorioDeHistoria;
import com.team.gestorproyectos.historia.domain.model.TrabajoDeHistoria;
import com.team.gestorproyectos.historia.domain.model.CambioHistoria.BugDeDevolucion;
import com.team.gestorproyectos.historia.domain.model.HistoriaParaGestionar;
import com.team.gestorproyectos.historia.domain.model.HistoriaParaGestionar.FaseDestino;
import com.team.gestorproyectos.historia.domain.model.HistoriaParaIntegrar;
import com.team.gestorproyectos.historia.domain.model.HistoriaParaIntegrar.HistoriaDeFase;
import com.team.gestorproyectos.historia.domain.model.HistoriaParaIntegrar.Modulo;
import com.team.gestorproyectos.historia.domain.model.HistoriaParaIntegrar.Publicada;
import com.team.gestorproyectos.historia.domain.model.HistoriaParaIntegrar.PullRequest;
import com.team.gestorproyectos.historia.domain.model.HistoriaParaIntegrar.TicketAIntegrar;
import com.team.gestorproyectos.historia.domain.model.ImagenGuardada;
import com.team.gestorproyectos.historia.domain.model.InfoHistoria;
import com.team.gestorproyectos.historia.domain.model.InfoHistoria.Adjunto;
import com.team.gestorproyectos.historia.domain.model.InfoHistoria.CambioDeEstado;
import com.team.gestorproyectos.historia.domain.model.InfoHistoria.Comentario;
import com.team.gestorproyectos.historia.domain.model.InfoHistoria.Criterio;
import com.team.gestorproyectos.historia.domain.model.InfoHistoria.Ticket;
import com.team.gestorproyectos.historia.domain.model.NuevaImagen;
import com.team.gestorproyectos.historia.domain.model.TicketDeHistoria;
import com.team.gestorproyectos.historia.domain.port.out.HistoriaGestionPort;
import com.team.gestorproyectos.historia.infrastructure.persistence.repository.HistoriaGestionJpaRepository;
import com.team.gestorproyectos.historia.infrastructure.persistence.repository.HistoriaGestionJpaRepository.AceptacionFila;
import com.team.gestorproyectos.historia.infrastructure.persistence.repository.HistoriaGestionJpaRepository.HistoriaFila;
import com.team.gestorproyectos.proyecto.infrastructure.persistence.entity.TicketEntity;
import com.team.gestorproyectos.proyecto.infrastructure.persistence.repository.TicketJpaRepository;
import com.team.gestorproyectos.shared.domain.exception.ReglaNegocioException;

import lombok.RequiredArgsConstructor;
import com.team.gestorproyectos.shared.domain.model.CodigoItem;
import com.team.gestorproyectos.shared.domain.model.FormaTrabajo;

@Component
@RequiredArgsConstructor
public class HistoriaGestionAdapter implements HistoriaGestionPort {

    private final HistoriaGestionJpaRepository repository;
    private final TicketJpaRepository tickets;

    @Override
    public List<Long> historiasPorDecidir(Long proyectoId) {
        return repository.historiasPorDecidir(proyectoId);
    }

    @Override
    public Optional<HistoriaParaGestionar> buscarParaGestionar(Long proyectoId, Long historiaId) {
        return repository.historia(proyectoId, historiaId).map(h -> new HistoriaParaGestionar(
                h.getId(), h.getProyectoId(), h.getNumero(), h.getTitulo(), h.getEstado(), h.getAnalistaId(),
                h.getLiderId(), h.getFaseId(), h.getFaseEstado(),
                lleno(h.getComo()) && lleno(h.getQuiero()) && lleno(h.getPara()),
                h.getCriterios().intValue(), h.getModulos().intValue(),
                repository.tickets(historiaId).stream()
                        .map(t -> new TicketDeHistoria(t.getId(), t.getTipo(), t.getNumero(), t.getTitulo(),
                                t.getEstado(), t.getResponsableId(), t.getTesterId()))
                        .toList(),
                repository.fasesDestino(proyectoId, h.getFaseId(), historiaId).stream()
                        .map(f -> new FaseDestino(f.getId(), f.getTitulo())).toList(),
                FormaTrabajo.valueOf(h.getFormaTrabajo()),
                !Boolean.FALSE.equals(h.getRequierePr()),
                desarrolladores(historiaId),
                (int) repository.trabajo(historiaId).stream().filter(t -> !Boolean.TRUE.equals(t.getHecho())).count()));
    }

    @Override
    public InfoHistoria info(Long proyectoId, Long historiaId) {
        HistoriaFila h = repository.historia(proyectoId, historiaId).orElseThrow();
        return new InfoHistoria(h.getId(), h.getProyectoId(), h.getProyectoNombre(),
                FormaTrabajo.valueOf(h.getFormaTrabajo()), h.getFaseId(), h.getFaseNumero(),
                h.getFaseNombre(), h.getFaseEstado(), h.getFaseInicio(), h.getFaseFin(), h.getNumero(), h.getTitulo(),
                h.getComo(), h.getQuiero(), h.getPara(), h.getEstado(), h.getPrioridad(), h.getPuntos(),
                h.getAnalistaNombre(), h.getFigmaUrl(),
                h.getDisenoDeFase() != null && h.getDisenoDeFase().intValue() != 0,
                Boolean.TRUE.equals(h.getDisenoAprobado()),
                h.getDisenoAprobadoPor(), h.getDisenoAprobadoEn(),
                repository.modulos(historiaId),
                repository.criterios(historiaId).stream()
                        .map(c -> new Criterio(c.getId(), c.getDescripcion(), Boolean.TRUE.equals(c.getVerificado()),
                                lista(c.getTickets())))
                        .toList(),
                repository.tickets(historiaId).stream()
                        .map(t -> new Ticket(t.getId(), t.getTipo(), t.getNumero(), t.getTitulo(), t.getEstado(),
                                t.getModuloTipo(), t.getResponsableNombre(), lista(t.getDependeDe())))
                        .toList(),
                repository.historial(historiaId).stream()
                        .map(c -> new CambioDeEstado(c.getEstado(), c.getMotivo(), Boolean.TRUE.equals(c.getAutomatico()),
                                c.getUsuario(), c.getFecha()))
                        .toList(),
                repository.adjuntos(historiaId).stream()
                        .map(a -> new Adjunto(a.getId(), a.getNombre(), a.getTipoMime(), a.getTamanoBytes())).toList(),
                repository.comentarios(historiaId).stream()
                        .map(c -> new Comentario(c.getUsuario(), c.getTexto(), c.getFecha())).toList());
    }

    @Override
    public HistoriaParaIntegrar paraIntegrar(Long proyectoId, Long historiaId) {
        HistoriaFila h = repository.historia(proyectoId, historiaId).orElseThrow();
        Optional<AceptacionFila> aceptacion = repository.aceptacion(historiaId);
        return new HistoriaParaIntegrar(
                CodigoItem.historia(h.getNumero()), h.getEstado(), h.getFaseNumero(),
                repository.versionDeFase(h.getFaseId()),
                aceptacion.map(AceptacionFila::getUsuario).orElse(null),
                aceptacion.map(AceptacionFila::getFecha).orElse(null),
                repository.ticketsConPr(historiaId).stream()
                        .map(t -> new TicketAIntegrar(t.getId(), t.getTipo(), t.getNumero(), t.getTitulo(),
                                t.getEstado(), t.getModuloId(), t.getRama(),
                                t.getPrNumero() == null ? null : new PullRequest(t.getPrNumero(), t.getPrEstado(),
                                        t.getPrCi(), Boolean.TRUE.equals(t.getPrConConflictos()), t.getPrRevisadoEn())))
                        .toList(),
                repository.modulosDeTickets(historiaId, h.getFaseId()).stream()
                        .map(m -> new Modulo(m.getId(), m.getTipo(), m.getNombre(), m.getTecnologia(),
                                m.getIntegradas().intValue()))
                        .toList(),
                repository.historiasDeFase(h.getFaseId()).stream()
                        .map(f -> new HistoriaDeFase(f.getId(), f.getNumero(), f.getEstado())).toList(),
                repository.ultimaIntegracion(h.getFaseId()),
                repository.enProduccion(proyectoId).map(p -> new Publicada(p.getVersion(), p.getFecha())).orElse(null));
    }

    @Override
    public String guardar(CambioHistoria c, Long usuarioId, LocalDateTime ahora) {
        if (!c.estadoNuevo().equals(c.estadoAnterior())) {
            if (repository.cambiarEstado(c.historiaId(), c.estadoAnterior(), c.estadoNuevo()) == 0) {
                throw new ReglaNegocioException("Alguien más movió esta historia al mismo tiempo. Recarga para ver cómo está.");
            }
            repository.registrarHistorial("HISTORIA", c.historiaId(), c.estadoAnterior(), c.estadoNuevo(), c.motivo(),
                    false, usuarioId, ahora);
        }

        String bug = null;
        switch (c.accion()) {
            case DESCARTAR -> {
                String motivo = "Se descartó la historia: " + c.motivo();
                repository.registrarCancelacion(c.historiaId(), motivo, usuarioId, ahora);
                repository.cancelarSinEmpezar(c.historiaId());
            }
            case DEVOLVER -> {
                if (c.ticketAReabrir() != null) {
                    String anterior = repository.estadoDeTicket(c.ticketAReabrir());
                    repository.reabrirTicket(c.historiaId(), c.ticketAReabrir(), c.estadoAlReabrir());
                    repository.registrarHistorial("TICKET", c.ticketAReabrir(), anterior, c.estadoAlReabrir(),
                            c.motivo(), false, usuarioId, ahora);
                } else if (c.bug() != null) {
                    bug = crearBug(c, c.bug(), usuarioId, ahora);
                } else {
                    // Con Desarrolladores: regresa a ellos y cada uno vuelve a marcar su parte.
                    repository.reiniciarPartes(c.historiaId());
                }
            }
            case PEDIR_CAMBIOS -> repository.reiniciarPartes(c.historiaId());
            case INTEGRAR -> integrarTickets(c.historiaId(), "Se integró a develop", usuarioId, ahora);
            case MOVER_DE_FASE -> {
                repository.cambiarFase(c.historiaId(), c.faseDestinoId());
                repository.moverTicketsDeFase(c.historiaId(), c.faseDestinoId());
            }
            default -> {
                // Marcar como Lista, regresar a Borrador, iniciar, aceptar y reabrir solo cambian el estado.
            }
        }
        if (c.avanzaSolaA() != null) {
            // Ya tenía una Actividad Lista: pasa sola, igual que al marcar Lista la Actividad. En la forma Normal,
            // aceptada ya queda integrada: su código ya está en develop.
            boolean integrada = "INTEGRADA".equals(c.avanzaSolaA());
            repository.cambiarEstado(c.historiaId(), c.estadoNuevo(), c.avanzaSolaA());
            repository.registrarHistorial("HISTORIA", c.historiaId(), c.estadoNuevo(), c.avanzaSolaA(),
                    integrada ? "Su código ya está en develop"
                            : repository.desarrolladores(c.historiaId()).isEmpty() ? "Ya tiene una Actividad Lista"
                            : "Lista para sus Desarrolladores",
                    true, usuarioId, ahora);
            if (integrada) {
                integrarTickets(c.historiaId(), "Su historia se aceptó", usuarioId, ahora);
            }
        }
        return bug;
    }

    /** Sus tickets aprobados pasan a Integrados y sus PR quedan mezclados. */
    private void integrarTickets(Long historiaId, String motivo, Long usuarioId, LocalDateTime ahora) {
        TicketDeHistoria.alIntegrar().forEach((desde, hacia) -> {
            repository.registrarTickets(historiaId, desde, hacia, motivo, usuarioId, ahora);
            repository.moverTickets(historiaId, desde, hacia);
        });
        repository.integrarPullRequests(historiaId);
    }

    @Override
    public List<Long> criterios(Long historiaId) {
        return repository.idsDeCriterios(historiaId);
    }

    @Override
    public void verificarCriterio(Long criterioId, boolean verificado) {
        repository.verificarCriterio(criterioId, verificado);
    }

    @Override
    public void comentar(Long historiaId, Long usuarioId, String texto, LocalDateTime ahora) {
        repository.comentar(historiaId, usuarioId, texto, ahora);
    }

    @Override
    public List<ImagenGuardada> imagenes(Long historiaId) {
        return repository.adjuntos(historiaId).stream()
                .map(a -> new ImagenGuardada(a.getId(), a.getNombre(), a.getTipoMime(), a.getRuta())).toList();
    }

    @Override
    public void agregarImagen(Long historiaId, NuevaImagen imagen, String ruta, Long usuarioId) {
        repository.agregarAdjunto(historiaId, imagen.nombre(), imagen.tipoMime(), imagen.tamanoBytes(), ruta, usuarioId);
    }

    @Override
    public void quitarImagen(Long imagenId) {
        repository.quitarAdjunto(imagenId);
    }

    @Override
    public long ticketsQueVienenDe(Long historiaId) {
        return repository.ticketsQueVienenDe(historiaId);
    }

    @Override
    public List<DesarrolladorDeHistoria> desarrolladores(Long historiaId) {
        return repository.desarrolladores(historiaId).stream()
                .map(d -> new DesarrolladorDeHistoria(d.getUsuarioId(), d.getNombre(), d.getModuloId(),
                        d.getModuloNombre(), Boolean.TRUE.equals(d.getTerminado())))
                .toList();
    }

    @Override
    public List<TrabajoDeHistoria> trabajo(Long historiaId) {
        return repository.trabajo(historiaId).stream()
                .map(t -> new TrabajoDeHistoria(t.getId(), t.getDescripcion(), t.getModuloId(), t.getModuloNombre(),
                        Boolean.TRUE.equals(t.getHecho()), t.getCreadoPor(), t.getCreadoEn()))
                .toList();
    }

    @Override
    public List<RepositorioDeHistoria> repositorios(Long historiaId) {
        return repository.repositorios(historiaId).stream()
                .map(r -> new RepositorioDeHistoria(r.getId(), r.getTipo(), r.getNombre(), r.getTecnologia(),
                        r.getRamaBase(), r.getUrl()))
                .toList();
    }

    @Override
    public void marcarParte(Long historiaId, Long usuarioId, boolean terminado) {
        repository.marcarParte(historiaId, usuarioId, terminado);
    }

    @Override
    public void agregarTrabajo(Long historiaId, String descripcion, Long moduloId, Long usuarioId,
                               LocalDateTime ahora) {
        repository.agregarTrabajo(historiaId, descripcion, moduloId, usuarioId, ahora);
    }

    @Override
    public boolean marcarTrabajo(Long historiaId, Long trabajoId, boolean hecho) {
        return repository.marcarTrabajo(historiaId, trabajoId, hecho) > 0;
    }

    @Override
    public boolean quitarTrabajo(Long historiaId, Long trabajoId) {
        return repository.quitarTrabajo(historiaId, trabajoId) > 0;
    }

    @Override
    public void eliminar(Long historiaId) {
        repository.quitarTodoElTrabajo(historiaId);
        repository.quitarDesarrolladores(historiaId);
        repository.quitarAdjuntos(historiaId);
        repository.quitarComentarios(historiaId);
        repository.quitarHistorial(historiaId);
        repository.quitarCriterios(historiaId);
        repository.quitarModulos(historiaId);
        repository.eliminarHistoria(historiaId);
    }

    /** El BUGS que nace al devolver por algo que no cubría ningún ticket (PDF pág. 33). */
    private String crearBug(CambioHistoria c, BugDeDevolucion datos, Long usuarioId, LocalDateTime ahora) {
        int numero = repository.siguienteNumeroBug(c.proyectoId());
        TicketEntity ticket = new TicketEntity();
        ticket.setProyectoId(c.proyectoId());
        ticket.setFaseId(c.faseId());
        ticket.setHistoriaId(c.historiaId());
        ticket.setTipo("BUGS");
        ticket.setNumero(numero);
        ticket.setTitulo(datos.titulo());
        ticket.setDescripcion(datos.descripcion());
        ticket.setEstado("CONFIRMADO");
        ticket.setPrioridad("MEDIA");
        ticket.setModuloId(repository.primerModulo(c.historiaId()));
        ticket.setResponsableId(datos.responsableId());
        ticket.setTesterId(datos.testerId());
        ticket.setCreadoPor(usuarioId);
        ticket.setHorasRegistradas(BigDecimal.ZERO);
        Long id = tickets.saveAndFlush(ticket).getId();
        repository.registrarHistorial("TICKET", id, null, "CONFIRMADO", c.motivo(), false, usuarioId, ahora);
        return String.format("BUGS-%02d", numero);
    }

    private static boolean lleno(String texto) {
        return texto != null && !texto.isBlank();
    }

    /** "ACTV-41,ACTV-42" -> [ACTV-41, ACTV-42] */
    private static List<String> lista(String texto) {
        return texto == null || texto.isBlank() ? List.of() : Arrays.asList(texto.split(","));
    }
}
