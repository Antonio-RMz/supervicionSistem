package com.team.gestorproyectos.fase.application;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.team.gestorproyectos.fase.domain.model.CriterioAutomatico;
import com.team.gestorproyectos.fase.domain.model.FaseExistente;
import com.team.gestorproyectos.fase.domain.model.ModuloOpcion;
import com.team.gestorproyectos.fase.domain.model.NuevaFase;
import com.team.gestorproyectos.fase.domain.model.PreparacionFase;
import com.team.gestorproyectos.fase.domain.model.Responsable;
import com.team.gestorproyectos.fase.domain.model.VersionRelease;
import com.team.gestorproyectos.fase.domain.port.in.CrearFaseUseCase;
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
public class CrearFaseService implements CrearFaseUseCase {

    private final FaseRepositoryPort fases;
    private final PermisosPort permisos;

    @Override
    @Transaccional(soloLectura = true)
    public PreparacionFase preparar(Long proyectoId, Long usuarioId, boolean esAdmin) {
        ProyectoDeFase proyecto = verificar(proyectoId, usuarioId, esAdmin);
        List<FaseExistente> existentes = fases.fases(proyectoId);
        FaseExistente activa = existentes.stream().filter(FaseExistente::activa).findFirst().orElse(null);
        return new PreparacionFase(
                proyecto.id(),
                proyecto.nombre(),
                FaseExistente.siguienteNumero(existentes),
                VersionRelease.siguiente(existentes.stream().map(FaseExistente::versionRelease).toList()),
                FaseExistente.inicioSugerido(existentes, LocalDate.now()),
                proyecto.liderId(),
                activa != null,
                activa == null ? null : "Fase " + activa.numero() + " · " + activa.nombre(),
                fases.modulos(proyectoId),
                fases.equipo(proyectoId),
                Arrays.stream(CriterioAutomatico.values()).map(CriterioAutomatico::descripcion).toList());
    }

    @Override
    @Transaccional
    public Long crear(Long proyectoId, Long usuarioId, boolean esAdmin, NuevaFase nueva) {
        verificar(proyectoId, usuarioId, esAdmin);
        List<FaseExistente> existentes = fases.fases(proyectoId);

        if (nueva.versionRelease() != null && existentes.stream()
                .anyMatch(f -> nueva.versionRelease().equals(f.versionRelease()))) {
            throw new ReglaNegocioException("La versión " + nueva.versionRelease() + " ya es de otra fase");
        }
        Set<Long> modulosDelProyecto = fases.modulos(proyectoId).stream().map(ModuloOpcion::id)
                .collect(Collectors.toSet());
        if (!modulosDelProyecto.containsAll(nueva.moduloIds())) {
            throw new ReglaNegocioException("Uno de los repositorios no es de este proyecto");
        }
        boolean esDelEquipo = fases.equipo(proyectoId).stream().map(Responsable::id)
                .anyMatch(id -> Objects.equals(id, nueva.responsableId()));
        if (!esDelEquipo) {
            throw new ReglaNegocioException("El responsable debe ser del equipo del proyecto");
        }

        return fases.crear(proyectoId, FaseExistente.siguienteNumero(existentes), nueva, usuarioId);
    }

    private ProyectoDeFase verificar(Long proyectoId, Long usuarioId, boolean esAdmin) {
        ProyectoDeFase proyecto = fases.proyectoVisible(proyectoId, usuarioId, esAdmin)
                .orElseThrow(() -> new NoEncontradoException("No encontramos ese proyecto"));
        if (!permisos.puede(usuarioId, PermisosPort.CREAR_FASES)) {
            throw new AccesoDenegadoException("Solo el Líder (o quien tenga el permiso) puede crear fases");
        }
        return proyecto;
    }
}
