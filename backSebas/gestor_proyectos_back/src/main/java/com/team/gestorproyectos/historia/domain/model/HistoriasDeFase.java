package com.team.gestorproyectos.historia.domain.model;

import java.util.List;

import com.team.gestorproyectos.shared.domain.model.CodigoItem;

/**
 * Las historias de una fase completas, para copiarlas como JSON desde Importar historias.
 * Todas, también las descartadas: quien copia elige cuáles.
 */
public record HistoriasDeFase(
        String proyectoNombre,
        int faseNumero,
        String faseNombre,
        List<Historia> historias) {

    /** @param figmaUrl su propio enlace o, si no tiene, el de la fase */
    public record Historia(int numero, String estado, String titulo, String como, String quiero, String para,
                           String prioridad, Integer puntos, String figmaUrl, List<String> criterios) {

        public String codigo() {
            return CodigoItem.historia(numero);
        }
    }
}
