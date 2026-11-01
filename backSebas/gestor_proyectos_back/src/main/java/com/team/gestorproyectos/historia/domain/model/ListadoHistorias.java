package com.team.gestorproyectos.historia.domain.model;

import java.time.LocalDateTime;
import java.util.List;

import com.team.gestorproyectos.shared.domain.model.CodigoItem;

/**
 * Todas las historias de un proyecto (menú Historias): de la más reciente a la más antigua.
 *
 * @param puedeCrear tiene el permiso historia.crear
 */
public record ListadoHistorias(Long proyectoId, String proyectoNombre, List<HistoriaDeLista> historias,
                               boolean puedeCrear) {

    /**
     * @param ticketsTerminados aprobados en QA o más adelante (sin contar cancelados)
     * @param ticketsTotal      los que cuentan: sin cancelados, rechazados ni no reproducibles
     */
    public record HistoriaDeLista(Long id, int numero, String titulo, String estado, String prioridad, Integer puntos,
                                  Long faseId, int faseNumero, String faseNombre, String analistaNombre,
                                  int ticketsTerminados, int ticketsTotal, LocalDateTime actualizadoEn) {

        /** "HU-0016" */
        public String codigo() {
            return CodigoItem.historia(numero);
        }
    }
}
