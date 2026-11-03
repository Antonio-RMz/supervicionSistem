package com.team.gestorproyectos.historia.domain.model;

import java.util.List;

import com.team.gestorproyectos.historia.domain.model.HistoriaParaGestionar.FaseDestino;

/**
 * El detalle de la historia (PDF pág. 24) con el panel de Acciones de quien la ve (PDF pág. 21).
 *
 * @param actor             quién decide en este estado: "Analista", "Líder" o "Nadie"
 * @param meToca            si quien la ve es ese actor (o el Admin)
 * @param ticketsSinEmpezar los que se cancelarían al descartarla
 * @param ticketsReabribles los que el Analista puede reabrir al devolverla
 * @param puedeEliminar     la puede borrar del todo: aún no se inicia y es el Líder del proyecto o un Admin
 * @param desarrollo        con Desarrolladores (sin Actividades): sus Desarrolladores, "Lo que hice" y repositorios;
 *                          null si la historia se trabaja con Actividades
 */
public record DetalleHistoria(
        InfoHistoria info,
        String actor,
        boolean meToca,
        List<AccionDisponible> acciones,
        boolean puedeVerificarCriterios,
        boolean puedeEditar,
        boolean puedeAdjuntar,
        boolean puedeCrearTicket,
        int ticketsSinEmpezar,
        List<TicketReabrible> ticketsReabribles,
        List<FaseDestino> fasesDestino,
        boolean puedeEliminar,
        DesarrolloHistoria desarrollo) {

    public record AccionDisponible(AccionHistoria accion, String texto, List<String> impedimentos) {
    }

    public record TicketReabrible(Long id, String codigo, String titulo) {
    }
}
