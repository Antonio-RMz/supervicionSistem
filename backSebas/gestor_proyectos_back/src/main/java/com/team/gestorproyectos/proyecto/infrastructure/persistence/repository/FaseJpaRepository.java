package com.team.gestorproyectos.proyecto.infrastructure.persistence.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.team.gestorproyectos.proyecto.infrastructure.persistence.entity.FaseEntity;

public interface FaseJpaRepository extends JpaRepository<FaseEntity, Long> {

    /** Fases normales (sin Mantenimiento), en orden. */
    List<FaseEntity> findByProyectoIdAndEsMantenimientoFalseOrderByNumero(Long proyectoId);
}
