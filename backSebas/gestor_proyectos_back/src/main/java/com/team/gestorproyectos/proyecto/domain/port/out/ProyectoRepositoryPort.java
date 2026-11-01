package com.team.gestorproyectos.proyecto.domain.port.out;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.team.gestorproyectos.proyecto.domain.model.ConfiguracionProyecto;
import com.team.gestorproyectos.proyecto.domain.model.DatosProyecto;
import com.team.gestorproyectos.proyecto.domain.model.ProyectoResumen;

public interface ProyectoRepositoryPort {

    List<ProyectoResumen> todos();

    List<ProyectoResumen> dondeEsMiembro(Long usuarioId);

    Set<String> codigos();

    boolean clienteActivo(Long clienteId);

    /** De los ids dados, los que son personas del equipo con cuenta activa. */
    Set<Long> personasActivas(Collection<Long> usuarioIds);

    /** Si la persona tiene el rol Líder: solo así puede liderar un proyecto. */
    boolean esLider(Long usuarioId);

    /** Crea el proyecto, su equipo, sus módulos y la fase de Mantenimiento. Devuelve el id. */
    Long crear(DatosProyecto datos, String codigo);

    Optional<ConfiguracionProyecto> configuracion(Long proyectoId);

    /** Módulos que ya tienen tickets o están en alguna fase: no se pueden quitar. */
    Set<Long> modulosConTrabajo(Long proyectoId);

    void actualizar(Long proyectoId, DatosProyecto datos);
}
