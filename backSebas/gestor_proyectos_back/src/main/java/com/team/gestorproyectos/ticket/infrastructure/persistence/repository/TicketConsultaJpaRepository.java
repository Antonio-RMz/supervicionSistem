package com.team.gestorproyectos.ticket.infrastructure.persistence.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import com.team.gestorproyectos.proyecto.infrastructure.persistence.entity.TicketEntity;

/** Consultas de solo lectura de Mis tickets y del Tablero. */
public interface TicketConsultaJpaRepository extends Repository<TicketEntity, Long> {

    @Query(value = """
            SELECT COUNT(*) > 0
            FROM proyecto p
            WHERE p.id = :proyectoId
              AND (:esAdmin = TRUE
                   OR p.lider_id = :usuarioId
                   OR EXISTS (SELECT 1 FROM proyecto_miembro m
                              WHERE m.proyecto_id = p.id AND m.usuario_id = :usuarioId))
            """, nativeQuery = true)
    Long proyectoVisible(@Param("proyectoId") Long proyectoId,
                         @Param("usuarioId") Long usuarioId,
                         @Param("esAdmin") boolean esAdmin);

    @Query(value = """
            SELECT t.id AS id, t.tipo AS tipo, t.numero AS numero, t.titulo AS titulo, t.estado AS estado,
                   t.prioridad AS prioridad, t.responsable_id AS responsableId, t.tester_id AS testerId,
                   t.creado_por AS creadoPorId, h.estado AS historiaEstado,
                   t.creado_en AS creadoEn, t.actualizado_en AS actualizadoEn,
                   p.lider_id AS liderId,
                   (SELECT he.estado_anterior FROM historial_estado he
                    WHERE he.entidad = 'TICKET' AND he.entidad_id = t.id
                    ORDER BY he.creado_en DESC, he.id DESC
                    LIMIT 1) AS ultimoEstadoAnterior,
                   f.id AS faseId, f.numero AS faseNumero, h.id AS historiaId, h.numero AS historiaNumero,
                   UPPER(CONCAT(LEFT(r.nombre, 1), LEFT(COALESCE(r.apellidos, ''), 1))) AS responsableIniciales
            FROM ticket t
            JOIN proyecto p      ON p.id = t.proyecto_id
            JOIN fase f          ON f.id = t.fase_id
            LEFT JOIN historia h ON h.id = t.historia_id
            LEFT JOIN usuario r  ON r.id = t.responsable_id
            WHERE t.proyecto_id = :proyectoId
              AND t.estado NOT IN ('CERRADA', 'CERRADO', 'CANCELADA', 'NO_REPRODUCIBLE', 'RECHAZADA', 'PUBLICADA', 'REVISADA')
            UNION ALL
            SELECT h.id, 'HIST', h.numero, h.titulo, h.estado, h.prioridad, h.analista_id, NULL,
                   h.creado_por, NULL, h.creado_en, h.actualizado_en, p.lider_id, NULL,
                   f.id, f.numero, NULL, NULL,
                   UPPER(CONCAT(LEFT(r.nombre, 1), LEFT(COALESCE(r.apellidos, ''), 1)))
            FROM historia h
            JOIN proyecto p     ON p.id = h.proyecto_id
            JOIN fase f         ON f.id = h.fase_id
            LEFT JOIN usuario r ON r.id = h.analista_id
            WHERE h.proyecto_id = :proyectoId
              AND h.estado NOT IN ('CERRADA', 'DESCARTADA')
            """, nativeQuery = true)
    List<FilaTicketFila> abiertos(@Param("proyectoId") Long proyectoId);

    /** Mismas columnas que abiertos(), pero solo tickets de una fase y también los cerrados. */
    @Query(value = """
            SELECT t.id AS id, t.tipo AS tipo, t.numero AS numero, t.titulo AS titulo, t.estado AS estado,
                   t.prioridad AS prioridad, t.responsable_id AS responsableId, t.tester_id AS testerId,
                   t.creado_por AS creadoPorId, h.estado AS historiaEstado,
                   t.creado_en AS creadoEn, t.actualizado_en AS actualizadoEn,
                   p.lider_id AS liderId,
                   (SELECT he.estado_anterior FROM historial_estado he
                    WHERE he.entidad = 'TICKET' AND he.entidad_id = t.id
                    ORDER BY he.creado_en DESC, he.id DESC
                    LIMIT 1) AS ultimoEstadoAnterior,
                   t.fase_id AS faseId, f.numero AS faseNumero, h.id AS historiaId, h.numero AS historiaNumero,
                   UPPER(CONCAT(LEFT(r.nombre, 1), LEFT(COALESCE(r.apellidos, ''), 1))) AS responsableIniciales
            FROM ticket t
            JOIN proyecto p      ON p.id = t.proyecto_id
            JOIN fase f          ON f.id = t.fase_id
            LEFT JOIN historia h ON h.id = t.historia_id
            LEFT JOIN usuario r  ON r.id = t.responsable_id
            WHERE t.proyecto_id = :proyectoId AND t.fase_id = :faseId
              AND t.estado NOT IN ('CANCELADA', 'NO_REPRODUCIBLE', 'RECHAZADA')
            """, nativeQuery = true)
    List<FilaTicketFila> deLaFase(@Param("proyectoId") Long proyectoId, @Param("faseId") Long faseId);

    @Query(value = """
            SELECT f.id AS id, f.numero AS numero, f.nombre AS nombre, f.estado AS estado,
                   f.es_mantenimiento AS mantenimiento
            FROM fase f
            WHERE f.proyecto_id = :proyectoId AND f.estado <> 'CANCELADA'
            ORDER BY f.es_mantenimiento, f.numero
            """, nativeQuery = true)
    List<FaseFila> fases(@Param("proyectoId") Long proyectoId);

    @Query(value = "SELECT u.id AS id, u.nombre AS nombre FROM usuario u WHERE u.tipo = 'EQUIPO'", nativeQuery = true)
    List<PersonaFila> personasDelEquipo();

    interface FilaTicketFila {
        Long getId();
        String getTipo();
        Integer getNumero();
        String getTitulo();
        String getEstado();
        String getPrioridad();
        Long getResponsableId();
        Long getTesterId();
        Long getCreadoPorId();
        String getHistoriaEstado();
        LocalDateTime getCreadoEn();
        LocalDateTime getActualizadoEn();
        Long getLiderId();
        String getUltimoEstadoAnterior();
        Long getFaseId();
        Integer getFaseNumero();
        Long getHistoriaId();
        Integer getHistoriaNumero();
        String getResponsableIniciales();
    }

    interface FaseFila {
        Long getId();
        Integer getNumero();
        String getNombre();
        String getEstado();
        Boolean getMantenimiento();
    }

    interface PersonaFila {
        Long getId();
        String getNombre();
    }
}
