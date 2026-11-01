package com.team.gestorproyectos.historia.domain.port.in;

import com.team.gestorproyectos.historia.domain.model.HistoriasDeFase;
import com.team.gestorproyectos.historia.domain.model.ListadoHistorias;

/** Menú Historias: la lista de historias de un proyecto. Pide ser miembro del proyecto, su Líder o Admin. */
public interface ListarHistoriasUseCase {

    ListadoHistorias listar(Long proyectoId, Long usuarioId, boolean esAdmin);

    /** Las historias de la fase completas, con criterios y Figma, para copiarlas como JSON. */
    HistoriasDeFase deFase(Long proyectoId, Long faseId, Long usuarioId, boolean esAdmin);
}
