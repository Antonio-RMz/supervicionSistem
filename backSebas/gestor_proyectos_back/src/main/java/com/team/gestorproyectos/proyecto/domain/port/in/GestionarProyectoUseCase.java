package com.team.gestorproyectos.proyecto.domain.port.in;

import com.team.gestorproyectos.proyecto.domain.model.ConfiguracionProyecto;
import com.team.gestorproyectos.proyecto.domain.model.DatosProyecto;

/** Nuevo proyecto (PDF pág. 16) y editarlo. */
public interface GestionarProyectoUseCase {

    /** Pide "proyecto.crear". Nace en Planeación, con su fase de Mantenimiento. Devuelve el id. */
    Long crear(Long usuarioId, DatosProyecto datos);

    /** Pide "proyecto.editar" y ser su Líder (o Admin). */
    ConfiguracionProyecto configuracion(Long proyectoId, Long usuarioId, boolean esAdmin);

    void actualizar(Long proyectoId, Long usuarioId, boolean esAdmin, DatosProyecto datos);
}
