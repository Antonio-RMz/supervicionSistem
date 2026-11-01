package com.team.gestorproyectos.usuario.domain.model;

import com.team.gestorproyectos.shared.domain.model.ItemPendiente;

/** Una fila de "Trabajo actual" de una persona. */
public record Tarea(ItemPendiente pendiente, boolean rechazadaEnQa) {
}
