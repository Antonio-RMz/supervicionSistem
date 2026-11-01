package com.team.gestorproyectos.ticket.infrastructure.persistence.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import com.team.gestorproyectos.proyecto.infrastructure.persistence.entity.TicketEntity;

/** La vista de Mantenimiento (PDF pág. 37). */
public interface TicketMantenimientoJpaRepository extends Repository<TicketEntity, Long> {

    @Query(value = "SELECT f.id FROM fase f WHERE f.proyecto_id = :proyectoId AND f.es_mantenimiento = TRUE LIMIT 1",
            nativeQuery = true)
    Optional<Long> faseMantenimiento(@Param("proyectoId") Long proyectoId);

    @Query(value = """
            SELECT t.id AS id, t.tipo AS tipo, t.numero AS numero, t.titulo AS titulo, t.estado AS estado,
                   t.severidad AS severidad, t.rama AS rama, r.id AS responsableId,
                   CONCAT(r.nombre, COALESCE(CONCAT(' ', SUBSTRING_INDEX(r.apellidos, ' ', 1)), '')) AS responsableNombre
            FROM ticket t
            LEFT JOIN usuario r ON r.id = t.responsable_id
            WHERE t.fase_id = :faseId
            """, nativeQuery = true)
    List<TicketFila> tickets(@Param("faseId") Long faseId);

    @Query(value = """
            SELECT f.numero FROM fase f
            WHERE f.proyecto_id = :proyectoId AND f.es_mantenimiento = FALSE
              AND f.estado IN ('EN_CURSO', 'PAUSADA', 'LISTA_PARA_RELEASE', 'EN_PUBLICACION', 'PLANEADA')
            ORDER BY f.estado = 'PLANEADA', f.numero
            LIMIT 1
            """, nativeQuery = true)
    Optional<Integer> proximaFaseNumero(@Param("proyectoId") Long proyectoId);

    /** De cuando se creó el bug a cuando pasó a Publicado, en los hotfix de Mantenimiento. */
    @Query(value = """
            SELECT TIMESTAMPDIFF(MINUTE, t.creado_en, he.creado_en)
            FROM historial_estado he
            JOIN ticket t ON t.id = he.entidad_id
            JOIN fase f   ON f.id = t.fase_id AND f.es_mantenimiento = TRUE
            WHERE he.entidad = 'TICKET' AND he.estado_nuevo = 'PUBLICADO' AND t.proyecto_id = :proyectoId
              AND t.tipo = 'BUGS' AND t.severidad IN ('CRITICA', 'ALTA') AND he.creado_en >= :desde
            """, nativeQuery = true)
    List<Long> minutosHotfix(@Param("proyectoId") Long proyectoId, @Param("desde") LocalDateTime desde);

    /** Los bugs de cada parche quedan en su historial con "Publicado en el parche v1.2.1". */
    @Query(value = """
            SELECT r.version AS version, r.publicado_en AS fecha,
                   (SELECT COUNT(DISTINCT he.entidad_id) FROM historial_estado he
                    WHERE he.entidad = 'TICKET' AND he.motivo = CONCAT('Publicado en el parche ', r.version)) AS bugs
            FROM version_release r
            WHERE r.proyecto_id = :proyectoId AND r.tipo = 'PARCHE' AND r.estado = 'PUBLICADA'
            ORDER BY r.publicado_en DESC, r.id DESC
            """, nativeQuery = true)
    List<ParcheFila> parches(@Param("proyectoId") Long proyectoId);

    @Query(value = """
            SELECT m.id AS id, m.tipo AS tipo, m.nombre AS nombre, m.tecnologia AS tecnologia,
                   m.repositorio AS repositorio, m.rama_base AS ramaBase
            FROM modulo m
            WHERE m.proyecto_id = :proyectoId
            ORDER BY FIELD(m.tipo, 'API', 'WEB', 'MOVIL', 'ESCRITORIO', 'BASE_DATOS', 'INFRAESTRUCTURA'), m.id
            """, nativeQuery = true)
    List<ModuloFila> modulos(@Param("proyectoId") Long proyectoId);

    @Query(value = """
            SELECT h.id AS id, h.numero AS numero, h.titulo AS titulo
            FROM historia h
            WHERE h.proyecto_id = :proyectoId AND h.estado NOT IN ('BORRADOR', 'EN_PLANEACION', 'DESCARTADA')
            ORDER BY h.numero DESC
            """, nativeQuery = true)
    List<HistoriaFila> historias(@Param("proyectoId") Long proyectoId);

    interface ModuloFila {
        Long getId();
        String getTipo();
        String getNombre();
        String getTecnologia();
        String getRepositorio();
        String getRamaBase();
    }

    interface HistoriaFila {
        Long getId();
        Integer getNumero();
        String getTitulo();
    }

    interface TicketFila {
        Long getId();
        String getTipo();
        Integer getNumero();
        String getTitulo();
        String getEstado();
        String getSeveridad();
        String getRama();
        Long getResponsableId();
        String getResponsableNombre();
    }

    interface ParcheFila {
        String getVersion();
        LocalDateTime getFecha();
        Long getBugs();
    }
}
