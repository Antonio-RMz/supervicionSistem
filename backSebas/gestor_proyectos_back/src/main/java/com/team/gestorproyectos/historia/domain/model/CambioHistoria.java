package com.team.gestorproyectos.historia.domain.model;

/**
 * Un cambio de estado de la historia ya validado, con sus efectos.
 *
 * @param ticketAReabrir  al devolver: el ticket que regresa con su Desarrollador
 * @param estadoAlReabrir a qué estado regresa ese ticket
 * @param bug             al devolver: el bug que se crea dentro de la historia
 * @param faseDestinoId   al moverla de fase
 * @param avanzaSolaA     al marcarla Lista si ya tiene una Actividad Lista: el estado al que pasa sola enseguida
 *                        (Lista para desarrollo, o En desarrollo si su fase ya corre); null si se queda
 */
public record CambioHistoria(
        Long historiaId,
        Long proyectoId,
        Long faseId,
        AccionHistoria accion,
        String estadoAnterior,
        String estadoNuevo,
        String motivo,
        Long ticketAReabrir,
        String estadoAlReabrir,
        BugDeDevolucion bug,
        Long faseDestinoId,
        String avanzaSolaA) {

    /** Lo que vale para mostrar: ya con el paso automático si lo hubo. */
    public String estadoFinal() {
        return avanzaSolaA == null ? estadoNuevo : avanzaSolaA;
    }

    /** Lo necesario para crear el BUGS: a quién se asigna y quién lo prueba. */
    public record BugDeDevolucion(String titulo, String descripcion, Long responsableId, Long testerId) {
    }
}
