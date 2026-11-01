package com.team.gestorproyectos.proyecto.infrastructure.persistence.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.team.gestorproyectos.proyecto.infrastructure.persistence.entity.TicketEntity;

public interface TicketJpaRepository extends JpaRepository<TicketEntity, Long> {

    /** Cuántos tickets hay por fase, módulo y estado. Con esto se calcula el avance. */
    @Query("""
            select new com.team.gestorproyectos.proyecto.infrastructure.persistence.repository.ConteoTicketsFila(
                       t.faseId, t.moduloId, t.estado, count(t))
            from TicketEntity t
            where t.proyectoId = :proyectoId
            group by t.faseId, t.moduloId, t.estado
            """)
    List<ConteoTicketsFila> contarPorFaseModuloYEstado(@Param("proyectoId") Long proyectoId);
}
