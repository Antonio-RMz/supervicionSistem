package com.team.gestorproyectos.fase.application;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.team.gestorproyectos.fase.domain.model.CriterioAutomatico;
import com.team.gestorproyectos.fase.domain.model.DisenoFase;
import com.team.gestorproyectos.fase.domain.model.EdicionFase;
import com.team.gestorproyectos.fase.domain.model.FaseGuardada;
import com.team.gestorproyectos.fase.domain.model.FaseParaGestionar;
import com.team.gestorproyectos.fase.domain.model.ModuloOpcion;
import com.team.gestorproyectos.fase.domain.model.NuevaFase;
import com.team.gestorproyectos.fase.domain.model.PreparacionFase;
import com.team.gestorproyectos.fase.domain.model.QuienGestiona;
import com.team.gestorproyectos.fase.domain.model.Responsable;
import com.team.gestorproyectos.fase.domain.port.in.EditarFaseUseCase;
import com.team.gestorproyectos.fase.domain.port.out.FaseGestionPort;
import com.team.gestorproyectos.fase.domain.port.out.FaseRepositoryPort;
import com.team.gestorproyectos.fase.domain.port.out.FaseRepositoryPort.ProyectoDeFase;
import com.team.gestorproyectos.shared.application.ServicioAplicacion;
import com.team.gestorproyectos.shared.application.Transaccional;
import com.team.gestorproyectos.shared.domain.exception.AccesoDenegadoException;
import com.team.gestorproyectos.shared.domain.exception.NoEncontradoException;
import com.team.gestorproyectos.shared.domain.exception.ReglaNegocioException;
import com.team.gestorproyectos.shared.domain.port.PermisosPort;

import lombok.RequiredArgsConstructor;

@ServicioAplicacion
@RequiredArgsConstructor
public class EditarFaseService implements EditarFaseUseCase {

    private final FaseRepositoryPort fases;
    private final FaseGestionPort gestion;
    private final PermisosPort permisos;

    @Override
    @Transaccional(soloLectura = true)
    public EdicionFase preparar(Long proyectoId, Long faseId, Long usuarioId, boolean esAdmin) {
        ProyectoDeFase proyecto = verificar(proyectoId, faseId, usuarioId, esAdmin);
        FaseGuardada fase = guardada(proyectoId, faseId);
        PreparacionFase preparacion = new PreparacionFase(
                proyecto.id(),
                proyecto.nombre(),
                fase.numero(),
                fase.versionRelease(),
                fase.fechaInicio(),
                proyecto.liderId(),
                false,
                null,
                fases.modulos(proyectoId),
                fases.equipo(proyectoId),
                Arrays.stream(CriterioAutomatico.values()).map(CriterioAutomatico::descripcion).toList());
        return new EdicionFase(preparacion, fase);
    }

    @Override
    @Transaccional
    public void editar(Long proyectoId, Long faseId, Long usuarioId, boolean esAdmin, NuevaFase cambios) {
        verificar(proyectoId, faseId, usuarioId, esAdmin);
        FaseGuardada fase = guardada(proyectoId, faseId);

        Map<Long, String> modulos = fases.modulos(proyectoId).stream()
                .collect(Collectors.toMap(ModuloOpcion::id, ModuloOpcion::nombre));
        if (!modulos.keySet().containsAll(cambios.moduloIds())) {
            throw new ReglaNegocioException("Uno de los repositorios no es de este proyecto");
        }
        boolean esDelEquipo = fases.equipo(proyectoId).stream().map(Responsable::id)
                .anyMatch(id -> Objects.equals(id, cambios.responsableId()));
        if (!esDelEquipo) {
            throw new ReglaNegocioException("El responsable debe ser del equipo del proyecto");
        }
        fase.validarCambios(cambios, fases.fases(proyectoId), modulos);

        fases.actualizar(faseId, cambios, fase);
    }

    @Override
    @Transaccional
    public void guardarDiseno(Long proyectoId, Long faseId, Long usuarioId, boolean esAdmin, DisenoFase diseno) {
        verificar(proyectoId, faseId, usuarioId, esAdmin);
        fases.guardarDiseno(faseId, diseno);
    }

    private ProyectoDeFase verificar(Long proyectoId, Long faseId, Long usuarioId, boolean esAdmin) {
        ProyectoDeFase proyecto = fases.proyectoVisible(proyectoId, usuarioId, esAdmin)
                .orElseThrow(() -> new NoEncontradoException("No encontramos ese proyecto"));
        FaseParaGestionar fase = gestion.buscarParaGestionar(proyectoId, faseId)
                .orElseThrow(() -> new NoEncontradoException("No encontramos esa fase"));
        Set<String> tiene = permisos.puede(usuarioId, PermisosPort.GESTIONAR_FASES)
                ? Set.of(PermisosPort.GESTIONAR_FASES) : Set.of();
        if (!fase.puedeEditar(new QuienGestiona(usuarioId, esAdmin, tiene))) {
            throw new AccesoDenegadoException(fase.motivoSinEdicion());
        }
        return proyecto;
    }

    private FaseGuardada guardada(Long proyectoId, Long faseId) {
        return fases.guardada(proyectoId, faseId)
                .orElseThrow(() -> new NoEncontradoException("No encontramos esa fase"));
    }
}
