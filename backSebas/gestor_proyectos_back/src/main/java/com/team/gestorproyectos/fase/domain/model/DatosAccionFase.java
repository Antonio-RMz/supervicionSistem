package com.team.gestorproyectos.fase.domain.model;

import java.time.LocalDate;

/**
 * Lo que llega con la acción, según cuál sea (PDF pág. 25).
 *
 * @param estadoEsperado     el estado que vio quien presionó; si ya cambió, no se aplica
 * @param motivo             Pausar, Cancelar y Revertir ("¿Qué falló?")
 * @param reanudarEl         Pausar, opcional
 * @param versionConfirmada  Subir a main: hay que escribir la versión para confirmar
 * @param asignarBugA        Revertir: a quién se asigna el BUGS que se crea
 */
public record DatosAccionFase(String estadoEsperado, String motivo, LocalDate reanudarEl, String versionConfirmada,
                              Long asignarBugA) {
}
