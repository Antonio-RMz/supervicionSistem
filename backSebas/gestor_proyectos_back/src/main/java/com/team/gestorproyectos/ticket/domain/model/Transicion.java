package com.team.gestorproyectos.ticket.domain.model;

/**
 * Un cambio de estado ya validado, listo para guardarse en el ticket y en su historial.
 *
 * @param ramaNueva     rama que se crea al iniciar el trabajo; null si no aplica o ya tenía
 * @param bloqueadoPorId al bloquearlo por otro ticket: cuál; se desbloquea solo cuando ese se termina
 * @param severidad      al confirmar un bug: la que se le asigna; null si no cambia
 * @param responsableId  al confirmar un bug o aprobar una mejora: quién lo hará; null si no cambia
 */
public record Transicion(Long ticketId, String accion, String estadoAnterior, String estadoNuevo, String motivo,
                         String ramaNueva, Long bloqueadoPorId, String severidad, Long responsableId) {

    public Transicion conMotivo(String nuevoMotivo) {
        return new Transicion(ticketId, accion, estadoAnterior, estadoNuevo, nuevoMotivo, ramaNueva, bloqueadoPorId,
                severidad, responsableId);
    }
}
