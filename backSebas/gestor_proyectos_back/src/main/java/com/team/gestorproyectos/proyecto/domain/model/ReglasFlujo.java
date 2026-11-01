package com.team.gestorproyectos.proyecto.domain.model;

/** Las cuatro reglas del flujo que se eligen al crear el proyecto (PDF pág. 16). */
public record ReglasFlujo(boolean permitirAutovalidacion, boolean comentarioAlRechazar, boolean ramaAlIniciar,
                          boolean notificarClienteFase) {
}
