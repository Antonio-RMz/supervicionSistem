package com.team.gestorproyectos.fase.domain.port.in;

import com.team.gestorproyectos.fase.domain.model.NuevaFase;
import com.team.gestorproyectos.fase.domain.model.PreparacionFase;

/** Nueva fase (PDF pág. 17). Pide el permiso "fase.crear" y ser miembro del proyecto. */
public interface CrearFaseUseCase {

    PreparacionFase preparar(Long proyectoId, Long usuarioId, boolean esAdmin);

    /** Devuelve el id de la fase nueva. */
    Long crear(Long proyectoId, Long usuarioId, boolean esAdmin, NuevaFase nueva);
}
