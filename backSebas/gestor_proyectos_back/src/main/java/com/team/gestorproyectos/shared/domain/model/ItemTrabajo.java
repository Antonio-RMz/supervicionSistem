package com.team.gestorproyectos.shared.domain.model;

import java.time.LocalDateTime;

/**
 * Un ticket o una historia abiertos. Se tratan igual en el Dashboard.
 *
 * @param tipo           ACTV, BUGS, MEJR o HIST
 * @param responsableId  en una historia es su Analista
 * @param historiaEstado estado de la historia del ticket (null si no tiene o si el item es una historia)
 */
public record ItemTrabajo(
        Long id,
        String tipo,
        int numero,
        String titulo,
        String estado,
        String prioridad,
        Long responsableId,
        Long testerId,
        Long creadoPorId,
        String historiaEstado,
        LocalDateTime creadoEn,
        LocalDateTime actualizadoEn) {

    /** "BUGS-31", "ACTV-05" */
    public String codigo() {
        return CodigoItem.de(tipo, numero);
    }

    public boolean esTicket() {
        return !"HIST".equals(tipo);
    }

    /** Si la persona está involucrada: lo trabaja, lo prueba o lo creó. */
    public boolean participa(Long usuarioId) {
        return usuarioId.equals(responsableId) || usuarioId.equals(testerId) || usuarioId.equals(creadoPorId);
    }

    /** ALTA primero. */
    public int ordenPrioridad() {
        return switch (prioridad == null ? "" : prioridad) {
            case "ALTA" -> 0;
            case "MEDIA" -> 1;
            default -> 2;
        };
    }
}
