package com.team.gestorproyectos.proyecto.infrastructure.persistence.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.team.gestorproyectos.proyecto.infrastructure.persistence.entity.DemoEntity;

public interface DemoJpaRepository extends JpaRepository<DemoEntity, Long> {

    @Query("""
            select d from DemoEntity d
            where d.estado = 'PENDIENTE'
              and d.faseId in (select f.id from FaseEntity f where f.proyectoId = :proyectoId)
            order by d.id
            """)
    List<DemoEntity> buscarPendientesDelProyecto(@Param("proyectoId") Long proyectoId);
}
