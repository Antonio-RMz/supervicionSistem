package com.team.gestorproyectos.historia.domain.model;

/**
 * El botón de una historia en las listas (PDF pág. 21, "Qué ve cada uno en la lista").
 *
 * @param impedimento por qué todavía no se puede; null si ya se puede
 */
public record AccionDeLista(Long historiaId, String estado, AccionHistoria accion, String texto,
                            String impedimento) {
}
