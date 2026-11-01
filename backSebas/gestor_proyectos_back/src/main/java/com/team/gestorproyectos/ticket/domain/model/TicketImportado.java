package com.team.gestorproyectos.ticket.domain.model;

import java.util.List;

/**
 * Un ticket del JSON de "Importar tickets", tal como lo devuelve la IA: lo necesario para crearlo y sus subtareas.
 *
 * @param repositorio el tipo ("API", "Web") o el nombre del repositorio; puede faltar si la historia tiene uno solo
 * @param subtareas   el trabajo técnico concreto, paso por paso (tablas, vistas, triggers, servicios, pantallas...)
 */
public record TicketImportado(String titulo, String descripcion, String repositorio, List<String> subtareas) {
}
