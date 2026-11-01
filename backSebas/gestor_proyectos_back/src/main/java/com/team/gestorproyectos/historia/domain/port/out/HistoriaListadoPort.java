package com.team.gestorproyectos.historia.domain.port.out;

import java.util.List;
import java.util.Optional;

import com.team.gestorproyectos.historia.domain.model.HistoriasDeFase;
import com.team.gestorproyectos.historia.domain.model.ListadoHistorias.HistoriaDeLista;

public interface HistoriaListadoPort {

    /** Todas las historias del proyecto, también las descartadas, de la más reciente a la más antigua. */
    List<HistoriaDeLista> historias(Long proyectoId);

    /** Vacío si la fase no es de ese proyecto. */
    Optional<HistoriasDeFase> deFase(Long proyectoId, Long faseId);
}
