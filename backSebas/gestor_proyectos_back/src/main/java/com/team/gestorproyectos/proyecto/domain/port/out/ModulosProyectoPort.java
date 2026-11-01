package com.team.gestorproyectos.proyecto.domain.port.out;

import java.util.List;
import java.util.Optional;

import com.team.gestorproyectos.proyecto.domain.model.ModuloProyecto;
import com.team.gestorproyectos.proyecto.domain.model.ModulosProyecto.Ambiente;
import com.team.gestorproyectos.proyecto.domain.model.ModulosProyecto.DatosModulo;

/** Los módulos de un proyecto con lo que se ve en su tarjeta (PDF pág. 35). */
public interface ModulosProyectoPort {

    /** En orden de creación, con su CI, tickets por estado, bugs de Mantenimiento e integraciones de la fase en curso. */
    List<DatosModulo> modulos(Long proyectoId);

    /** La versión del release de la fase en curso ("v1.3.0"); vacío si no hay una. */
    Optional<String> versionFaseEnCurso(Long proyectoId);

    /** La última versión publicada en producción y cuándo. */
    Optional<Ambiente> produccion(Long proyectoId);

    /** Crea el módulo (id null) o lo actualiza. @return su id */
    Long guardar(Long proyectoId, ModuloProyecto modulo, Long responsableId);

    void quitar(Long proyectoId, Long moduloId);
}
