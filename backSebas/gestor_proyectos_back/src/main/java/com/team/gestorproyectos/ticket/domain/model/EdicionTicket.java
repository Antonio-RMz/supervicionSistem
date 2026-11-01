package com.team.gestorproyectos.ticket.domain.model;

/** Lo que necesita el formulario para editar un ticket: lo mismo que uno nuevo, más lo que ya tiene. */
public record EdicionTicket(PreparacionTicket preparacion, TicketEditable ticket) {
}
