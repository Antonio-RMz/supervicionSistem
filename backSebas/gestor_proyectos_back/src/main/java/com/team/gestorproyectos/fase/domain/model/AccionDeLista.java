package com.team.gestorproyectos.fase.domain.model;

/**
 * El único botón de una fase en las listas del Dashboard y de Mis tickets
 * (PDF pág. 20, "Qué ve cada uno en la lista").
 *
 * @param numero      y nombre, para mostrarla como "Fase 4 · Portal móvil"
 * @param impedimento por qué todavía no se puede ("BUGS-50 sigue abierto"); null si ya se puede
 */
public record AccionDeLista(Long faseId, int numero, String nombre, String estado, AccionFase accion, String texto,
                            String impedimento) {
}
