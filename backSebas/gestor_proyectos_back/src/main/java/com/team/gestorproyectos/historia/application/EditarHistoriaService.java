package com.team.gestorproyectos.historia.application;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.team.gestorproyectos.historia.domain.model.Analista;
import com.team.gestorproyectos.historia.domain.model.EdicionHistoria;
import com.team.gestorproyectos.historia.domain.model.FaseParaHistoria;
import com.team.gestorproyectos.historia.domain.model.HistoriaGuardada;
import com.team.gestorproyectos.historia.domain.model.HistoriaParaGestionar;
import com.team.gestorproyectos.historia.domain.model.NuevaHistoria;
import com.team.gestorproyectos.historia.domain.model.PreparacionHistoria;
import com.team.gestorproyectos.historia.domain.model.QuienGestiona;
import com.team.gestorproyectos.historia.domain.port.in.EditarHistoriaUseCase;
import com.team.gestorproyectos.historia.domain.port.out.HistoriaGestionPort;
import com.team.gestorproyectos.historia.domain.port.out.HistoriaRepositoryPort;
import com.team.gestorproyectos.shared.application.ServicioAplicacion;
import com.team.gestorproyectos.shared.application.Transaccional;
import com.team.gestorproyectos.shared.domain.exception.AccesoDenegadoException;
import com.team.gestorproyectos.shared.domain.exception.NoEncontradoException;
import com.team.gestorproyectos.shared.domain.exception.ReglaNegocioException;
import com.team.gestorproyectos.shared.domain.port.AvisosPort;
import com.team.gestorproyectos.shared.domain.port.PermisosPort;

import lombok.RequiredArgsConstructor;

@ServicioAplicacion
@RequiredArgsConstructor
public class EditarHistoriaService implements EditarHistoriaUseCase {

    private final HistoriaRepositoryPort historias;
    private final HistoriaGestionPort gestion;
    private final PermisosPort permisos;
    private final AvisosPort avisos;

    @Override
    @Transaccional(soloLectura = true)
    public EdicionHistoria preparar(Long proyectoId, Long historiaId, Long usuarioId, boolean esAdmin) {
        String proyecto = verificar(proyectoId, historiaId, usuarioId, esAdmin);
        HistoriaGuardada historia = guardada(proyectoId, historiaId);
        // Responsable de la historia: un Analista o un Líder (o el que ya tiene); los Analistas primero.
        List<Analista> equipo = historias.equipo(proyectoId).stream()
                .filter(a -> a.puedeSerResponsable() || a.id().equals(historia.analistaId()))
                .sorted(Comparator.comparing(Analista::esAnalista).reversed())
                .toList();
        // Solo su fase: para cambiarla de fase está "Mover de fase".
        List<FaseParaHistoria> fase = historias.fasesAbiertas(proyectoId).stream()
                .filter(f -> f.id().equals(historia.faseId())).toList();
        Set<Long> yaAsignados = historia.desarrolladores().stream()
                .map(NuevaHistoria.Desarrollador::usuarioId).collect(Collectors.toSet());
        List<Analista> desarrolladores = historias.equipo(proyectoId).stream()
                .filter(a -> a.esDesarrollador() || yaAsignados.contains(a.id())).toList();
        return new EdicionHistoria(new PreparacionHistoria(proyectoId, proyecto, historia.numero(), fase, equipo,
                historias.usuariosDelSistema(proyectoId), desarrolladores),
                historia);
    }

    @Override
    @Transaccional
    public void editar(Long proyectoId, Long historiaId, Long usuarioId, boolean esAdmin, NuevaHistoria cambios) {
        verificar(proyectoId, historiaId, usuarioId, esAdmin);
        HistoriaGuardada antes = guardada(proyectoId, historiaId);

        if (!antes.faseId().equals(cambios.faseId())) {
            throw new ReglaNegocioException("Para cambiarla de fase usa \"Mover de fase\" en su detalle");
        }
        // Mientras se define puede quedar incompleta; ya Lista, no.
        if (!Set.of("BORRADOR", "EN_PLANEACION").contains(antes.estado()) && cambios.borrador()) {
            throw new ReglaNegocioException("La historia ya está Lista: no puede quedar incompleta");
        }
        FaseParaHistoria fase = historias.fasesAbiertas(proyectoId).stream()
                .filter(f -> f.id().equals(antes.faseId())).findFirst()
                .orElseThrow(() -> new ReglaNegocioException("Su fase ya no recibe cambios en las historias"));
        Set<Long> modulosDeFase = fase.modulos().stream().map(FaseParaHistoria.Opcion::id).collect(Collectors.toSet());
        if (!modulosDeFase.containsAll(cambios.moduloIds())) {
            throw new ReglaNegocioException("Solo se pueden elegir repositorios de la Fase " + fase.numero());
        }
        // Solo al cambiarlo se pide que sea Analista o Líder: las que ya tenían otro se siguen editando.
        boolean cambiaResponsable = !cambios.analistaId().equals(antes.analistaId());
        if (historias.equipo(proyectoId).stream().noneMatch(a -> a.id().equals(cambios.analistaId())
                && (!cambiaResponsable || a.puedeSerResponsable()))) {
            throw new ReglaNegocioException("El responsable debe ser un Analista o un Líder del equipo del proyecto");
        }

        // Igual que el Analista: solo a quien se agrega se le pide el rol de Desarrollador.
        Set<Long> antesDevs = antes.desarrolladores().stream().map(NuevaHistoria.Desarrollador::usuarioId)
                .collect(Collectors.toSet());
        cambios.verificarDesarrolladores(historias.equipo(proyectoId), antesDevs);

        historias.actualizar(historiaId, cambios, antes, usuarioId);

        // Solo a quien entra: el nuevo responsable y los Desarrolladores que se agregaron.
        if (cambiaResponsable) {
            avisos.historiaAsignada(historiaId, AvisosPort.Papel.RESPONSABLE, usuarioId, List.of(cambios.analistaId()));
        }
        avisos.historiaAsignada(historiaId, AvisosPort.Papel.DESARROLLADOR, usuarioId, cambios.desarrolladores().stream()
                .map(NuevaHistoria.Desarrollador::usuarioId).filter(id -> !antesDevs.contains(id)).toList());
    }

    private String verificar(Long proyectoId, Long historiaId, Long usuarioId, boolean esAdmin) {
        String proyecto = historias.proyectoVisible(proyectoId, usuarioId, esAdmin)
                .orElseThrow(() -> new NoEncontradoException("No encontramos ese proyecto"));
        HistoriaParaGestionar historia = gestion.buscarParaGestionar(proyectoId, historiaId)
                .orElseThrow(() -> new NoEncontradoException("No encontramos esa historia"));
        Set<String> tiene = permisos.puede(usuarioId, PermisosPort.MARCAR_HISTORIA_LISTA)
                ? Set.of(PermisosPort.MARCAR_HISTORIA_LISTA) : Set.of();
        if (!historia.puedeEditar(new QuienGestiona(usuarioId, esAdmin, tiene))) {
            throw new AccesoDenegadoException(historia.motivoSinEdicion());
        }
        return proyecto;
    }

    private HistoriaGuardada guardada(Long proyectoId, Long historiaId) {
        return historias.guardada(proyectoId, historiaId)
                .orElseThrow(() -> new NoEncontradoException("No encontramos esa historia"));
    }
}
