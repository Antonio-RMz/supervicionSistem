package com.team.gestorproyectos.historia.domain.model;

import java.util.List;
import java.util.Set;

/** Una fase donde se pueden agregar historias, con los módulos que la historia puede tomar de ella. */
public record FaseParaHistoria(
        Long id,
        int numero,
        String nombre,
        String estado,
        List<Opcion> modulos) {

    /** Una fase cerrada, cancelada o la de Mantenimiento ya no recibe historias. */
    private static final Set<String> RECIBE_HISTORIAS = Set.of("PLANEADA", "EN_CURSO", "PAUSADA");

    public static boolean recibeHistorias(String estado, boolean mantenimiento) {
        return !mantenimiento && RECIBE_HISTORIAS.contains(estado);
    }

    /** @param tipo API, WEB, MOVIL... */
    public record Opcion(Long id, String tipo, String nombre) {
    }
}
