package com.team.gestorproyectos.usuario.infrastructure.persistence;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Component;

import com.team.gestorproyectos.shared.domain.model.Movimiento;
import com.team.gestorproyectos.shared.domain.model.Persona;
import com.team.gestorproyectos.usuario.domain.model.ComentarioHecho;
import com.team.gestorproyectos.shared.domain.model.ItemDeProyecto;
import com.team.gestorproyectos.usuario.domain.model.ProyectoDeUsuario;
import com.team.gestorproyectos.usuario.domain.port.out.PerfilUsuarioConsultaPort;
import com.team.gestorproyectos.usuario.infrastructure.persistence.mapper.PerfilUsuarioPersistenceMapper;
import com.team.gestorproyectos.usuario.infrastructure.persistence.repository.PerfilUsuarioJpaRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PerfilUsuarioConsultaAdapter implements PerfilUsuarioConsultaPort {

    private final PerfilUsuarioJpaRepository repository;
    private final PerfilUsuarioPersistenceMapper mapper;

    @Override
    public List<ItemDeProyecto> itemsAbiertos(Long usuarioId) {
        return mapper.aItemsDeProyecto(repository.itemsAbiertos(usuarioId));
    }

    @Override
    public List<Persona> personasDelEquipo() {
        return mapper.aPersonas(repository.personasDelEquipo());
    }

    @Override
    public long ticketsCerradosDesde(Long usuarioId, LocalDateTime desde) {
        return repository.ticketsCerradosDesde(usuarioId, desde);
    }

    @Override
    public long rechazosEnQaDesde(Long usuarioId, LocalDateTime desde) {
        return repository.rechazosEnQaDesde(usuarioId, desde);
    }

    @Override
    public List<Movimiento> movimientosDe(Long usuarioId, int limite) {
        return mapper.aMovimientos(repository.movimientosDe(usuarioId, limite));
    }

    @Override
    public List<ComentarioHecho> comentariosDe(Long usuarioId, int limite) {
        return mapper.aComentarios(repository.comentariosDe(usuarioId, limite));
    }

    @Override
    public List<ProyectoDeUsuario> proyectosDe(Long usuarioId) {
        return mapper.aProyectos(repository.proyectosDe(usuarioId));
    }
}
