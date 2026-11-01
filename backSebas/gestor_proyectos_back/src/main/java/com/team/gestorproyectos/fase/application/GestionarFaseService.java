package com.team.gestorproyectos.fase.application;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.team.gestorproyectos.fase.domain.model.AccionDeLista;
import com.team.gestorproyectos.fase.domain.model.AccionFase;
import com.team.gestorproyectos.fase.domain.model.CambioFase;
import com.team.gestorproyectos.fase.domain.model.DatosAccionFase;
import com.team.gestorproyectos.fase.domain.model.DetalleFase;
import com.team.gestorproyectos.fase.domain.model.DetalleFase.AccionDisponible;
import com.team.gestorproyectos.fase.domain.model.FaseParaGestionar;
import com.team.gestorproyectos.fase.domain.model.GrupoTickets;
import com.team.gestorproyectos.fase.domain.model.InfoFase;
import com.team.gestorproyectos.fase.domain.model.InfoFase.HistoriaDeFase;
import com.team.gestorproyectos.fase.domain.model.InfoRelease;
import com.team.gestorproyectos.fase.domain.model.QuienGestiona;
import com.team.gestorproyectos.fase.domain.model.ReleaseFase;
import com.team.gestorproyectos.fase.domain.model.ResultadoAccionFase;
import com.team.gestorproyectos.fase.domain.port.in.GestionarFaseUseCase;
import com.team.gestorproyectos.fase.domain.port.out.FaseGestionPort;
import com.team.gestorproyectos.fase.domain.port.out.FaseRepositoryPort;
import com.team.gestorproyectos.shared.application.ServicioAplicacion;
import com.team.gestorproyectos.shared.application.Transaccional;
import com.team.gestorproyectos.shared.domain.exception.NoEncontradoException;
import com.team.gestorproyectos.shared.domain.exception.ReglaNegocioException;
import com.team.gestorproyectos.shared.domain.model.ConteoTickets;
import com.team.gestorproyectos.shared.domain.model.Progreso;
import com.team.gestorproyectos.shared.domain.port.ArchivosPort;
import com.team.gestorproyectos.shared.domain.port.PermisosPort;

import lombok.RequiredArgsConstructor;

@ServicioAplicacion
@RequiredArgsConstructor
public class GestionarFaseService implements GestionarFaseUseCase {

    /** Los permisos que deciden qué botones ve cada quien en la fase. */
    private static final List<String> PERMISOS_DE_FASE = List.of(
            PermisosPort.GESTIONAR_FASES, PermisosPort.REABRIR_FASES, PermisosPort.MARCAR_CRITERIOS,
            PermisosPort.PUBLICAR_RELEASE, PermisosPort.REVERTIR_RELEASE);

    private final FaseRepositoryPort proyectos;
    private final FaseGestionPort fases;
    private final PermisosPort permisos;
    private final ArchivosPort archivos;

    @Override
    @Transaccional(soloLectura = true)
    public List<AccionDeLista> accionesDeLista(Long proyectoId, Long usuarioId, boolean esAdmin) {
        proyectos.proyectoVisible(proyectoId, usuarioId, esAdmin)
                .orElseThrow(() -> new NoEncontradoException("No encontramos ese proyecto"));
        QuienGestiona quien = quien(usuarioId, esAdmin);
        return fases.fases(proyectoId).stream()
                .flatMap(id -> fases.buscarParaGestionar(proyectoId, id).stream())
                .flatMap(fase -> fase.acciones(quien).stream().filter(AccionFase::enLista).findFirst()
                        .map(a -> new AccionDeLista(fase.id(), fase.numero(), fase.nombre(), fase.estado(), a, a.texto(),
                                fase.impedimentos(a).stream().findFirst().orElse(null)))
                        .stream())
                .toList();
    }

    @Override
    @Transaccional(soloLectura = true)
    public DetalleFase detalle(Long proyectoId, Long faseId, Long usuarioId, boolean esAdmin) {
        FaseParaGestionar fase = buscar(proyectoId, faseId, usuarioId, esAdmin);
        QuienGestiona quien = quien(usuarioId, esAdmin);
        InfoFase info = fases.info(proyectoId, faseId);

        List<AccionFase> acciones = fase.acciones(quien);
        boolean marcaCriterios = fase.puedeMarcarCriterios(quien);
        boolean soloExclusivas = acciones.stream().allMatch(a -> a == AccionFase.REABRIR);
        String rol = acciones.isEmpty() && !marcaCriterios ? null
                : soloExclusivas && !marcaCriterios ? "Admin" : "Líder";

        List<ConteoTickets> conteos = info.tickets().stream()
                .map(t -> new ConteoTickets(faseId, null, t.estado(), t.cantidad()))
                .toList();
        Progreso avance = Progreso.de(conteos);
        return new DetalleFase(
                info,
                Progreso.deFase(faseId, info.estado(), conteos),
                avance.terminados(),
                avance.total(),
                GrupoTickets.contar(info.tickets()),
                fase.criterios(),
                fase.versionAnterior(),
                fase.bugReversion(),
                rol,
                usuarioId.equals(fase.responsableId()),
                acciones.stream().map(a -> new AccionDisponible(a, texto(a, fase), fase.impedimentos(a))).toList(),
                marcaCriterios,
                permisos.puede(usuarioId, PermisosPort.CREAR_HISTORIAS),
                fase.puedeEditar(quien),
                fase.puedeEliminar(quien) && fase.impedimentosParaEliminar(fases.bloqueosParaEliminar(faseId)).isEmpty(),
                !fase.puedeEliminarHistorias(quien) ? List.of()
                        : info.historias().stream().filter(HistoriaDeFase::eliminable).map(HistoriaDeFase::id).toList());
    }

