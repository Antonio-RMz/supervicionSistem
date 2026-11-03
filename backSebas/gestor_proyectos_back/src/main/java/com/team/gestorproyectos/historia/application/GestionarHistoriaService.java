package com.team.gestorproyectos.historia.application;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.team.gestorproyectos.historia.domain.model.AccionDeLista;
import com.team.gestorproyectos.historia.domain.model.AccionHistoria;
import com.team.gestorproyectos.historia.domain.model.CambioHistoria;
import com.team.gestorproyectos.historia.domain.model.DatosAccionHistoria;
import com.team.gestorproyectos.historia.domain.model.DesarrolloHistoria;
import com.team.gestorproyectos.historia.domain.model.DesarrolladorDeHistoria;
import com.team.gestorproyectos.historia.domain.model.DetalleHistoria;
import com.team.gestorproyectos.historia.domain.model.DetalleHistoria.AccionDisponible;
import com.team.gestorproyectos.historia.domain.model.DetalleHistoria.TicketReabrible;
import com.team.gestorproyectos.historia.domain.model.HistoriaParaGestionar;
import com.team.gestorproyectos.historia.domain.model.HistoriaParaIntegrar;
import com.team.gestorproyectos.historia.domain.model.ImagenGuardada;
import com.team.gestorproyectos.historia.domain.model.InfoHistoria;
import com.team.gestorproyectos.historia.domain.model.IntegracionHistoria;
import com.team.gestorproyectos.historia.domain.model.QuienGestiona;
import com.team.gestorproyectos.historia.domain.model.ResultadoAccionHistoria;
import com.team.gestorproyectos.historia.domain.port.in.GestionarHistoriaUseCase;
import com.team.gestorproyectos.historia.domain.port.out.HistoriaGestionPort;
import com.team.gestorproyectos.historia.domain.port.out.HistoriaRepositoryPort;
import com.team.gestorproyectos.shared.application.ServicioAplicacion;
import com.team.gestorproyectos.shared.application.Transaccional;
import com.team.gestorproyectos.shared.domain.exception.AccesoDenegadoException;
import com.team.gestorproyectos.shared.domain.exception.NoEncontradoException;
import com.team.gestorproyectos.shared.domain.exception.ReglaNegocioException;
import com.team.gestorproyectos.shared.domain.model.FormaTrabajo;
import com.team.gestorproyectos.shared.domain.port.ArchivosPort;
import com.team.gestorproyectos.shared.domain.port.PermisosPort;

import lombok.RequiredArgsConstructor;

@ServicioAplicacion
@RequiredArgsConstructor
public class GestionarHistoriaService implements GestionarHistoriaUseCase {

    /** Los permisos que deciden qué botones ve cada quien en la historia. */
    private static final List<String> PERMISOS_DE_HISTORIA = List.of(
            PermisosPort.MARCAR_HISTORIA_LISTA, PermisosPort.INICIAR_HISTORIAS, PermisosPort.ACEPTAR_HISTORIAS,
            PermisosPort.DESCARTAR_HISTORIAS, PermisosPort.INTEGRAR_HISTORIAS);

    private static final int LARGO_COMENTARIO = 2000;
    private static final int LARGO_TRABAJO = 300;

    private final HistoriaRepositoryPort proyectos;
    private final HistoriaGestionPort historias;
    private final PermisosPort permisos;
    private final ArchivosPort archivos;

    @Override
    @Transaccional(soloLectura = true)
    public List<AccionDeLista> accionesDeLista(Long proyectoId, Long usuarioId, boolean esAdmin) {
        verificarProyecto(proyectoId, usuarioId, esAdmin);
        QuienGestiona quien = quien(usuarioId, esAdmin);
        return historias.historiasPorDecidir(proyectoId).stream()
                .flatMap(id -> historias.buscarParaGestionar(proyectoId, id).stream())
                .flatMap(h -> h.acciones(quien).stream().filter(AccionHistoria::enLista).findFirst()
                        .map(a -> new AccionDeLista(h.id(), h.estado(), a, a.texto(),
                                impedimentos(proyectoId, h, a).stream().findFirst().orElse(null)))
                        .stream())
                .toList();
    }

