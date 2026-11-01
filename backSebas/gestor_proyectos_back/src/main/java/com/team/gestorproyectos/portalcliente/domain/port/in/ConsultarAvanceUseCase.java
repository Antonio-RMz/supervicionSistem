package com.team.gestorproyectos.portalcliente.domain.port.in;

import com.team.gestorproyectos.portalcliente.domain.model.AvanceProyecto;

/** Pantalla "Avance": el detalle de un proyecto. Solo si pertenece a la empresa del cliente. */
public interface ConsultarAvanceUseCase {

    AvanceProyecto consultar(Long proyectoId, Long clienteId);
}