    @Override
    @Transaccional
    public ResultadoAccionFase ejecutar(Long proyectoId, Long faseId, Long usuarioId, boolean esAdmin,
                                        AccionFase accion, DatosAccionFase datos) {
        FaseParaGestionar fase = buscar(proyectoId, faseId, usuarioId, esAdmin);
        CambioFase cambio = fase.ejecutar(quien(usuarioId, esAdmin), accion, datos, LocalDate.now());
        String bug = fases.guardar(cambio, usuarioId, LocalDateTime.now());
        return new ResultadoAccionFase(texto(accion, fase), cambio.estadoNuevo(), bug);
    }

    @Override
    @Transaccional(soloLectura = true)
    public ReleaseFase release(Long proyectoId, Long faseId, Long usuarioId, boolean esAdmin) {
        FaseParaGestionar fase = buscar(proyectoId, faseId, usuarioId, esAdmin);
        if (fase.mantenimiento() || fase.versionRelease() == null) {
            throw new ReglaNegocioException("Esta fase no se publica con un release");
        }
        InfoRelease info = fases.release(proyectoId, faseId);
        return new ReleaseFase(info, info.notasVigentes(), fase.puedeEditarNotas(quien(usuarioId, esAdmin)));
    }

    @Override
    @Transaccional
    public void guardarNotas(Long proyectoId, Long faseId, Long usuarioId, boolean esAdmin, String notas,
                             boolean enviarAlCliente) {
        FaseParaGestionar fase = buscar(proyectoId, faseId, usuarioId, esAdmin);
        String limpias = fase.notasParaGuardar(quien(usuarioId, esAdmin), notas);
        fases.guardarNotas(proyectoId, faseId, fase.versionRelease(), fase.nombre(), limpias, enviarAlCliente);
    }

    @Override
    @Transaccional
    public ResultadoAccionFase marcarCriterio(Long proyectoId, Long faseId, Long criterioId, Long usuarioId,
                                              boolean esAdmin) {
        FaseParaGestionar fase = buscar(proyectoId, faseId, usuarioId, esAdmin);
        boolean quedaLista = fase.marcarCriterio(quien(usuarioId, esAdmin), criterioId);
        LocalDateTime ahora = LocalDateTime.now();
        fases.marcarCriterio(faseId, criterioId, usuarioId, ahora);
        if (quedaLista) {
            fases.pasarAListaParaRelease(faseId, ahora);
        }
        return new ResultadoAccionFase("Criterio cumplido", quedaLista ? "LISTA_PARA_RELEASE" : fase.estado(), null);
    }

    @Override
    @Transaccional
    public void eliminar(Long proyectoId, Long faseId, Long usuarioId, boolean esAdmin) {
        FaseParaGestionar fase = buscar(proyectoId, faseId, usuarioId, esAdmin);
        fase.verificarEliminar(quien(usuarioId, esAdmin), fases.bloqueosParaEliminar(faseId));
        List<String> rutas = fases.rutasDeImagenes(faseId);
        fases.eliminar(faseId);
        // Los archivos al final: si falla la base, las imágenes siguen ahí.
        rutas.forEach(archivos::borrar);
    }

    private FaseParaGestionar buscar(Long proyectoId, Long faseId, Long usuarioId, boolean esAdmin) {
        proyectos.proyectoVisible(proyectoId, usuarioId, esAdmin)
                .orElseThrow(() -> new NoEncontradoException("No encontramos ese proyecto"));
        return fases.buscarParaGestionar(proyectoId, faseId)
                .orElseThrow(() -> new NoEncontradoException("No encontramos esa fase"));
    }

    private QuienGestiona quien(Long usuarioId, boolean esAdmin) {
        Set<String> tiene = PERMISOS_DE_FASE.stream()
                .filter(p -> permisos.puede(usuarioId, p))
                .collect(Collectors.toSet());
        return new QuienGestiona(usuarioId, esAdmin, tiene);
    }

    /** El texto del botón en el detalle, con su versión: "Subir a main y crear tag v1.3.0", "Revertir a v1.2.0". */
    private static String texto(AccionFase accion, FaseParaGestionar fase) {
        return switch (accion) {
            case SUBIR_A_MAIN -> fase.versionRelease() == null ? accion.texto()
                    : "Subir a main y crear tag " + fase.versionRelease();
            case REVERTIR -> Stream.ofNullable(fase.versionAnterior()).map(v -> "Revertir a " + v).findFirst()
                    .orElse("Revertir publicación");
            default -> accion.texto();
        };
    }
}
