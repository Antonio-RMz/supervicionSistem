package com.team.gestorproyectos.proyecto.infrastructure.persistence.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import com.team.gestorproyectos.proyecto.infrastructure.persistence.entity.ModuloEntity;

/** Lo que se ve en la tarjeta de cada módulo (PDF pág. 35). */
public interface ModulosProyectoJpaRepository extends Repository<ModuloEntity, Long> {

    /** Cada módulo con su responsable técnico, el CI de su PR más reciente y si ya tiene trabajo. */
    @Query(value = """
            SELECT m.id AS id, m.tipo AS tipo, m.nombre AS nombre, m.tecnologia AS tecnologia,
                   m.repositorio AS repositorio, m.rama_base AS ramaBase, r.id AS responsableId,
                   CONCAT(r.nombre, COALESCE(CONCAT(' ', SUBSTRING_INDEX(r.apellidos, ' ', 1)), '')) AS responsableNombre,
                   (SELECT pr.ci_estado FROM pull_request pr WHERE pr.modulo_id = m.id
                    ORDER BY pr.actualizado_en DESC, pr.id DESC LIMIT 1) AS ciEstado,
                   (SELECT pr.actualizado_en FROM pull_request pr WHERE pr.modulo_id = m.id
                    ORDER BY pr.actualizado_en DESC, pr.id DESC LIMIT 1) AS ciFecha,
                   (EXISTS (SELECT 1 FROM ticket t WHERE t.modulo_id = m.id)
                    OR EXISTS (SELECT 1 FROM fase_modulo fm WHERE fm.modulo_id = m.id)
                    OR EXISTS (SELECT 1 FROM pull_request pr WHERE pr.modulo_id = m.id)) AS conTrabajo
            FROM modulo m
            LEFT JOIN usuario r ON r.id = m.responsable_id
            WHERE m.proyecto_id = :proyectoId
            ORDER BY m.id
            """, nativeQuery = true)
    List<ModuloFila> modulos(@Param("proyectoId") Long proyectoId);

    /** Tickets por módulo y estado; de la fase de Mantenimiento solo los bugs. */
    @Query(value = """
            SELECT t.modulo_id AS moduloId, f.es_mantenimiento AS mantenimiento, t.estado AS estado, COUNT(*) AS cantidad
            FROM ticket t
            JOIN fase f ON f.id = t.fase_id
            WHERE t.proyecto_id = :proyectoId AND t.modulo_id IS NOT NULL
              AND (f.es_mantenimiento = FALSE OR t.tipo = 'BUGS')
            GROUP BY t.modulo_id, f.es_mantenimiento, t.estado
            """, nativeQuery = true)
    List<ConteoFila> conteos(@Param("proyectoId") Long proyectoId);

    /** La fase que se trabaja ahora (sin Mantenimiento). */
    @Query(value = """
            SELECT f.id AS id, f.version_release AS version FROM fase f
            WHERE f.proyecto_id = :proyectoId AND f.es_mantenimiento = FALSE
              AND f.estado IN ('EN_CURSO', 'PAUSADA', 'LISTA_PARA_RELEASE', 'EN_PUBLICACION')
            ORDER BY f.numero
            LIMIT 1
            """, nativeQuery = true)
    Optional<FaseFila> faseEnCurso(@Param("proyectoId") Long proyectoId);

    /** Por módulo: cuántas historias de la fase ya metieron código suyo a develop, y la última vez. */
    @Query(value = """
            SELECT t.modulo_id AS moduloId, COUNT(DISTINCT h.id) AS integradas, MAX(he.creado_en) AS ultima
            FROM historia h
            JOIN ticket t ON t.historia_id = h.id
            LEFT JOIN historial_estado he ON he.entidad = 'HISTORIA' AND he.entidad_id = h.id AND he.estado_nuevo = 'INTEGRADA'
            WHERE h.fase_id = :faseId AND h.estado IN ('INTEGRADA', 'CERRADA') AND t.modulo_id IS NOT NULL
            GROUP BY t.modulo_id
            """, nativeQuery = true)
    List<IntegracionFila> integraciones(@Param("faseId") Long faseId);

    @Query(value = """
            SELECT r.version AS version, r.publicado_en AS fecha FROM version_release r
            WHERE r.proyecto_id = :proyectoId AND r.estado = 'PUBLICADA'
            ORDER BY r.publicado_en DESC, r.id DESC
            LIMIT 1
            """, nativeQuery = true)
    Optional<PublicadaFila> produccion(@Param("proyectoId") Long proyectoId);

    interface ModuloFila {
        Long getId();
        String getTipo();
        String getNombre();
        String getTecnologia();
        String getRepositorio();
        String getRamaBase();
        Long getResponsableId();
        String getResponsableNombre();
        String getCiEstado();
        LocalDateTime getCiFecha();
        Long getConTrabajo();
    }

    interface ConteoFila {
        Long getModuloId();
        Boolean getMantenimiento();
        String getEstado();
        Long getCantidad();
    }

    interface FaseFila {
        Long getId();
        String getVersion();
    }

    interface IntegracionFila {
        Long getModuloId();
        Long getIntegradas();
        LocalDateTime getUltima();
    }

    interface PublicadaFila {
        String getVersion();
        LocalDateTime getFecha();
    }
}
