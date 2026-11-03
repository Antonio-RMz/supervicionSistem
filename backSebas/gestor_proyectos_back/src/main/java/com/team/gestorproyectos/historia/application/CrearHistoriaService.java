package com.team.gestorproyectos.historia.application;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.team.gestorproyectos.historia.domain.model.Analista;
import com.team.gestorproyectos.historia.domain.model.FaseParaHistoria;
import com.team.gestorproyectos.historia.domain.model.HistoriaImportada;
import com.team.gestorproyectos.historia.domain.model.NuevaHistoria;
import com.team.gestorproyectos.historia.domain.model.PreparacionHistoria;
import com.team.gestorproyectos.historia.domain.model.ResultadoImportacion;
import com.team.gestorproyectos.historia.domain.port.in.CrearHistoriaUseCase;
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
public class CrearHistoriaService implements CrearHistoriaUseCase {

    private static final int MAXIMO_IMPORTADAS = 50;

    private final HistoriaRepositoryPort historias;
    private final PermisosPort permisos;
    private final AvisosPort avisos;

    @Override
    @Transaccional(soloLectura = true)
    public PreparacionHistoria preparar(Long proyectoId, Long usuarioId, boolean esAdmin) {
        String nombre = verificar(proyectoId, usuarioId, esAdmin);
        // Responsable de la historia: un Analista o un Líder; los Analistas primero.
        List<Analista> equipo = historias.equipo(proyectoId).stream()
                .filter(Analista::puedeSerResponsable)
                .sorted(Comparator.comparing(Analista::esAnalista).reversed())
                .toList();
        List<Analista> desarrolladores = historias.equipo(proyectoId).stream().filter(Analista::esDesarrollador).toList();
        return new PreparacionHistoria(proyectoId, nombre, historias.siguienteNumero(proyectoId),
                historias.fasesAbiertas(proyectoId), equipo, historias.usuariosDelSistema(proyectoId),
                desarrolladores);
    }

    @Override
    @Transaccional
    public Long crear(Long proyectoId, Long usuarioId, boolean esAdmin, NuevaHistoria nueva) {
        verificar(proyectoId, usuarioId, esAdmin);

        FaseParaHistoria fase = faseAbierta(proyectoId, nueva.faseId());
        // Los módulos de una historia salen de los de su fase (PDF: "Solo los módulos de la Fase 4").
        if (!ids(fase.modulos()).containsAll(nueva.moduloIds())) {
            throw new ReglaNegocioException("Solo se pueden elegir repositorios de la Fase " + fase.numero());
        }
        verificarResponsable(proyectoId, nueva.analistaId());
        nueva.verificarDesarrolladores(historias.equipo(proyectoId), Set.of());

        Long historiaId = historias.crear(proyectoId, historias.siguienteNumero(proyectoId), nueva, usuarioId);
        // Les llega como en Mis tickets: al responsable y a cada Desarrollador.
        avisos.historiaAsignada(historiaId, AvisosPort.Papel.RESPONSABLE, usuarioId, List.of(nueva.analistaId()));
        avisos.historiaAsignada(historiaId, AvisosPort.Papel.DESARROLLADOR, usuarioId, desarrolladores(nueva));
        return historiaId;
    }

    @Override
    @Transaccional
    public ResultadoImportacion importar(Long proyectoId, Long usuarioId, boolean esAdmin, Long faseId, Long analistaId,
                                         List<HistoriaImportada> importadas) {
        verificar(proyectoId, usuarioId, esAdmin);
        if (importadas == null || importadas.isEmpty()) {
            throw new ReglaNegocioException("El JSON no trae historias");
        }
        if (importadas.size() > MAXIMO_IMPORTADAS) {
            throw new ReglaNegocioException("Se pueden importar hasta " + MAXIMO_IMPORTADAS + " historias a la vez");
        }
        FaseParaHistoria fase = faseAbierta(proyectoId, faseId);
        if (analistaId == null) {
            throw new ReglaNegocioException("Elige al Analista de las historias");
        }
        verificarResponsable(proyectoId, analistaId);

        // Primero se validan todas: si una falla, no se crea ninguna.
        Set<Long> repositorios = ids(fase.modulos());
        List<NuevaHistoria> nuevas = new ArrayList<>();
        for (int i = 0; i < importadas.size(); i++) {
            nuevas.add(aNueva(i + 1, importadas.get(i), faseId, analistaId, repositorios));
        }

        int incompletas = 0;
        List<Long> creadas = new ArrayList<>();
        for (NuevaHistoria nueva : nuevas) {
            creadas.add(historias.crear(proyectoId, historias.siguienteNumero(proyectoId), nueva, usuarioId));
            if (incompleta(nueva)) {
                incompletas++;
            }
        }
        avisos.historiasAsignadas(creadas, usuarioId, analistaId);
        return new ResultadoImportacion(nuevas.size(), incompletas);
    }

    /** Cada historia nace en Borrador con los repositorios de su fase; el error dice cuál falló. */
    private static NuevaHistoria aNueva(int posicion, HistoriaImportada h, Long faseId, Long analistaId,
                                        Set<Long> repositorios) {
        String cual = "Historia " + posicion + ": ";
        if (h == null) {
            throw new ReglaNegocioException(cual + "viene vacía");
        }
        if (h.como() == null || h.como().isBlank()) {
            throw new ReglaNegocioException(cual + "falta \"como\"");
        }
        try {
            return new NuevaHistoria(true, faseId, h.titulo(), h.como(), h.quiero(), h.para(), h.criterios(), null,
                    false, analistaId, h.prioridad(), h.puntos(), repositorios, List.of());
        } catch (ReglaNegocioException e) {
            throw new ReglaNegocioException(cual + e.getMessage());
        }
    }

    /** Lo mismo que pide "Marcar como Lista": Como / Quiero / Para, criterios y repositorios. */
    private static boolean incompleta(NuevaHistoria h) {
        return h.como() == null || h.quiero() == null || h.para() == null || h.criterios().isEmpty()
                || h.moduloIds().isEmpty();
    }

    private FaseParaHistoria faseAbierta(Long proyectoId, Long faseId) {
        return historias.fasesAbiertas(proyectoId).stream()
                .filter(f -> f.id().equals(faseId))
                .findFirst()
                .orElseThrow(() -> new ReglaNegocioException("Esa fase no es de este proyecto o ya no recibe historias"));
    }

    private void verificarResponsable(Long proyectoId, Long analistaId) {
        if (historias.equipo(proyectoId).stream()
                .noneMatch(a -> a.id().equals(analistaId) && a.puedeSerResponsable())) {
            throw new ReglaNegocioException("El responsable debe ser un Analista o un Líder del equipo del proyecto");
        }
    }

    private String verificar(Long proyectoId, Long usuarioId, boolean esAdmin) {
        String nombre = historias.proyectoVisible(proyectoId, usuarioId, esAdmin)
                .orElseThrow(() -> new NoEncontradoException("No encontramos ese proyecto"));
        if (!permisos.puede(usuarioId, PermisosPort.CREAR_HISTORIAS)) {
            throw new AccesoDenegadoException("No tienes permiso para crear historias de usuario");
        }
        return nombre;
    }

    private static List<Long> desarrolladores(NuevaHistoria historia) {
        return historia.desarrolladores().stream().map(NuevaHistoria.Desarrollador::usuarioId).toList();
    }

    private static Set<Long> ids(List<FaseParaHistoria.Opcion> opciones) {
        return opciones.stream().map(FaseParaHistoria.Opcion::id).collect(Collectors.toSet());
    }
}
