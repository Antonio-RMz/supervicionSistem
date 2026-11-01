package com.team.gestorproyectos.proyecto.infrastructure.persistence;

import java.util.List;

import org.springframework.stereotype.Component;

import com.team.gestorproyectos.proyecto.domain.model.UsuarioDelSistema;
import com.team.gestorproyectos.proyecto.domain.port.out.UsuariosDelSistemaPort;
import com.team.gestorproyectos.proyecto.infrastructure.persistence.repository.UsuariosDelSistemaJpaRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UsuariosDelSistemaAdapter implements UsuariosDelSistemaPort {

    private final UsuariosDelSistemaJpaRepository repository;

    @Override
    public List<UsuarioDelSistema> usuarios(Long proyectoId) {
        return repository.usuarios(proyectoId).stream()
                .map(u -> new UsuarioDelSistema(u.getNombre(), u.getDescripcion()))
                .toList();
    }

    @Override
    public void reemplazar(Long proyectoId, List<UsuarioDelSistema> usuarios) {
        repository.quitarTodos(proyectoId);
        for (int i = 0; i < usuarios.size(); i++) {
            repository.agregar(proyectoId, usuarios.get(i).nombre(), usuarios.get(i).descripcion(), i + 1);
        }
    }
}
