package com.team.gestorproyectos.historia.infrastructure.persistence.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import com.team.gestorproyectos.proyecto.infrastructure.persistence.entity.HistoriaEntity;

/** La lista de historias de un proyecto (menú Historias). */
public interface HistoriaListadoJpaRepository extends Repository<HistoriaEntity, Long> {

    /** Terminados y "fuera" con los mismos estados que ConteoTickets. */
    @Query(value = """
            SELECT h.id AS id, h.numero AS numero, h.titulo AS titulo, h.estado AS estado, h.prioridad AS prioridad,
                   h.puntos AS puntos, f.id AS faseId, f.numero AS faseNumero, f.nombre AS faseNombre,
                   CONCAT(a.nombre, COALESCE(CONCAT(' ', SUBSTRING_INDEX(a.apellidos, ' ', 1)), '')) AS analistaNombre,
                   (SELECT COUNT(*) FROM ticket t WHERE t.historia_id = h.id
                      AND t.estado IN ('APROBADA', 'APROBADO', 'APROBADA_QA', 'HECHA', 'INTEGRADA', 'INTEGRADO',
                                       'PUBLICADO', 'PUBLICADA', 'CERRADA', 'CERRADO', 'REVISADA')) AS ticketsTerminados,
                   (SELECT COUNT(*) FROM ticket t WHERE t.historia_id = h.id
                      AND t.estado NOT IN ('CANCELADA', 'NO_REPRODUCIBLE', 'RECHAZADA')) AS ticketsTotal,
                   h.actualizado_en AS actualizadoEn
            FROM historia h
            JOIN fase f    ON f.id = h.fase_id
            JOIN usuario a ON a.id = h.analista_id
            WHERE h.proyecto_id = :proyectoId
            ORDER BY h.numero DESC
            """, nativeQuery = true)
    List<HistoriaFila> historias(@Param("proyectoId") Long proyectoId);

    @Query(value = """
            SELECT p.nombre AS proyectoNombre, f.numero AS numero, f.nombre AS nombre
            FROM fase f JOIN proyecto p ON p.id = f.proyecto_id
            WHERE f.id = :faseId AND f.proyecto_id = :proyectoId
            """, nativeQuery = true)
    Optional<FaseFila> fase(@Param("proyectoId") Long proyectoId, @Param("faseId") Long faseId);

    /** Con el Figma que se ve en su detalle: el suyo o, si no tiene, el de la fase. */
    @Query(value = """
            SELECT h.id AS id, h.numero AS numero, h.estado AS estado, h.titulo AS titulo, h.como AS como,
                   h.quiero AS quiero, h.para AS para, h.prioridad AS prioridad, h.puntos AS puntos,
                   COALESCE(h.figma_url, IF(f.figma_para_todas, f.figma_url, NULL)) AS figmaUrl
            FROM historia h JOIN fase f ON f.id = h.fase_id
            WHERE h.fase_id = :faseId
            ORDER BY h.numero
            """, nativeQuery = true)
    List<HistoriaCompletaFila> historiasDeFase(@Param("faseId") Long faseId);

    @Query(value = """
            SELECT c.historia_id AS historiaId, c.descripcion AS descripcion
            FROM historia_criterio c JOIN historia h ON h.id = c.historia_id
            WHERE h.fase_id = :faseId
            ORDER BY c.historia_id, c.orden, c.id
            """, nativeQuery = true)
    List<CriterioFila> criteriosDeFase(@Param("faseId") Long faseId);

    interface FaseFila {
        String getProyectoNombre();
        Integer getNumero();
        String getNombre();
    }

    interface HistoriaCompletaFila {
        Long getId();
        Integer getNumero();
        String getEstado();
        String getTitulo();
        String getComo();
        String getQuiero();
        String getPara();
        String getPrioridad();
        Integer getPuntos();
        String getFigmaUrl();
    }

    interface CriterioFila {
        Long getHistoriaId();
        String getDescripcion();
    }

    interface HistoriaFila {
        Long getId();
        Integer getNumero();
        String getTitulo();
        String getEstado();
        String getPrioridad();
        Integer getPuntos();
        Long getFaseId();
        Integer getFaseNumero();
        String getFaseNombre();
        String getAnalistaNombre();
        Long getTicketsTerminados();
        Long getTicketsTotal();
        LocalDateTime getActualizadoEn();
    }
}
