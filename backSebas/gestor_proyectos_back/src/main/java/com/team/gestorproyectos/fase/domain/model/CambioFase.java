package com.team.gestorproyectos.fase.domain.model;

import java.time.LocalDate;

/**
 * Un cambio de estado de la fase ya validado, listo para guardarse con sus efectos
 * (cancelar historias y tickets, publicar el release, crear el BUGS al revertir...).
 */
public record CambioFase(
        Long faseId,
        Long proyectoId,
        AccionFase accion,
        String estadoAnterior,
        String estadoNuevo,
        String motivo,
        LocalDate reanudarEl,
        String version,
        Long asignarBugA) {
}
