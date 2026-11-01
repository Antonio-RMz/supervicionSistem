package com.team.gestorproyectos.fase.domain.model;

import java.time.LocalDateTime;

/**
 * Una condición para cerrar la fase (PDF pág. 32). Los automáticos los revisa el sistema;
 * los manuales los marca el Líder (ej. visto bueno del cliente en la demo).
 *
 * @param detalle     lo que se ve a la derecha: "1 de 3 aceptadas", "Automático"
 * @param cumplidoPor en uno manual: quién lo marcó ("Jorge"); null si no se ha marcado
 * @param cumplidoEn  en uno manual: cuándo se marcó
 */
public record CriterioCierre(Long id, String descripcion, boolean manual, boolean cumplido, String detalle,
                             String cumplidoPor, LocalDateTime cumplidoEn) {

    public CriterioCierre(Long id, String descripcion, boolean manual, boolean cumplido, String detalle) {
        this(id, descripcion, manual, cumplido, detalle, null, null);
    }

    public static final String AUTOMATICO = "Automático";

    /** Lo que cuenta el sistema para los criterios automáticos. */
    public record Conteos(long historias, long historiasAceptadas, long bugsAltaAbiertos) {
    }

    /**
     * Evalúa un criterio guardado. El despliegue en staging el sistema no lo puede medir (no ve el CI):
     * lo marca el Líder como uno manual cuando el CI terminó.
     *
     * @param codigo HISTORIAS_ACEPTADAS, SIN_BUGS_ALTA, MODULOS_EN_STAGING; null en los manuales
     */
    public static CriterioCierre evaluar(Long id, String descripcion, String codigo, boolean guardado,
                                         String cumplidoPor, LocalDateTime cumplidoEn, Conteos conteos) {
        if (codigo == null) {
            return new CriterioCierre(id, descripcion, true, guardado, null, guardado ? cumplidoPor : null,
                    guardado ? cumplidoEn : null);
        }
        return switch (CriterioAutomatico.valueOf(codigo)) {
            case HISTORIAS_ACEPTADAS -> new CriterioCierre(id, descripcion, false,
                    conteos.historias() > 0 && conteos.historiasAceptadas() == conteos.historias(),
                    conteos.historias() == 0 ? "Sin historias"
                            : conteos.historiasAceptadas() + " de " + conteos.historias() + " aceptadas");
            case SIN_BUGS_ALTA -> new CriterioCierre(id, descripcion, false, conteos.bugsAltaAbiertos() == 0,
                    conteos.bugsAltaAbiertos() == 0 ? AUTOMATICO
                            : conteos.bugsAltaAbiertos() + (conteos.bugsAltaAbiertos() == 1 ? " abierto" : " abiertos"));
            case MODULOS_EN_STAGING -> new CriterioCierre(id, descripcion, true, guardado,
                    guardado ? null : "Lo marca el Líder cuando el CI despliega", guardado ? cumplidoPor : null,
                    guardado ? cumplidoEn : null);
        };
    }
}
