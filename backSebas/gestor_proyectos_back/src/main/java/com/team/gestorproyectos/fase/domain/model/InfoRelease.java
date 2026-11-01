package com.team.gestorproyectos.fase.domain.model;

import java.time.LocalDateTime;
import java.util.List;

import com.team.gestorproyectos.fase.domain.model.NotasRelease.Cambio;

/**
 * El release de una fase (PDF pág. 27): lo que sube a main por módulo, qué incluye, sus notas,
 * quién aprobó y a qué versión se puede volver.
 *
 * @param estado        del release: PREPARANDO, PUBLICADA, REVERTIDA; null si aún no se prepara
 * @param enProduccion  la versión que está hoy en producción (puede ser esta)
 * @param notas         las que guardó el Líder; null si aún no las edita (se usan las sugeridas)
 * @param cambios       los tickets de la fase que entran en las notas
 * @param demo          la demo que revisó el cliente; null si no hay
 */
public record InfoRelease(
        String version,
        String estado,
        LocalDateTime publicadoEn,
        String publicadoPor,
        VersionPublicada enProduccion,
        String liderNombre,
        List<ModuloRelease> modulos,
        String notas,
        boolean enviarNotas,
        List<Cambio> cambios,
        Demo demo) {

    public record VersionPublicada(String version, LocalDateTime fecha) {
    }

    public record ModuloRelease(String tipo, String nombre, String tecnologia) {
    }

    /** @param estado PENDIENTE, APROBADA o CON_CAMBIOS */
    public record Demo(String estado, String revisadaPor, LocalDateTime revisadaEn) {
    }

    /** Las notas que se ven: las que editó el Líder o, si no, las armadas con los tickets. */
    public String notasVigentes() {
        return notas != null ? notas : NotasRelease.sugeridas(cambios);
    }
}
