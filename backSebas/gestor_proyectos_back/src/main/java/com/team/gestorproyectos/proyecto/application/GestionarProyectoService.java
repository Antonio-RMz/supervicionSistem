package com.team.gestorproyectos.proyecto.application;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.team.gestorproyectos.proyecto.domain.model.CodigoProyecto;
import com.team.gestorproyectos.proyecto.domain.model.ConfiguracionProyecto;
import com.team.gestorproyectos.proyecto.domain.model.DatosProyecto;
import com.team.gestorproyectos.proyecto.domain.model.EstadoProyecto;
import com.team.gestorproyectos.proyecto.domain.model.ModuloProyecto;
import com.team.gestorproyectos.proyecto.domain.port.in.GestionarProyectoUseCase;
import com.team.gestorproyectos.proyecto.domain.port.out.ProyectoRepositoryPort;
import com.team.gestorproyectos.shared.application.ServicioAplicacion;
import com.team.gestorproyectos.shared.application.Transaccional;
import com.team.gestorproyectos.shared.domain.exception.AccesoDenegadoException;
import com.team.gestorproyectos.shared.domain.exception.NoEncontradoException;
import com.team.gestorproyectos.shared.domain.exception.ReglaNegocioException;
import com.team.gestorproyectos.shared.domain.port.PermisosPort;

import lombok.RequiredArgsConstructor;

@ServicioAplicacion
@RequiredArgsConstructor
public class GestionarProyectoService implements GestionarProyectoUseCase {

    private final ProyectoRepositoryPort proyectos;
    private final PermisosPort permisos;

    @Override
    @Transaccional
    public Long crear(Long usuarioId, DatosProyecto datos) {
        if (!permisos.puede(usuarioId, PermisosPort.CREAR_PROYECTOS)) {
            throw new AccesoDenegadoException("Solo el Administrador puede crear proyectos");
        }
        // Un cliente inactivo no recibe proyectos nuevos (PDF pág. 10). Uno interno no lleva cliente.
        if (datos.clienteId() != null && !proyectos.clienteActivo(datos.clienteId())) {
            throw new ReglaNegocioException("Ese cliente está inactivo o no existe");
        }
        validarEquipo(datos);
        validarLider(datos.liderId());
        DatosProyecto enPlaneacion = new DatosProyecto(datos.nombre(), datos.clienteId(),
                datos.prioridad(), EstadoProyecto.PLANEACION, datos.fechaInicio(), datos.fechaEntrega(),
                datos.descripcion(), datos.liderId(), datos.miembroIds(), datos.modulos(), datos.reglas());
        return proyectos.crear(enPlaneacion, CodigoProyecto.sugerir(datos.nombre(), proyectos.codigos()));
    }

    @Override
    @Transaccional(soloLectura = true)
    public ConfiguracionProyecto configuracion(Long proyectoId, Long usuarioId, boolean esAdmin) {
        ConfiguracionProyecto actual = proyectos.configuracion(proyectoId)
                .orElseThrow(() -> new NoEncontradoException("No encontramos ese proyecto"));
        verificarEdicion(actual, usuarioId, esAdmin);
        return actual;
    }

    @Override
    @Transaccional
    public void actualizar(Long proyectoId, Long usuarioId, boolean esAdmin, DatosProyecto datos) {
        ConfiguracionProyecto actual = configuracion(proyectoId, usuarioId, esAdmin);
        // El cliente puede seguir siendo uno inactivo; cambiarlo a otro sí pide que esté activo.
        if (datos.clienteId() != null && !datos.clienteId().equals(actual.datos().clienteId())
                && !proyectos.clienteActivo(datos.clienteId())) {
            throw new ReglaNegocioException("Ese cliente está inactivo o no existe");
        }
        validarEquipo(datos);
        // El Líder edita su proyecto, pero el equipo lo arma el Administrador.
        boolean cambiaEquipo = !datos.liderId().equals(actual.datos().liderId())
                || !Set.copyOf(datos.miembroIds()).equals(Set.copyOf(actual.datos().miembroIds()));
        if (cambiaEquipo && !permisos.puede(usuarioId, PermisosPort.ARMAR_EQUIPO)) {
            throw new AccesoDenegadoException("Solo un Administrador cambia el Líder o el equipo del proyecto");
        }
        // Solo al cambiarlo: un proyecto que ya tenía otro Líder se sigue pudiendo editar.
        if (!datos.liderId().equals(actual.datos().liderId())) {
            validarLider(datos.liderId());
        }
        // La forma de trabajo se eligió al crearlo y ya no cambia.
        datos = new DatosProyecto(datos.nombre(), datos.clienteId(), datos.prioridad(),
                datos.estado() == null ? actual.datos().estado() : datos.estado(), datos.fechaInicio(),
                datos.fechaEntrega(), datos.descripcion(), datos.liderId(), datos.miembroIds(), datos.modulos(),
                datos.reglas().conFormaDe(actual.datos().reglas()));

        Set<Long> quedan = datos.modulos().stream().map(ModuloProyecto::id).filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Set<Long> conTrabajo = proyectos.modulosConTrabajo(proyectoId);
        actual.datos().modulos().stream()
                .filter(m -> !quedan.contains(m.id()) && conTrabajo.contains(m.id()))
                .findFirst()
                .ifPresent(m -> {
                    throw new ReglaNegocioException("El repositorio " + m.nombre() + " ya tiene tickets o fases; no se puede quitar");
                });
        Set<Long> propios = actual.datos().modulos().stream().map(ModuloProyecto::id).collect(Collectors.toSet());
        if (!propios.containsAll(quedan)) {
            throw new ReglaNegocioException("Uno de los repositorios no es de este proyecto");
        }

        proyectos.actualizar(proyectoId, datos);
    }

    private void verificarEdicion(ConfiguracionProyecto proyecto, Long usuarioId, boolean esAdmin) {
        boolean esSuLider = usuarioId.equals(proyecto.datos().liderId());
        if (!(esAdmin || esSuLider) || !permisos.puede(usuarioId, PermisosPort.EDITAR_PROYECTOS)) {
            throw new AccesoDenegadoException("Solo el Líder del proyecto o un Administrador pueden editarlo");
        }
    }

    /** El Líder decide sobre historias, fases y releases: tiene que tener el rol. */
    private void validarLider(Long liderId) {
        if (!proyectos.esLider(liderId)) {
            throw new ReglaNegocioException("El Líder del proyecto debe tener el rol Líder");
        }
    }

    private void validarEquipo(DatosProyecto datos) {
        Set<Long> faltan = new HashSet<>(datos.miembroIds());
        faltan.removeAll(proyectos.personasActivas(datos.miembroIds()));
        if (faltan.contains(datos.liderId())) {
            throw new ReglaNegocioException("El Líder debe ser una persona del equipo con cuenta activa");
        }
        if (!faltan.isEmpty()) {
            throw new ReglaNegocioException("Alguien del equipo no existe o tiene la cuenta inactiva");
        }
    }
}
