package com.team.gestorproyectos.ticket.domain.model;

import java.util.List;

/** @param faseId la fase que se está viendo (null si el proyecto no tiene fases) */
public record VistaTablero(List<FaseTablero> fases, Long faseId, List<Tarjeta> tarjetas) {
}
