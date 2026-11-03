package com.team.gestorproyectos.dashboard.infrastructure.persistence.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import com.team.gestorproyectos.proyecto.infrastructure.persistence.entity.ProyectoEntity;

/** Consultas de solo lectura que arma el Dashboard. Cada una devuelve una "fila" (proyección). */
public interface DashboardJpaRepository extends Repository<ProyectoEntity, Long> {

    @Query(value = """
            SELECT p.id AS id, p.nombre AS nombre, p.lider_id AS liderId
            FROM proyecto p
            WHERE p.id = :proyectoId
              AND (:esAdmin = TRUE
                   OR p.lider_id = :usuarioId
                   OR EXISTS (SELECT 1 FROM proyecto_miembro m
                              WHERE m.proyecto_id = p.id AND m.usuario_id = :usuarioId))
            """, nativeQuery = true)
    Optional<ProyectoFila> buscarVisible(@Param("proyectoId") Long proyectoId,
                                         @Param("usuarioId") Long usuarioId,
                                         @Param("esAdmin") boolean esAdmin);

    @Query(value = """
            SELECT t.id AS id, t.tipo AS tipo, t.numero AS numero, t.titulo AS titulo, t.estado AS estado,
                   t.prioridad AS prioridad, t.responsable_id AS responsableId, t.tester_id AS testerId,
                   t.creado_por AS creadoPorId, h.estado AS historiaEstado,
                   t.creado_en AS creadoEn, t.actualizado_en AS actualizadoEn, IF(p.requiere_pr, 'true', 'false') AS requierePr
            FROM ticket t
            JOIN proyecto p      ON p.id = t.proyecto_id
            LEFT JOIN historia h ON h.id = t.historia_id
            WHERE t.proyecto_id = :proyectoId
              AND t.estado NOT IN ('CERRADA', 'CERRADO', 'CANCELADA', 'NO_REPRODUCIBLE', 'RECHAZADA', 'PUBLICADA', 'REVISADA')
            UNION ALL
            SELECT h.id, 'HIST', h.numero, h.titulo, h.estado, h.prioridad, h.analista_id, NULL,
                   h.creado_por, NULL, h.creado_en, h.actualizado_en, IF(p.requiere_pr, 'true', 'false')
            FROM historia h
            JOIN proyecto p ON p.id = h.proyecto_id
            WHERE h.proyecto_id = :proyectoId
              AND h.estado NOT IN ('CERRADA', 'DESCARTADA')
            """, nativeQuery = true)
    List<ItemFila> itemsAbiertos(@Param("proyectoId") Long proyectoId);

    @Query(value = """
            SELECT u.id AS id, u.nombre AS nombre
            FROM usuario u
            WHERE u.id IN (SELECT m.usuario_id FROM proyecto_miembro m WHERE m.proyecto_id = :proyectoId)
               OR u.id = (SELECT p.lider_id FROM proyecto p WHERE p.id = :proyectoId)
            """, nativeQuery = true)
    List<PersonaFila> personas(@Param("proyectoId") Long proyectoId);

    @Query(value = """
            SELECT COALESCE(SUM(h.entidad = 'HISTORIA'), 0) AS historias,
                   COALESCE(SUM(h.entidad = 'TICKET'), 0) AS tickets
            FROM historial_estado h
            LEFT JOIN ticket t    ON h.entidad = 'TICKET'   AND t.id = h.entidad_id
            LEFT JOIN historia hi ON h.entidad = 'HISTORIA' AND hi.id = h.entidad_id
            WHERE h.creado_en >= :desde
              AND h.estado_nuevo IN ('CERRADA', 'CERRADO', 'PUBLICADA', 'REVISADA')
              AND COALESCE(t.proyecto_id, hi.proyecto_id) = :proyectoId
            """, nativeQuery = true)
    CierresFila cierresDesde(@Param("proyectoId") Long proyectoId, @Param("desde") LocalDateTime desde);

    @Query(value = """
            SELECT u.nombre AS usuario, h.entidad AS entidad,
                   CASE h.entidad WHEN 'TICKET' THEN t.tipo WHEN 'HISTORIA' THEN 'HIST' ELSE 'FASE' END AS tipo,
                   COALESCE(t.numero, hi.numero, f.numero) AS numero,
                   COALESCE(t.titulo, hi.titulo, f.nombre) AS titulo,
                   h.estado_anterior AS estadoAnterior, h.estado_nuevo AS estadoNuevo,
                   h.automatico AS automatico, h.creado_en AS fecha
            FROM historial_estado h
            LEFT JOIN ticket t    ON h.entidad = 'TICKET'   AND t.id = h.entidad_id
            LEFT JOIN historia hi ON h.entidad = 'HISTORIA' AND hi.id = h.entidad_id
            LEFT JOIN fase f      ON h.entidad = 'FASE'     AND f.id = h.entidad_id
            LEFT JOIN usuario u   ON u.id = h.usuario_id
            WHERE COALESCE(t.proyecto_id, hi.proyecto_id, f.proyecto_id) = :proyectoId
            ORDER BY h.creado_en DESC, h.id DESC
            LIMIT :limite
            """, nativeQuery = true)
    List<MovimientoFila> ultimosMovimientos(@Param("proyectoId") Long proyectoId, @Param("limite") int limite);

    interface ProyectoFila {
        Long getId();
        String getNombre();
        Long getLiderId();
    }

    interface ItemFila {
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
        // Con UNION, MySQL lo devuelve como TINYINT (Integer) y Spring no lo pasa a Boolean: va como texto
        Boolean getRequierePr();
    }

    interface PersonaFila {
        Long getId();
        String getNombre();
    }

    interface CierresFila {
        Long getHistorias();
        Long getTickets();
    }

    interface MovimientoFila {
        String getUsuario();
        String getEntidad();
        String getTipo();
        Integer getNumero();
        String getTitulo();
        String getEstadoAnterior();
        String getEstadoNuevo();
        Boolean getAutomatico();
        LocalDateTime getFecha();
    }
}
