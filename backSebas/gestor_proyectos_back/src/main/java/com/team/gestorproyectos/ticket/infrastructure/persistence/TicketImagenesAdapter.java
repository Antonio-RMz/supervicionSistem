package com.team.gestorproyectos.ticket.infrastructure.persistence;

import java.util.List;

import org.springframework.stereotype.Component;

import com.team.gestorproyectos.historia.domain.model.NuevaImagen;
import com.team.gestorproyectos.ticket.domain.port.out.TicketImagenesPort;
import com.team.gestorproyectos.ticket.infrastructure.persistence.repository.TicketImagenesJpaRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TicketImagenesAdapter implements TicketImagenesPort {

    private final TicketImagenesJpaRepository repository;

    @Override
    public boolean existe(Long proyectoId, Long ticketId) {
        return repository.contar(proyectoId, ticketId) > 0;
    }

    @Override
    public List<ImagenGuardada> imagenes(Long ticketId) {
        return repository.adjuntos(ticketId).stream()
                .map(a -> new ImagenGuardada(a.getId(), a.getNombre(), a.getTipoMime(),
                        a.getTamanoBytes() == null ? 0 : a.getTamanoBytes(), a.getRuta()))
                .toList();
    }

    @Override
    public void agregar(Long ticketId, NuevaImagen imagen, String ruta, Long usuarioId) {
        repository.agregar(ticketId, imagen.nombre(), imagen.tipoMime(), imagen.tamanoBytes(), ruta, usuarioId);
    }
}
