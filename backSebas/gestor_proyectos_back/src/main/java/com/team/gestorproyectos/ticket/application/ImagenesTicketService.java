package com.team.gestorproyectos.ticket.application;

import java.util.List;

import com.team.gestorproyectos.historia.domain.model.NuevaImagen;
import com.team.gestorproyectos.shared.application.ServicioAplicacion;
import com.team.gestorproyectos.shared.application.Transaccional;
import com.team.gestorproyectos.shared.domain.exception.NoEncontradoException;
import com.team.gestorproyectos.shared.domain.exception.ReglaNegocioException;
import com.team.gestorproyectos.shared.domain.port.ArchivosPort;
import com.team.gestorproyectos.ticket.domain.port.in.ImagenesTicketUseCase;
import com.team.gestorproyectos.ticket.domain.port.out.TicketConsultaPort;
import com.team.gestorproyectos.ticket.domain.port.out.TicketImagenesPort;
import com.team.gestorproyectos.ticket.domain.port.out.TicketImagenesPort.ImagenGuardada;

import lombok.RequiredArgsConstructor;

@ServicioAplicacion
@RequiredArgsConstructor
public class ImagenesTicketService implements ImagenesTicketUseCase {

    private final TicketConsultaPort proyectos;
    private final TicketImagenesPort tickets;
    private final ArchivosPort archivos;

    @Override
    @Transaccional(soloLectura = true)
    public List<ImagenDeTicket> listar(Long proyectoId, Long ticketId, Long usuarioId, boolean esAdmin) {
        verificar(proyectoId, ticketId, usuarioId, esAdmin);
        return tickets.imagenes(ticketId).stream()
                .map(i -> new ImagenDeTicket(i.id(), i.nombre(), i.tamanoBytes())).toList();
    }

    @Override
    @Transaccional
    public void subir(Long proyectoId, Long ticketId, Long usuarioId, boolean esAdmin, NuevaImagen imagen) {
        verificar(proyectoId, ticketId, usuarioId, esAdmin);
        if (tickets.imagenes(ticketId).size() >= MAXIMO_POR_TICKET) {
            throw new ReglaNegocioException("El ticket ya tiene " + MAXIMO_POR_TICKET + " imágenes");
        }
        String ruta = archivos.guardar(imagen.contenido(), imagen.extension());
        try {
            tickets.agregar(ticketId, imagen, ruta, usuarioId);
        } catch (RuntimeException e) {
            archivos.borrar(ruta);
            throw e;
        }
    }

    @Override
    @Transaccional(soloLectura = true)
    public Imagen ver(Long proyectoId, Long ticketId, Long imagenId, Long usuarioId, boolean esAdmin) {
        verificar(proyectoId, ticketId, usuarioId, esAdmin);
        ImagenGuardada imagen = tickets.imagenes(ticketId).stream().filter(i -> i.id().equals(imagenId)).findFirst()
                .orElseThrow(() -> new NoEncontradoException("No encontramos esa imagen"));
        return new Imagen(imagen.nombre(), imagen.tipoMime(), archivos.leer(imagen.ruta()));
    }

    private void verificar(Long proyectoId, Long ticketId, Long usuarioId, boolean esAdmin) {
        if (!proyectos.proyectoVisible(proyectoId, usuarioId, esAdmin)) {
            throw new NoEncontradoException("No encontramos ese proyecto");
        }
        if (!tickets.existe(proyectoId, ticketId)) {
            throw new NoEncontradoException("No encontramos ese ticket");
        }
    }
}
