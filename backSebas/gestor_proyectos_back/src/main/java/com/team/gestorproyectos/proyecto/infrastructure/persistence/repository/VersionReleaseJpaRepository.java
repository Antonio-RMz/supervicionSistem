package com.team.gestorproyectos.proyecto.infrastructure.persistence.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.team.gestorproyectos.proyecto.infrastructure.persistence.entity.VersionReleaseEntity;

public interface VersionReleaseJpaRepository extends JpaRepository<VersionReleaseEntity, Long> {

    List<VersionReleaseEntity> findByProyectoIdAndEstadoOrderByPublicadoEnDesc(Long proyectoId, String estado);
}
