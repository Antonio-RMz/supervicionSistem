package com.team.gestorproyectos.historia.domain.port.in;

import com.team.gestorproyectos.historia.domain.model.EdicionHistoria;
import com.team.gestorproyectos.historia.domain.model.NuevaHistoria;

/**
 * Corregir lo que se capturó en Nueva historia (título, Como/Quiero/Para, criterios, módulos...).
 * Lo hace su Analista o el Líder mientras la historia no tenga tickets.
 */
public interface EditarHistoriaUseCase {

    EdicionHistoria preparar(Long proyectoId, Long historiaId, Long usuarioId, boolean esAdmin);

    void editar(Long proyectoId, Long historiaId, Long usuarioId, boolean esAdmin, NuevaHistoria cambios);
}
