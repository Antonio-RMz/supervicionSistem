package com.team.gestorproyectos.proyecto.application;

import java.util.List;
import java.util.Locale;

import com.team.gestorproyectos.proyecto.domain.model.ModuloProyecto;
import com.team.gestorproyectos.proyecto.domain.model.ModulosProyecto;
import com.team.gestorproyectos.proyecto.domain.model.ModulosProyecto.DatosModulo;
import com.team.gestorproyectos.proyecto.domain.port.in.GestionarModulosUseCase;
import com.team.gestorproyectos.proyecto.domain.port.out.EquipoProyectoPort;
import com.team.gestorproyectos.proyecto.domain.port.out.EquipoProyectoPort.ProyectoDelEquipo;
import com.team.gestorproyectos.proyecto.domain.port.out.ModulosProyectoPort;
import com.team.gestorproyectos.proyecto.domain.port.out.ProyectoConsultaPort;
import com.team.gestorproyectos.shared.application.ServicioAplicacion;
import com.team.gestorproyectos.shared.application.Transaccional;
import com.team.gestorproyectos.shared.domain.exception.AccesoDenegadoException;
import com.team.gestorproyectos.shared.domain.exception.NoEncontradoException;
import com.team.gestorproyectos.shared.domain.exception.ReglaNegocioException;
import com.team.gestorproyectos.shared.domain.port.PermisosPort;

import lombok.RequiredArgsConstructor;

@ServicioAplicacion
@RequiredArgsConstructor
public class GestionarModulosService implements GestionarModulosUseCase {

    private final ModulosProyectoPort modulos;
    private final EquipoProyectoPort equipos;
    private final ProyectoConsultaPort consulta;
    private final PermisosPort permisos;

    @Override
    @Transaccional(soloLectura = true)
    public ModulosProyecto modulos(Long proyectoId, Long usuarioId, boolean esAdmin) {
        ProyectoDelEquipo proyecto = verificar(proyectoId, usuarioId, esAdmin);
        return ModulosProyecto.armar(modulos.modulos(proyectoId), modulos.versionFaseEnCurso(proyectoId).orElse(null),
                modulos.produccion(proyectoId).orElse(null), puedeGestionar(proyecto, usuarioId, esAdmin));
    }

    @Override
    @Transaccional
    public Long guardar(Long proyectoId, ModuloProyecto modulo, Long responsableId, Long usuarioId, boolean esAdmin) {
        verificarGestion(proyectoId, usuarioId, esAdmin);
        if (modulo.tipo() == null) {
            throw new ReglaNegocioException("Elige qué parte del sistema es: API, Web, Móvil…");
        }
        if (modulo.nombre().isEmpty()) {
            throw new ReglaNegocioException("Escribe el nombre del repositorio, ej. portal-api");
        }
        List<DatosModulo> actuales = modulos.modulos(proyectoId);
        if (modulo.id() != null && actuales.stream().noneMatch(d -> modulo.id().equals(d.modulo().modulo().id()))) {
            throw new NoEncontradoException("Ese repositorio no es de este proyecto");
        }
        String nombre = modulo.nombre().toLowerCase(Locale.ROOT);
        actuales.stream().map(d -> d.modulo().modulo())
                .filter(m -> !m.id().equals(modulo.id()) && m.nombre().toLowerCase(Locale.ROOT).equals(nombre))
                .findFirst().ifPresent(m -> {
                    throw new ReglaNegocioException("Ya hay un repositorio llamado " + m.nombre());
                });
        if (responsableId != null && equipos.personas(proyectoId).stream().noneMatch(p -> p.id().equals(responsableId))) {
            throw new ReglaNegocioException("El responsable técnico debe ser del equipo del proyecto");
        }
        return modulos.guardar(proyectoId, modulo, responsableId);
    }

    @Override
    @Transaccional
    public void quitar(Long proyectoId, Long moduloId, Long usuarioId, boolean esAdmin) {
        verificarGestion(proyectoId, usuarioId, esAdmin);
        DatosModulo modulo = modulos.modulos(proyectoId).stream()
                .filter(d -> d.modulo().modulo().id().equals(moduloId)).findFirst()
                .orElseThrow(() -> new NoEncontradoException("Ese repositorio no es de este proyecto"));
        if (modulo.conTrabajo()) {
            throw new ReglaNegocioException("El repositorio " + modulo.modulo().modulo().nombre()
                    + " ya tiene tickets, fases o PR; no se puede quitar");
        }
        modulos.quitar(proyectoId, moduloId);
    }

    private ProyectoDelEquipo verificar(Long proyectoId, Long usuarioId, boolean esAdmin) {
        if (!consulta.visible(proyectoId, usuarioId, esAdmin)) {
            throw new NoEncontradoException("No encontramos ese proyecto");
        }
        return equipos.proyecto(proyectoId).orElseThrow(() -> new NoEncontradoException("No encontramos ese proyecto"));
    }

    private void verificarGestion(Long proyectoId, Long usuarioId, boolean esAdmin) {
        if (!puedeGestionar(verificar(proyectoId, usuarioId, esAdmin), usuarioId, esAdmin)) {
            throw new AccesoDenegadoException("Solo el Líder del proyecto o un Administrador gestionan sus repositorios");
        }
    }

    private boolean puedeGestionar(ProyectoDelEquipo proyecto, Long usuarioId, boolean esAdmin) {
        return (esAdmin || usuarioId.equals(proyecto.liderId())) && permisos.puede(usuarioId, PermisosPort.EDITAR_PROYECTOS);
    }
}
