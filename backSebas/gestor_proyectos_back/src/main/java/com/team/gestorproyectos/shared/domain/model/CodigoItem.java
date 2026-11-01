package com.team.gestorproyectos.shared.domain.model;

/**
 * Cómo se nombra cada item: las historias como "HU-0016" y los tickets como "ACTV-41", "BUGS-07".
 * Es la única regla; nadie más arma estos códigos.
 */
public final class CodigoItem {

    private CodigoItem() {
    }

    /** "HU-0016" */
    public static String historia(int numero) {
        return String.format("HU-%04d", numero);
    }

    /** "HU-0016" si tipo es HIST; si no, "ACTV-41" */
    public static String de(String tipo, int numero) {
        return "HIST".equals(tipo) ? historia(numero) : String.format("%s-%02d", tipo, numero);
    }
}
