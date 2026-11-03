package com.team.gestorproyectos.usuario.infrastructure.persistence.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import com.team.gestorproyectos.usuario.infrastructure.persistence.entity.UsuarioEntity;

/** Consultas de solo lectura del detalle de una persona, a través de todos sus proyectos. */
public interface PerfilUsuarioJpaRepository extends Repository<UsuarioEntity, Long> {

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
                   IF(p.requiere_pr, 'true', 'false') AS requierePr
            FROM ticket t
            JOIN proyecto p     ON p.id = t.proyecto_id
            LEFT JOIN historia h ON h.id = t.historia_id
            WHERE t.estado NOT IN ('CERRADA', 'CERRADO', 'CANCELADA', 'NO_REPRODUCIBLE', 'RECHAZADA', 'PUBLICADA', 'REVISADA')
              AND (t.responsable_id = :usuarioId OR t.tester_id = :usuarioId
                   OR t.creado_por = :usuarioId OR p.lider_id = :usuarioId)
            UNION ALL
            SELECT h.id, 'HIST', h.numero, h.titulo, h.estado, h.prioridad, h.analista_id, NULL,
                   h.creado_por, NULL, h.creado_en, h.actualizado_en, p.lider_id, NULL, IF(p.requiere_pr, 'true', 'false')
            FROM historia h
            JOIN proyecto p ON p.id = h.proyecto_id
            WHERE h.estado NOT IN ('CERRADA', 'DESCARTADA')
              AND (h.analista_id = :usuarioId OR h.creado_por = :usuarioId OR p.lider_id = :usuarioId)
            """, nativeQuery = true)
    List<ItemFila> itemsAbiertos(@Param("usuarioId") Long usuarioId);

    @Query(value = "SELECT u.id AS id, u.nombre AS nombre FROM usuario u WHERE u.tipo = 'EQUIPO'", nativeQuery = true)
    List<PersonaFila> personasDelEquipo();

    @Query(value = """
            SELECT COUNT(DISTINCT t.id)
            FROM historial_estado he
            JOIN ticket t ON he.entidad = 'TICKET' AND t.id = he.entidad_id
            WHERE t.responsable_id = :usuarioId
              AND he.creado_en >= :desde
              AND he.estado_nuevo IN ('CERRADA', 'CERRADO', 'PUBLICADA', 'REVISADA')
            """, nativeQuery = true)
    long ticketsCerradosDesde(@Param("usuarioId") Long usuarioId, @Param("desde") LocalDateTime desde);

    @Query(value = """
            SELECT COUNT(*)
            FROM historial_estado he
            JOIN ticket t ON he.entidad = 'TICKET' AND t.id = he.entidad_id
            WHERE t.responsable_id = :usuarioId
              AND he.creado_en >= :desde
              AND he.estado_anterior = 'EN_QA'
              AND he.estado_nuevo IN ('EN_PROGRESO', 'EN_CORRECCION')
            """, nativeQuery = true)
    long rechazosEnQaDesde(@Param("usuarioId") Long usuarioId, @Param("desde") LocalDateTime desde);

    @Query(value = """
            SELECT u.nombre AS usuario, h.entidad AS entidad,
                   CASE h.entidad WHEN 'TICKET' THEN t.tipo WHEN 'HISTORIA' THEN 'HIST' ELSE 'FASE' END AS tipo,
                   COALESCE(t.numero, hi.numero, f.numero) AS numero,
                   COALESCE(t.titulo, hi.titulo, f.nombre) AS titulo,
                   h.estado_anterior AS estadoAnterior, h.estado_nuevo AS estadoNuevo,
                   h.automatico AS automatico, h.creado_en AS fecha
            FROM historial_estado h
            JOIN usuario u        ON u.id = h.usuario_id
            LEFT JOIN ticket t    ON h.entidad = 'TICKET'   AND t.id = h.entidad_id
            LEFT JOIN historia hi ON h.entidad = 'HISTORIA' AND hi.id = h.entidad_id
            LEFT JOIN fase f      ON h.entidad = 'FASE'     AND f.id = h.entidad_id
            WHERE h.usuario_id = :usuarioId AND h.automatico = FALSE
            ORDER BY h.creado_en DESC, h.id DESC
            LIMIT :limite
            """, nativeQuery = true)
    List<MovimientoFila> movimientosDe(@Param("usuarioId") Long usuarioId, @Param("limite") int limite);

    @Query(value = """
            SELECT c.entidad AS entidad,
                   CASE c.entidad WHEN 'TICKET' THEN t.tipo WHEN 'HISTORIA' THEN 'HIST' ELSE 'FASE' END AS tipo,
                   COALESCE(t.numero, hi.numero, f.numero) AS numero,
                   COALESCE(t.titulo, hi.titulo, f.nombre) AS titulo,
                   c.creado_en AS fecha
            FROM comentario c
            LEFT JOIN ticket t    ON c.entidad = 'TICKET'   AND t.id = c.entidad_id
            LEFT JOIN historia hi ON c.entidad = 'HISTORIA' AND hi.id = c.entidad_id
            LEFT JOIN fase f      ON c.entidad = 'FASE'     AND f.id = c.entidad_id
            WHERE c.usuario_id = :usuarioId
            ORDER BY c.creado_en DESC, c.id DESC
            LIMIT :limite
            """, nativeQuery = true)
    List<ComentarioFila> comentariosDe(@Param("usuarioId") Long usuarioId, @Param("limite") int limite);

    @Query(value = """
            SELECT p.id AS id, p.nombre AS nombre,
                   (SELECT COUNT(*) FROM ticket t
                    WHERE t.proyecto_id = p.id AND t.responsable_id = :usuarioId
                      AND t.estado NOT IN ('CERRADA', 'CERRADO', 'CANCELADA', 'NO_REPRODUCIBLE', 'RECHAZADA', 'PUBLICADA', 'REVISADA')
                   ) AS ticketsAbiertos
            FROM proyecto p
            WHERE p.lider_id = :usuarioId
               OR EXISTS (SELECT 1 FROM proyecto_miembro m WHERE m.proyecto_id = p.id AND m.usuario_id = :usuarioId)
            ORDER BY ticketsAbiertos DESC, p.nombre
            """, nativeQuery = true)
    List<ProyectoFila> proyectosDe(@Param("usuarioId") Long usuarioId);

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
        Long getLiderId();
        String getUltimoEstadoAnterior();
        // Con UNION, MySQL lo devuelve como TINYINT (Integer) y Spring no lo pasa a Boolean: va como texto
        Boolean getRequierePr();
    }

    interface PersonaFila {
        Long getId();
        String getNombre();
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

    interface ComentarioFila {
        String getEntidad();
        String getTipo();
        Integer getNumero();
        String getTitulo();
        LocalDateTime getFecha();
    }

    interface ProyectoFila {
        Long getId();
        String getNombre();
        Long getTicketsAbiertos();
    }
}