    @Override
    @Transaccional(soloLectura = true)
    public DetalleHistoria detalle(Long proyectoId, Long historiaId, Long usuarioId, boolean esAdmin) {
        HistoriaParaGestionar historia = buscar(proyectoId, historiaId, usuarioId, esAdmin);
        QuienGestiona quien = quien(usuarioId, esAdmin);
        List<AccionHistoria> acciones = historia.acciones(quien);
        DesarrolloHistoria desarrollo = !historia.trabajoDirecto() ? null : new DesarrolloHistoria(
                historia.desarrolladores(),
                historias.trabajo(historiaId),
                historias.repositorios(historiaId),
                historia.desarrolladores().stream().filter(d -> d.usuarioId().equals(usuarioId)).findFirst()
                        .map(DesarrolladorDeHistoria::terminado).orElse(null),
                historia.puedeAnotarTrabajo(quien),
                historia.puedeMarcarTrabajo(quien),
                historia.requierePr() || historia.formaTrabajo() == FormaTrabajo.GITFLOW);
        return new DetalleHistoria(
                historias.info(proyectoId, historiaId),
                historia.actor(),
                !acciones.isEmpty() && !"Nadie".equals(historia.actor()),
                acciones.stream()
                        .map(a -> new AccionDisponible(a, a.texto(), impedimentos(proyectoId, historia, a))).toList(),
                historia.puedeVerificarCriterios(quien),
                historia.puedeEditar(quien),
                historia.puedeAdjuntar(quien),
                // Bugs o mejoras de la historia (ya no hay Actividades).
                Set.of("LISTA", "LISTA_DESARROLLO", "EN_DESARROLLO").contains(historia.estado())
                        && permisos.puede(usuarioId, PermisosPort.CREAR_TICKETS),
                historia.ticketsSinEmpezar().size(),
                historia.ticketsReabribles().stream()
                        .map(t -> new TicketReabrible(t.id(), t.codigo(), t.titulo())).toList(),
                historia.fasesDestino(),
                historia.puedeEliminar(quien)
                        && historia.impedimentosParaEliminar(historias.ticketsQueVienenDe(historiaId)).isEmpty(),
                desarrollo);
    }

    @Override
    @Transaccional(soloLectura = true)
    public IntegracionHistoria integracion(Long proyectoId, Long historiaId, Long usuarioId, boolean esAdmin) {
        HistoriaParaGestionar historia = buscar(proyectoId, historiaId, usuarioId, esAdmin);
        QuienGestiona quien = quien(usuarioId, esAdmin);
        HistoriaParaIntegrar integrar = historias.paraIntegrar(proyectoId, historiaId);
        InfoHistoria info = historias.info(proyectoId, historiaId);
        boolean puede = historia.puede(quien, AccionHistoria.INTEGRAR);
        List<String> impedimentos = AccionHistoria.INTEGRAR.aplicaEn(historia.estado())
                ? impedimentos(historia, AccionHistoria.INTEGRAR, integrar) : List.of();
        return new IntegracionHistoria(historiaId, proyectoId, info.proyectoNombre(), info.faseId(),
                info.faseNumero(), integrar.version(), historia.codigo(), historia.titulo(), historia.estado(),
                info.analistaNombre(), !puede ? null : usuarioId.equals(historia.liderId()) ? "Líder" : "Admin",
                impedimentos, integrar.prefijosDeRama(),
                (int) integrar.porIntegrar().stream().filter(t -> t.pr() != null).count(),
                integrar.queSeIntegra().stream().mapToInt(m -> m.tickets().size()).sum(),
                integrar.queSeIntegra(), integrar.revision(), integrar.ambientes(), integrar.historiasDeFase(),
                integrar.pasos());
    }

    @Override
    @Transaccional
    public ResultadoAccionHistoria ejecutar(Long proyectoId, Long historiaId, Long usuarioId, boolean esAdmin,
                                            AccionHistoria accion, DatosAccionHistoria datos) {
        HistoriaParaGestionar historia = buscar(proyectoId, historiaId, usuarioId, esAdmin);
        CambioHistoria cambio = historia.ejecutar(quien(usuarioId, esAdmin), accion, datos);
        if (accion == AccionHistoria.INTEGRAR) {
            // La revisión previa de la pág. 28 (PR, CI, conflictos) también impide integrar.
            historias.paraIntegrar(proyectoId, historiaId).impedimentos().stream().findFirst().ifPresent(falta -> {
                throw new ReglaNegocioException(falta);
            });
        }
        String bug = historias.guardar(cambio, usuarioId, LocalDateTime.now());
        return new ResultadoAccionHistoria(accion.texto(), cambio.estadoFinal(), bug);
    }

    @Override
    @Transaccional
    public void verificarCriterio(Long proyectoId, Long historiaId, Long criterioId, boolean verificado,
                                  Long usuarioId, boolean esAdmin) {
        HistoriaParaGestionar historia = buscar(proyectoId, historiaId, usuarioId, esAdmin);
        historia.verificarCriterio(quien(usuarioId, esAdmin), criterioId, historias.criterios(historiaId));
        historias.verificarCriterio(criterioId, verificado);
    }

    @Override
    @Transaccional
    public void comentar(Long proyectoId, Long historiaId, String texto, Long usuarioId, boolean esAdmin) {
        buscar(proyectoId, historiaId, usuarioId, esAdmin);
        String limpio = texto == null ? "" : texto.trim();
        if (limpio.isEmpty()) {
            throw new ReglaNegocioException("Escribe el comentario");
        }
        if (limpio.length() > LARGO_COMENTARIO) {
            throw new ReglaNegocioException("El comentario es muy largo (máximo " + LARGO_COMENTARIO + " letras)");
        }
        historias.comentar(historiaId, usuarioId, limpio, LocalDateTime.now());
    }

    @Override
    @Transaccional
    public void marcarMiParte(Long proyectoId, Long historiaId, boolean terminado, Long usuarioId, boolean esAdmin) {
        HistoriaParaGestionar historia = buscar(proyectoId, historiaId, usuarioId, esAdmin);
        historia.verificarMiParte(quien(usuarioId, esAdmin));
        historias.marcarParte(historiaId, usuarioId, terminado);
    }

