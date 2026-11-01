package com.team.gestorproyectos.ticket.domain.model;

/**
 * Un ticket de trabajo tal como se capturó, para corregirlo antes de empezar (mismo formulario que Nuevo ticket).
 *
 * @param datos lo que se captura; las subtareas no van aquí: se agregan y quitan desde el detalle
 */
public record TicketEditable(Long id, String codigo, String estado, Long creadoPorId, Long liderId, NuevoTicket datos) {

    /** Se edita mientras siga en su estado inicial; lo editan quien lo creó, el Líder o un Admin. */
    public boolean puedeEditar(Long usuarioId, boolean esAdmin) {
        return sinEmpezar() && (esAdmin || usuarioId.equals(creadoPorId) || usuarioId.equals(liderId));
    }

    public boolean sinEmpezar() {
        return datos.tipo().sinEmpezar(estado);
    }

    /** Por qué no puede editarlo, para el 403. */
    public String motivoSinEdicion() {
        return sinEmpezar() ? "El ticket lo editan quien lo creó o el Líder del proyecto"
                : "El ticket ya empezó: ya no se edita";
    }
}
