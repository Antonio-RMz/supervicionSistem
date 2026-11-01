package com.team.gestorproyectos.usuario.infrastructure.persistence.mapper;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.team.gestorproyectos.usuario.domain.model.Permiso;
import com.team.gestorproyectos.usuario.domain.model.PermisoExtra;
import com.team.gestorproyectos.usuario.domain.model.Rol;
import com.team.gestorproyectos.usuario.domain.model.Usuario;
import com.team.gestorproyectos.usuario.infrastructure.persistence.entity.PermisoEntity;
import com.team.gestorproyectos.usuario.infrastructure.persistence.entity.RolEntity;
import com.team.gestorproyectos.usuario.infrastructure.persistence.entity.UsuarioEntity;
import com.team.gestorproyectos.usuario.infrastructure.persistence.repository.UsuarioPermisoJpaRepository.PermisoExtraFila;

/** Entidad JPA -> modelo de dominio. MapStruct genera la implementación al compilar. */
@Mapper(componentModel = "spring")
public interface UsuarioEntityMapper {

    @Mapping(target = "clienteId", source = "cliente.id")
    @Mapping(target = "clienteNombre", source = "cliente.nombreCorto")
    Usuario aDominio(UsuarioEntity entity);

    Rol aRol(RolEntity entity);

    List<Rol> aRoles(List<RolEntity> entities);

    Permiso aPermiso(PermisoEntity entity);

    List<Permiso> aPermisos(List<PermisoEntity> entities);

    PermisoExtra aPermisoExtra(PermisoExtraFila fila);

    List<PermisoExtra> aPermisosExtra(List<PermisoExtraFila> filas);

    default Set<String> codigosDeRol(Set<RolEntity> roles) {
        return roles.stream().map(RolEntity::getCodigo).collect(Collectors.toSet());
    }

    default Set<String> codigosDePermiso(Set<PermisoEntity> permisos) {
        return permisos.stream().map(PermisoEntity::getCodigo).collect(Collectors.toSet());
    }
}
