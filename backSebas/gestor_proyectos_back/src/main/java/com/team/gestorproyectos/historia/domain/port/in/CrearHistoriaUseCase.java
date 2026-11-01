package com.team.gestorproyectos.historia.domain.port.in;

import java.util.List;

import com.team.gestorproyectos.historia.domain.model.HistoriaImportada;
import com.team.gestorproyectos.historia.domain.model.NuevaHistoria;
import com.team.gestorproyectos.historia.domain.model.PreparacionHistoria;
import com.team.gestorproyectos.historia.domain.model.ResultadoImportacion;

/** Nueva historia de usuario (PDF pág. 18). Pide "historia.crear" y ser miembro del proyecto. */
public interface CrearHistoriaUseCase {

    PreparacionHistoria preparar(Long proyectoId, Long usuarioId, boolean esAdmin);

    /** Devuelve el id de la historia nueva. */
    Long crear(Long proyectoId, Long usuarioId, boolean esAdmin, NuevaHistoria nueva);

    /**
     * Importar historias: crea varias de un JSON en la fase, todas en Borrador, con el mismo Analista y con
     * los repositorios de la fase. Si una no es válida no se crea ninguna.
     */
    ResultadoImportacion importar(Long proyectoId, Long usuarioId, boolean esAdmin, Long faseId, Long analistaId,
                                  List<HistoriaImportada> historias);
}
