package com.team.gestorproyectos.shared.domain.model;

import java.time.LocalDateTime;

/**
 * Un ticket o una historia abiertos. Se tratan igual en el Dashboard.
 *
 * @param tipo           ACTV, BUGS, MEJR o HIST
 * @param responsableId  en una historia es su Analista
 * @param historiaEstado estado de la historia del ticket (null si no tiene o si el item es una historia);
 *                       en la fila de cada Desarrollador de una historia, {@link #FILA_DE_DESARROLLADOR}
 * @param requierePr     la Actividad termina con su PR aprobado; false en un proyecto Normal sin PR (se termina)
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
        LocalDateTime actualizadoEn,
        boolean requierePr) {

    /**
     * Una historia con Desarrolladores sale una vez por cada uno (con él como responsable) mientras la desarrollan:
     * esa fila lleva esta marca en historiaEstado. La fila normal de la historia es la de su Analista.
     */
    public static final String FILA_DE_DESARROLLADOR = "DEV";

    public boolean esHistoriaDeDesarrollador() {
        return "HIST".equals(tipo) && FILA_DE_DESARROLLADOR.equals(historiaEstado);
    }

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
