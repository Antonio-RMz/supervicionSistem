package com.team.gestorproyectos.ticket.domain.port.in;

import java.util.List;

import com.team.gestorproyectos.historia.domain.model.NuevaImagen;

/**
 * Las imágenes de un ticket (por ejemplo, la captura de un bug al reportarlo). Las ve y las sube cualquiera
 * del equipo del proyecto (o un Admin).
 */
public interface ImagenesTicketUseCase {

    /** Máximo de imágenes por ticket. */
    int MAXIMO_POR_TICKET = 5;

    List<ImagenDeTicket> listar(Long proyectoId, Long ticketId, Long usuarioId, boolean esAdmin);

    void subir(Long proyectoId, Long ticketId, Long usuarioId, boolean esAdmin, NuevaImagen imagen);

    Imagen ver(Long proyectoId, Long ticketId, Long imagenId, Long usuarioId, boolean esAdmin);

    record ImagenDeTicket(Long id, String nombre, long tamanoBytes) {
    }

    record Imagen(String nombre, String tipoMime, byte[] contenido) {
    }
}