    @Override
    @Transaccional
    public void agregarTrabajo(Long proyectoId, Long historiaId, String descripcion, Long moduloId, Long usuarioId,
                               boolean esAdmin) {
        HistoriaParaGestionar historia = buscar(proyectoId, historiaId, usuarioId, esAdmin);
        if (!historia.puedeAnotarTrabajo(quien(usuarioId, esAdmin))) {
            throw new AccesoDenegadoException("\"Lo que hice\" lo anotan sus Desarrolladores, su Analista o el Líder"
                    + " mientras no se acepta");
        }
        String limpia = descripcion == null ? "" : descripcion.trim();
        if (limpia.isEmpty()) {
            throw new ReglaNegocioException("Escribe lo que hiciste");
        }
        if (limpia.length() > LARGO_TRABAJO) {
            throw new ReglaNegocioException("Es muy largo (máximo " + LARGO_TRABAJO + " letras)");
        }
        if (moduloId != null && historias.repositorios(historiaId).stream().noneMatch(r -> r.id().equals(moduloId))) {
            throw new ReglaNegocioException("Ese repositorio no es de la historia");
        }
        historias.agregarTrabajo(historiaId, limpia, moduloId, usuarioId, LocalDateTime.now());
    }

    @Override
    @Transaccional
    public void marcarTrabajo(Long proyectoId, Long historiaId, Long trabajoId, boolean hecho, Long usuarioId,
                              boolean esAdmin) {
        HistoriaParaGestionar historia = buscar(proyectoId, historiaId, usuarioId, esAdmin);
        if (!historia.puedeMarcarTrabajo(quien(usuarioId, esAdmin))) {
            throw new AccesoDenegadoException("Lo marcan sus Desarrolladores mientras está en desarrollo");
        }
        if (!historias.marcarTrabajo(historiaId, trabajoId, hecho)) {
            throw new NoEncontradoException("Ese punto no es de esta historia");
        }
    }

    @Override
    @Transaccional
    public void quitarTrabajo(Long proyectoId, Long historiaId, Long trabajoId, Long usuarioId, boolean esAdmin) {
        HistoriaParaGestionar historia = buscar(proyectoId, historiaId, usuarioId, esAdmin);
        if (!historia.puedeAnotarTrabajo(quien(usuarioId, esAdmin))) {
            throw new AccesoDenegadoException("\"Lo que hice\" lo cambian sus Desarrolladores, su Analista o el Líder"
                    + " mientras no se acepta");
        }
        if (!historias.quitarTrabajo(historiaId, trabajoId)) {
            throw new NoEncontradoException("Ese punto no es de esta historia");
        }
    }

    @Override
    @Transaccional
    public void eliminar(Long proyectoId, Long historiaId, Long usuarioId, boolean esAdmin) {
        HistoriaParaGestionar historia = buscar(proyectoId, historiaId, usuarioId, esAdmin);
        historia.verificarEliminar(quien(usuarioId, esAdmin), historias.ticketsQueVienenDe(historiaId));
        List<String> rutas = historias.imagenes(historiaId).stream().map(ImagenGuardada::ruta).toList();
        historias.eliminar(historiaId);
        // Los archivos al final: si falla la base, las imágenes siguen ahí.
        rutas.forEach(archivos::borrar);
    }

    /** Integrar además espera la revisión previa de sus PR (PDF pág. 28). */
    private List<String> impedimentos(Long proyectoId, HistoriaParaGestionar historia, AccionHistoria accion) {
        return accion == AccionHistoria.INTEGRAR
                ? impedimentos(historia, accion, historias.paraIntegrar(proyectoId, historia.id()))
                : historia.impedimentos(accion);
    }

    private static List<String> impedimentos(HistoriaParaGestionar historia, AccionHistoria accion,
                                             HistoriaParaIntegrar integrar) {
        List<String> faltan = new ArrayList<>(historia.impedimentos(accion));
        integrar.impedimentos().stream().filter(f -> !faltan.contains(f)).forEach(faltan::add);
        return faltan;
    }

    private HistoriaParaGestionar buscar(Long proyectoId, Long historiaId, Long usuarioId, boolean esAdmin) {
        verificarProyecto(proyectoId, usuarioId, esAdmin);
        return historias.buscarParaGestionar(proyectoId, historiaId)
                .orElseThrow(() -> new NoEncontradoException("No encontramos esa historia"));
    }

    private void verificarProyecto(Long proyectoId, Long usuarioId, boolean esAdmin) {
        proyectos.proyectoVisible(proyectoId, usuarioId, esAdmin)
                .orElseThrow(() -> new NoEncontradoException("No encontramos ese proyecto"));
    }

    private QuienGestiona quien(Long usuarioId, boolean esAdmin) {
        Set<String> tiene = PERMISOS_DE_HISTORIA.stream()
                .filter(p -> permisos.puede(usuarioId, p))
                .collect(Collectors.toSet());
        return new QuienGestiona(usuarioId, esAdmin, tiene);
    }
}
