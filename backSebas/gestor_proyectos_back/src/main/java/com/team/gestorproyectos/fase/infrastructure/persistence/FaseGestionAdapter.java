package com.team.gestorproyectos.fase.infrastructure.persistence;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;


import com.team.gestorproyectos.fase.domain.model.AccionFase;
import com.team.gestorproyectos.fase.domain.model.BloqueosEliminarFase;
import com.team.gestorproyectos.fase.domain.model.CambioFase;
import com.team.gestorproyectos.fase.domain.model.CriterioCierre;
import com.team.gestorproyectos.fase.domain.model.FaseParaGestionar;
import com.team.gestorproyectos.fase.domain.model.FaseParaGestionar.BugReversion;
import com.team.gestorproyectos.fase.domain.model.InfoFase;
import com.team.gestorproyectos.fase.domain.model.InfoFase.HistoriaDeFase;
import com.team.gestorproyectos.fase.domain.model.InfoFase.Impacto;
import com.team.gestorproyectos.fase.domain.model.InfoFase.PersonaDeFase;
import com.team.gestorproyectos.fase.domain.model.InfoFase.TicketsEnEstado;
import com.team.gestorproyectos.fase.domain.model.InfoRelease;
import com.team.gestorproyectos.fase.domain.model.NotasRelease;
import com.team.gestorproyectos.fase.domain.port.out.FaseGestionPort;
import com.team.gestorproyectos.fase.infrastructure.persistence.mapper.FasePersistenceMapper;
import com.team.gestorproyectos.fase.infrastructure.persistence.repository.FaseEscrituraJpaRepository;
import com.team.gestorproyectos.fase.infrastructure.persistence.repository.FaseGestionJpaRepository;
import com.team.gestorproyectos.fase.infrastructure.persistence.repository.FaseGestionJpaRepository.BloqueosFila;
import com.team.gestorproyectos.fase.infrastructure.persistence.repository.FaseGestionJpaRepository.ConteosFila;
import com.team.gestorproyectos.fase.infrastructure.persistence.repository.FaseGestionJpaRepository.FaseFila;
import com.team.gestorproyectos.fase.infrastructure.persistence.repository.FaseGestionJpaRepository.ReleaseFila;
import com.team.gestorproyectos.fase.infrastructure.persistence.repository.FaseGestionJpaRepository.TicketsFila;
import com.team.gestorproyectos.proyecto.infrastructure.persistence.entity.TicketEntity;
import com.team.gestorproyectos.proyecto.infrastructure.persistence.repository.TicketJpaRepository;
import com.team.gestorproyectos.shared.domain.exception.ReglaNegocioException;
import com.team.gestorproyectos.shared.domain.model.CodigoItem;
import com.team.gestorproyectos.shared.domain.model.ConteoTickets;
import com.team.gestorproyectos.shared.domain.model.Progreso;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class FaseGestionAdapter implements FaseGestionPort {

    private static final int ACTIVIDAD = 5;

    /** Lo que ya llegó a develop: al cancelar la fase se queda como está. */
    private static final Set<String> HISTORIAS_QUE_SE_QUEDAN = Set.of("INTEGRADA", "CERRADA", "DESCARTADA");
    private static final Set<String> HISTORIAS_A_CANCELAR = Set.of(
            "BORRADOR", "EN_PLANEACION", "LISTA", "LISTA_DESARROLLO", "EN_DESARROLLO", "EN_VALIDACION",
            "ACEPTADA");
    private static final Set<String> TICKETS_QUE_SE_QUEDAN = Set.of(
            "INTEGRADA", "INTEGRADO", "PUBLICADA", "PUBLICADO", "CERRADA", "CERRADO", "REVISADA",
            "CANCELADA", "NO_REPRODUCIBLE", "RECHAZADA");

    /** El BUGS de la reversión ya está resuelto cuando pasó QA; "Publicar de nuevo" es lo que lo lleva a producción. */
    private static final Set<String> BUG_RESUELTO = Set.of("APROBADO", "INTEGRADO", "PUBLICADO", "CERRADO",
            "NO_REPRODUCIBLE");

    private final FaseGestionJpaRepository repository;
    private final FaseEscrituraJpaRepository escritura;
    private final TicketJpaRepository tickets;
    private final FasePersistenceMapper mapper;

    @Override
    public List<Long> fases(Long proyectoId) {
        return repository.fases(proyectoId);
    }

    @Override
    public Optional<FaseParaGestionar> buscarParaGestionar(Long proyectoId, Long faseId) {
        return repository.fase(proyectoId, faseId).map(f -> {
            List<ReleaseFila> publicados = repository.publicados(proyectoId);
            boolean ultimaPublicada = !publicados.isEmpty() && faseId.equals(publicados.get(0).getFaseId());
            // La anterior es la más reciente publicada que no sea esta fase.
            String anterior = publicados.stream().filter(r -> !faseId.equals(r.getFaseId()))
                    .map(ReleaseFila::getVersion).findFirst().orElse(null);
            ConteosFila c = repository.conteos(faseId);
            CriterioCierre.Conteos conteos = new CriterioCierre.Conteos(c.getHistorias(), c.getHistoriasAceptadas(),
                    c.getBugsAltaAbiertos());
            List<CriterioCierre> criterios = repository.criterios(faseId).stream()
                    .map(k -> CriterioCierre.evaluar(k.getId(), k.getDescripcion(), k.getCodigo(),
                            Boolean.TRUE.equals(k.getCumplido()), k.getCumplidoPor(), k.getCumplidoEn(), conteos))
                    .toList();
            BugReversion bug = f.getBugId() == null ? null
                    : new BugReversion(f.getBugId(), codigo(f.getBugTipo(), f.getBugNumero()),
                            !BUG_RESUELTO.contains(f.getBugEstado()));
            Set<Long> equipo = escritura.equipo(proyectoId).stream()
                    .map(FaseEscrituraJpaRepository.PersonaFila::getId).collect(Collectors.toSet());
            return new FaseParaGestionar(f.getId(), f.getProyectoId(), f.getNumero(), f.getNombre(), f.getEstado(),
                    Boolean.TRUE.equals(f.getMantenimiento()), f.getLiderId(), f.getResponsableId(),
                    f.getVersionRelease(), anterior, ultimaPublicada,
                    repository.otraFaseActiva(proyectoId, faseId).orElse(null), c.getHistorias(),
                    c.getHistoriasListas(), c.getTickets(), criterios,
                    bug, equipo);
        });
    }

    @Override
    public InfoFase info(Long proyectoId, Long faseId) {
        FaseFila f = repository.fase(proyectoId, faseId).orElseThrow();
        List<TicketsFila> filas = repository.tickets(faseId);
        List<TicketsEnEstado> porEstado = filas.stream()
                .collect(Collectors.groupingBy(t -> t.getTipo() + ":" + t.getEstado(),
                        Collectors.summingLong(TicketsFila::getCantidad)))
                .entrySet().stream()
                .map(e -> new TicketsEnEstado(e.getKey().split(":")[0], e.getKey().split(":")[1], e.getValue()))
                .toList();
        Map<Long, List<ConteoTickets>> porHistoria = filas.stream()
                .filter(t -> t.getHistoriaId() != null)
                .collect(Collectors.groupingBy(TicketsFila::getHistoriaId, Collectors.mapping(
                        t -> new ConteoTickets(faseId, null, t.getEstado(), t.getCantidad()), Collectors.toList())));
        List<HistoriaDeFase> historias = repository.historias(faseId).stream()
                .map(h -> {
                    Progreso p = Progreso.de(porHistoria.getOrDefault(h.getId(), List.of()));
                    return new HistoriaDeFase(h.getId(), h.getNumero(), h.getTitulo(), h.getEstado(), p.terminados(),
                            p.total(), iniciales(h.getAnalistaNombre(), h.getAnalistaApellidos()),
                            verdadero(h.getIncompleta()), verdadero(h.getDisenoPropio()),
                            verdadero(h.getSinTickets()));
                })
                .toList();
        var ultimo = repository.ultimoCambio(faseId, f.getEstado());
        List<PersonaDeFase> equipo = escritura.equipo(f.getProyectoId()).stream()
                .map(p -> new PersonaDeFase(p.getId(), nombreCompleto(p.getNombre(), p.getApellidos()),
                        p.getRoles() == null ? List.of() : List.of(p.getRoles().split(","))))
                .toList();

        return new InfoFase(f.getId(), f.getProyectoId(), f.getProyectoNombre(), f.getProyectoCodigo(),
                f.getClienteNombre(), f.getNumero(), f.getNombre(), f.getObjetivo(), f.getEstado(), f.getPrioridad(),
                f.getFechaInicio(), f.getFechaFin(), f.getResponsableNombre(), f.getVersionRelease(),
                Boolean.TRUE.equals(f.getMantenimiento()), f.getFigmaUrl(), !Boolean.FALSE.equals(f.getFigmaParaTodas()),
                f.getMotivoEstado(), f.getReanudarEl(),
                ultimo.map(FaseGestionJpaRepository.CambioFila::getFecha).orElse(null),
                ultimo.map(FaseGestionJpaRepository.CambioFila::getUsuario).orElse(null),
                mapper.aModulos(repository.modulos(faseId)),
                porEstado,
                historias,
                mapper.aMovimientos(repository.actividad(faseId, ACTIVIDAD)),
                new Impacto(repository.contarHistorias(faseId, HISTORIAS_A_CANCELAR),
                        repository.contarTicketsFuera(faseId, TICKETS_QUE_SE_QUEDAN)),
                equipo);
    }

    @Override
    public String guardar(CambioFase c, Long usuarioId, LocalDateTime ahora) {
        int cambiadas = repository.cambiarEstado(c.faseId(), c.estadoAnterior(), c.estadoNuevo(), c.motivo(),
                c.reanudarEl());
        if (cambiadas == 0) {
            throw new ReglaNegocioException("Alguien más movió esta fase al mismo tiempo. Recarga para ver cómo está.");
        }
        repository.registrarHistorial("FASE", c.faseId(), c.estadoAnterior(), c.estadoNuevo(), c.motivo(), false,
                usuarioId, ahora);

        String bug = null;
        switch (c.accion()) {
            case INICIAR, REANUDAR -> {
                // Las que ya tienen sus Actividades arrancan con la fase; las demás siguen planeándose
                // y arrancan solas al terminar sus Actividades.
                repository.registrarHistoriasListas(c.faseId(), usuarioId, ahora);
                repository.iniciarHistoriasListas(c.faseId());
            }
            case CANCELAR -> {
                String motivo = "Se canceló la fase: " + c.motivo();
                moverHistorias(c.faseId(), HISTORIAS_A_CANCELAR, "DESCARTADA", motivo, usuarioId, ahora);
                repository.registrarTickets(c.faseId(), TICKETS_QUE_SE_QUEDAN, true, "CANCELADA", motivo, usuarioId, ahora);
                repository.moverTickets(c.faseId(), TICKETS_QUE_SE_QUEDAN, true, "CANCELADA");
            }
            case SUBIR_A_MAIN, PUBLICAR_DE_NUEVO ->
                    repository.prepararRelease(c.proyectoId(), c.faseId(), c.version(), nombreDeFase(c));
            case DESPLEGAR -> {
                repository.cambiarRelease(c.proyectoId(), c.version(), "PUBLICADA", usuarioId, ahora);
                // Al publicar, lo integrado se cierra solo; un bug queda Publicado hasta que el Analista lo verifique.
                String motivo = "Publicado en " + c.version();
                moverHistorias(c.faseId(), Set.of("INTEGRADA"), "CERRADA", motivo, usuarioId, ahora);
                moverTickets(c.faseId(), Set.of("INTEGRADA"), "CERRADA", motivo, usuarioId, ahora);
                moverTickets(c.faseId(), Set.of("INTEGRADO"), "PUBLICADO", motivo, usuarioId, ahora);
            }
            case REVERTIR -> {
                repository.cambiarRelease(c.proyectoId(), c.version(), "REVERTIDA", usuarioId, ahora);
                moverHistorias(c.faseId(), Set.of("CERRADA"), "INTEGRADA", "Se revirtió " + c.version(), usuarioId, ahora);
                bug = crearBugDeReversion(c, usuarioId, ahora);
            }
            default -> {
                // Pausar, reanudar, cerrar y reabrir solo cambian el estado de la fase.
            }
        }
        return bug;
    }

    @Override
    public InfoRelease release(Long proyectoId, Long faseId) {
        String version = repository.fase(proyectoId, faseId).map(FaseFila::getVersionRelease).orElseThrow();
        var release = repository.release(proyectoId, version);
        return new InfoRelease(
                version,
                release.map(FaseGestionJpaRepository.DatosReleaseFila::getEstado).orElse(null),
                release.map(FaseGestionJpaRepository.DatosReleaseFila::getPublicadoEn).orElse(null),
                release.map(FaseGestionJpaRepository.DatosReleaseFila::getPublicadoPor).orElse(null),
                repository.enProduccion(proyectoId)
                        .map(v -> new InfoRelease.VersionPublicada(v.getVersion(), v.getFecha())).orElse(null),
                repository.liderNombre(proyectoId),
                repository.modulosDelRelease(faseId).stream()
                        .map(m -> new InfoRelease.ModuloRelease(m.getTipo(), m.getNombre(), m.getTecnologia())).toList(),
                release.map(FaseGestionJpaRepository.DatosReleaseFila::getNotas).orElse(null),
                release.map(r -> !Boolean.FALSE.equals(r.getEnviarNotas())).orElse(true),
                repository.cambios(faseId).stream()
                        .map(c -> new NotasRelease.Cambio(c.getTipo(), c.getCodigo(), c.getTitulo())).toList(),
                repository.demo(faseId)
                        .map(d -> new InfoRelease.Demo(d.getEstado(), d.getRevisadaPor(), d.getRevisadaEn())).orElse(null));
    }

    @Override
    public void guardarNotas(Long proyectoId, Long faseId, String version, String resumen, String notas,
                             boolean enviar) {
        repository.guardarNotas(proyectoId, faseId, version, resumen, notas, enviar);
    }

    @Override
    public void marcarCriterio(Long faseId, Long criterioId, Long usuarioId, LocalDateTime ahora) {
        if (repository.marcarCriterio(faseId, criterioId, usuarioId, ahora) == 0) {
            throw new ReglaNegocioException("Alguien más ya marcó ese criterio. Recarga para verlo.");
        }
    }

    @Override
    public void pasarAListaParaRelease(Long faseId, LocalDateTime ahora) {
        String destino = AccionFase.CERRAR.destino();
        if (repository.cambiarEstado(faseId, "EN_CURSO", destino, null, null) > 0) {
            repository.registrarHistorial("FASE", faseId, "EN_CURSO", destino, "Se cumplieron todos los criterios de cierre",
                    true, null, ahora);
        }
    }

    /** El BUGS de prioridad alta que nace al revertir (PDF pág. 25), ya confirmado por el Líder. */
    private String crearBugDeReversion(CambioFase c, Long usuarioId, LocalDateTime ahora) {
        int numero = repository.siguienteNumeroBug(c.proyectoId());
        String titulo = "Falla en " + c.version() + ": " + c.motivo();
        TicketEntity ticket = new TicketEntity();
        ticket.setProyectoId(c.proyectoId());
        ticket.setFaseId(c.faseId());
        ticket.setTipo("BUGS");
        ticket.setNumero(numero);
        ticket.setTitulo(titulo.length() > 200 ? titulo.substring(0, 197) + "…" : titulo);
        ticket.setDescripcion(c.motivo());
        ticket.setEstado("CONFIRMADO");
        ticket.setPrioridad("ALTA");
        ticket.setModuloId(repository.primerModulo(c.faseId()));
        ticket.setResponsableId(c.asignarBugA());
        ticket.setCreadoPor(usuarioId);
        ticket.setHorasRegistradas(BigDecimal.ZERO);
        Long id = tickets.saveAndFlush(ticket).getId();
        repository.registrarHistorial("TICKET", id, null, "CONFIRMADO", "Se revirtió " + c.version(), false,
                usuarioId, ahora);
        repository.guardarBugReversion(c.faseId(), id);
        return codigo("BUGS", numero);
    }

    private void moverHistorias(Long faseId, Set<String> desde, String hacia, String motivo, Long usuarioId,
                                LocalDateTime ahora) {
        repository.registrarHistorias(faseId, desde, hacia, motivo, usuarioId, ahora);
        repository.moverHistorias(faseId, desde, hacia);
    }

    private void moverTickets(Long faseId, Set<String> desde, String hacia, String motivo, Long usuarioId,
                              LocalDateTime ahora) {
        repository.registrarTickets(faseId, desde, false, hacia, motivo, usuarioId, ahora);
        repository.moverTickets(faseId, desde, false, hacia);
    }

    private String nombreDeFase(CambioFase c) {
        return repository.fase(c.proyectoId(), c.faseId()).map(FaseFila::getNombre).orElse(null);
    }

    private static String codigo(String tipo, Integer numero) {
        return CodigoItem.de(tipo, numero);
    }

    private static String nombreCompleto(String nombre, String apellidos) {
        return apellidos == null || apellidos.isBlank() ? nombre : nombre + " " + apellidos.trim();
    }

    @Override
    public BloqueosEliminarFase bloqueosParaEliminar(Long faseId) {
        BloqueosFila b = repository.bloqueosParaEliminar(faseId);
        return new BloqueosEliminarFase(b.getHistoriasIniciadas(), b.getTickets(), b.getOtros());
    }

    @Override
    public List<String> rutasDeImagenes(Long faseId) {
        return repository.rutasDeImagenes(faseId);
    }

    @Override
    public void eliminar(Long faseId) {
        repository.quitarImagenesDeHistorias(faseId);
        repository.quitarComentariosDeHistorias(faseId);
        repository.quitarHistorial(faseId);
        repository.quitarCriteriosDeHistorias(faseId);
        repository.quitarModulosDeHistorias(faseId);
        repository.quitarHistorias(faseId);
        repository.quitarCriteriosDeCierre(faseId);
        repository.quitarModulos(faseId);
        repository.eliminarFase(faseId);
    }

    /** MySQL devuelve las condiciones como 0 o 1. */
    private static boolean verdadero(Number valor) {
        return valor != null && valor.intValue() != 0;
    }

    private static String iniciales(String nombre, String apellidos) {
        if (nombre == null) {
            return null;
        }
        String a = apellidos == null ? "" : apellidos.trim();
        return (nombre.substring(0, 1) + (a.isEmpty() ? "" : a.substring(0, 1))).toUpperCase();
    }
}
