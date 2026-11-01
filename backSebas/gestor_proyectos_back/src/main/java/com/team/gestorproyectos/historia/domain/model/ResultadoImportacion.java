package com.team.gestorproyectos.historia.domain.model;

/**
 * Lo que dejó "Importar historias".
 *
 * @param incompletas cuántas aún no se pueden marcar Lista (les falta Quiero, Para o criterios)
 */
public record ResultadoImportacion(int creadas, int incompletas) {
}
