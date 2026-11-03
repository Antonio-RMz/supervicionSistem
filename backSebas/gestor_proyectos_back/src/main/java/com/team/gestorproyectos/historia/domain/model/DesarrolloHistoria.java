package com.team.gestorproyectos.historia.domain.model;

import java.util.List;

/**
 * La pestaña Desarrollo de una historia con Desarrolladores (sin Actividades como ticket).
 *
 * @param miParteTerminada si quien la ve es uno de sus Desarrolladores: si ya terminó su parte; null si no lo es
 * @param puedeAnotar      puede agregar o quitar puntos de "Lo que hice"
 * @param puedeMarcar      puede marcarlos como hechos (sus Desarrolladores, mientras está en desarrollo)
 * @param requierePr       el proyecto pide PR: al terminar la revisa el Líder
 */
public record DesarrolloHistoria(
        List<DesarrolladorDeHistoria> desarrolladores,
        List<TrabajoDeHistoria> trabajo,
        List<RepositorioDeHistoria> repositorios,
        Boolean miParteTerminada,
        boolean puedeAnotar,
        boolean puedeMarcar,
        boolean requierePr) {
}
