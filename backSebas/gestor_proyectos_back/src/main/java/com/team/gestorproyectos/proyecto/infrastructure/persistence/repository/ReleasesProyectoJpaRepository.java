package com.team.gestorproyectos.proyecto.infrastructure.persistence.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import com.team.gestorproyectos.proyecto.infrastructure.persistence.entity.VersionReleaseEntity;

/** La pestaña Releases del proyecto (PDF pág. 36). */
public interface ReleasesProyectoJpaRepository extends Repository<VersionReleaseEntity, Long> {

    @Query(value = """
            SELECT h.id AS id, h.numero AS numero, h.titulo AS titulo FROM historia h
            WHERE h.proyecto_id = :proyectoId AND h.estado = 'ACEPTADA'
            ORDER BY h.numero
            """, nativeQuery = true)
    List<HistoriaFila> historiasAceptadas(@Param("proyectoId") Long proyectoId);

    /** Primero la que está en curso (o a punto de publicarse); si no, la siguiente planeada. */
    @Query(value = """
            SELECT f.id FROM fase f
            WHERE f.proyecto_id = :proyectoId AND f.es_mantenimiento = FALSE
              AND f.estado IN ('EN_CURSO', 'PAUSADA', 'LISTA_PARA_RELEASE', 'EN_PUBLICACION', 'PLANEADA')
            ORDER BY f.estado = 'PLANEADA', f.numero
            LIMIT 1
            """, nativeQuery = true)
    Optional<Long> proximaFase(@Param("proyectoId") Long proyectoId);

    @Query(value = "SELECT f.id FROM fase f WHERE f.proyecto_id = :proyectoId AND f.es_mantenimiento = TRUE LIMIT 1",
            nativeQuery = true)
    Optional<Long> faseMantenimiento(@Param("proyectoId") Long proyectoId);

    @Query(value = """
            SELECT COALESCE(SUM(t.estado = 'APROBADO'), 0) AS aprobados,
                   COALESCE(SUM(t.estado IN ('CONFIRMADO', 'EN_CORRECCION', 'BLOQUEADA', 'EN_QA')), 0) AS enCamino
            FROM ticket t JOIN fase f ON f.id = t.fase_id
            WHERE t.proyecto_id = :proyectoId AND t.tipo = 'BUGS' AND f.es_mantenimiento = TRUE
              AND t.severidad IN ('CRITICA', 'ALTA')
            """, nativeQuery = true)
    HotfixFila hotfixAbiertos(@Param("proyectoId") Long proyectoId);

    /** De una fase cuenta sus historias (sin descartadas) y tickets (sin cancelados). */
    @Query(value = """
            SELECT r.id AS id, r.version AS version, r.tipo AS tipo, r.estado AS estado, f.id AS faseId,
                   f.numero AS faseNumero, f.nombre AS faseNombre, r.publicado_en AS fecha,
                   TRIM(CONCAT(u.nombre, ' ', COALESCE(u.apellidos, ''))) AS publicadoPor,
                   (SELECT COUNT(*) FROM historia h WHERE h.fase_id = f.id AND h.estado <> 'DESCARTADA') AS historias,
                   (SELECT COUNT(*) FROM ticket t WHERE t.fase_id = f.id
                      AND t.estado NOT IN ('CANCELADA', 'NO_REPRODUCIBLE', 'RECHAZADA')) AS tickets
            FROM version_release r
            LEFT JOIN fase f    ON f.id = r.fase_id
            LEFT JOIN usuario u ON u.id = r.publicado_por
            WHERE r.proyecto_id = :proyectoId AND r.estado IN ('PUBLICADA', 'REVERTIDA')
            """, nativeQuery = true)
    List<PublicadoFila> publicados(@Param("proyectoId") Long proyectoId);

    /** Los bugs que salieron en un parche quedan en su historial con "Publicado en el parche v1.2.1". */
    @Query(value = """
            SELECT DISTINCT t.id AS id, CONCAT(t.tipo, '-', LPAD(t.numero, 2, '0')) AS codigo, t.titulo AS titulo
            FROM historial_estado he JOIN ticket t ON t.id = he.entidad_id
            WHERE he.entidad = 'TICKET' AND t.proyecto_id = :proyectoId AND he.motivo = CONCAT('Publicado en el parche ', :version)
            ORDER BY codigo
            """, nativeQuery = true)
    List<BugFila> bugsDelParche(@Param("proyectoId") Long proyectoId, @Param("version") String version);

    interface HistoriaFila {
        Long getId();
        Integer getNumero();
        String getTitulo();
    }

    interface HotfixFila {
        Number getAprobados();
        Number getEnCamino();
    }

    interface PublicadoFila {
        Long getId();
        String getVersion();
        String getTipo();
        String getEstado();
        Long getFaseId();
        Integer getFaseNumero();
        String getFaseNombre();
        LocalDateTime getFecha();
        String getPublicadoPor();
        Long getHistorias();
        Long getTickets();
    }

    interface BugFila {
        Long getId();
        String getCodigo();
        String getTitulo();
    }
}
