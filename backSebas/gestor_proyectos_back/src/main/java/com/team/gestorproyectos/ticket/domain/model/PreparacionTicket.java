package com.team.gestorproyectos.ticket.domain.model;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Lo que necesita el formulario de Nuevo ticket.
 *
 * @param siguientes  el número que tendría un ticket de cada tipo: {ACTV=41, BUGS=32...}
 * @param abiertos    tickets del proyecto de los que el nuevo puede depender
 */
public record PreparacionTicket(
        Long proyectoId,
        String proyectoNombre,
        Long liderId,
        List<FaseDeTicket> fases,
        List<PersonaDeEquipo> equipo,
        Map<TipoTicket, Integer> siguientes,
        List<TicketAbierto> abiertos) {

    public record FaseDeTicket(Long id, int numero, String nombre, List<ModuloDeTicket> modulos,
                               List<HistoriaDeTicket> historias) {
    }

    public record ModuloDeTicket(Long id, String tipo, String nombre, String tecnologia, String repositorio,
                                 String ramaBase) {
    }

    /**
     * Una historia que ya puede tener tickets (Lista o En desarrollo).
     *
     * @param moduloIds módulos de la historia; si no tiene, aplican los de la fase
     */
    /** @param conDesarrolladores le llega directo a sus Desarrolladores: no lleva Actividades, solo bugs o mejoras */
    public record HistoriaDeTicket(Long id, int numero, String titulo, String estado, String como, String quiero,
                                   String para, String analistaNombre, Set<Long> moduloIds, long tickets,
                                   List<CriterioDeHistoria> criterios, boolean conDesarrolladores) {
    }

    /** @param cubiertoPor "ACTV-42 · App móvil" si otro ticket ya lo cubre; null si no */
    public record CriterioDeHistoria(Long id, String descripcion, String cubiertoPor) {
    }

    public record PersonaDeEquipo(Long id, String nombreCompleto, List<String> roles) {

        /** Solo un Desarrollador hace tickets. */
        public boolean esDesarrollador() {
            return roles.contains("DESARROLLADOR");
        }
    }

    public record TicketAbierto(Long id, String codigo, String titulo, String estado) {
    }
}
