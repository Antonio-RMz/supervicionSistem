package com.team.gestorproyectos.proyecto.domain.model;

import java.util.List;

/** @param sueltos tickets de la fase que no cuelgan de una historia (bugs, tareas técnicas...) */
public record FaseArbol(FaseResumen fase, List<HistoriaArbol> historias, List<TicketArbol> sueltos) {
}
