package com.team.gestorproyectos.usuario.infrastructure.persistence;

import java.util.List;

import org.springframework.stereotype.Component;

import com.team.gestorproyectos.usuario.domain.model.Permiso;
import com.team.gestorproyectos.usuario.domain.model.Rol;
import com.team.gestorproyectos.usuario.domain.port.out.CatalogoAccesosPort;
import com.team.gestorproyectos.usuario.infrastructure.persistence.mapper.UsuarioEntityMapper;
import com.team.gestorproyectos.usuario.infrastructure.persistence.repository.PermisoJpaRepository;
import com.team.gestorproyectos.usuario.infrastructure.persistence.repository.RolJpaRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CatalogoAccesosAdapter implements CatalogoAccesosPort {

    private final RolJpaRepository roles;
    private final PermisoJpaRepository permisos;
    private final UsuarioEntityMapper mapper;

    @Override
    public List<Rol> roles() {
        return mapper.aRoles(roles.findAllByOrderById());
    }

    @Override
    public List<Permiso> permisos() {
        return mapper.aPermisos(permisos.findAllByOrderById());
    }
}
