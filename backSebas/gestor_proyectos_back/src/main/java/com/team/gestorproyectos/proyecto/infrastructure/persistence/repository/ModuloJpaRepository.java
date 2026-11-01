package com.team.gestorproyectos.proyecto.infrastructure.persistence.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.team.gestorproyectos.proyecto.infrastructure.persistence.entity.ModuloEntity;

public interface ModuloJpaRepository extends JpaRepository<ModuloEntity, Long> {

    List<ModuloEntity> findByProyectoIdOrderById(Long proyectoId);
}
