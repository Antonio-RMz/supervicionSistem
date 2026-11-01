package com.team.gestorproyectos.proyecto.infrastructure.persistence.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.team.gestorproyectos.proyecto.infrastructure.persistence.entity.HistoriaEntity;

public interface HistoriaJpaRepository extends JpaRepository<HistoriaEntity, Long> {

    List<HistoriaEntity> findByFaseIdAndEstadoNotOrderByNumero(Long faseId, String estado);
}
