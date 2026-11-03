package com.team.gestorproyectos.ticket.domain.port.out;

import java.util.List;

import com.team.gestorproyectos.historia.domain.model.NuevaImagen;

/** Las imágenes de un ticket, guardadas en la tabla adjunto con entidad TICKET. */
public interface TicketImagenesPort {

    /** Si el ticket existe y es de ese proyecto. */
    boolean existe(Long proyectoId, Long ticketId);

    List<ImagenGuardada> imagenes(Long ticketId);

    void agregar(Long ticketId, NuevaImagen imagen, String ruta, Long usuarioId);

    record ImagenGuardada(Long id, String nombre, String tipoMime, long tamanoBytes, String ruta) {
    }
}
