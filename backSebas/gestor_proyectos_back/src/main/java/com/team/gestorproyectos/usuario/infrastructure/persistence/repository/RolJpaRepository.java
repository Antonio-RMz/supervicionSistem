package com.team.gestorproyectos.usuario.infrastructure.persistence.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.team.gestorproyectos.usuario.infrastructure.persistence.entity.RolEntity;

public interface RolJpaRepository extends JpaRepository<RolEntity, Long> {

    /** En el orden en que se dieron de alta: Administrador, Líder, Analista y Desarrollador. */
    @EntityGraph(attributePaths = "permisos")
    List<RolEntity> findAllByOrderById();

    List<RolEntity> findByCodigoIn(Collection<String> codigos);
}
