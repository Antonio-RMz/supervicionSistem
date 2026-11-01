package com.team.gestorproyectos.usuario.infrastructure.persistence.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.team.gestorproyectos.usuario.infrastructure.persistence.entity.PermisoEntity;

public interface PermisoJpaRepository extends JpaRepository<PermisoEntity, Long> {

    List<PermisoEntity> findAllByOrderById();

    List<PermisoEntity> findByCodigoIn(Collection<String> codigos);
}
