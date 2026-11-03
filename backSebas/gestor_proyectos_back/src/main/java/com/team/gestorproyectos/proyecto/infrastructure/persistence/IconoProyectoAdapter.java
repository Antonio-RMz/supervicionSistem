package com.team.gestorproyectos.proyecto.infrastructure.persistence;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.team.gestorproyectos.proyecto.domain.port.out.IconoProyectoPort;
import com.team.gestorproyectos.proyecto.infrastructure.persistence.repository.IconoProyectoJpaRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class IconoProyectoAdapter implements IconoProyectoPort {

    private final IconoProyectoJpaRepository repository;

    @Override
    public Optional<ProyectoConIcono> visible(Long proyectoId, Long usuarioId, boolean esAdmin) {
        return repository.visible(proyectoId, usuarioId, esAdmin)
                .map(f -> new ProyectoConIcono(f.getLiderId(), f.getRuta(), f.getTipoMime()));
    }

    @Override
    public void guardar(Long proyectoId, String ruta, String tipoMime) {
        repository.guardar(proyectoId, ruta, tipoMime);
    }
}
