package com.team.gestorproyectos.fase.infrastructure.persistence.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import com.team.gestorproyectos.proyecto.infrastructure.persistence.entity.FaseEntity;

/** Lecturas para preparar una fase nueva y las tablas que la acompañan (módulos y criterios de cierre). */
public interface FaseEscrituraJpaRepository extends Repository<FaseEntity, Long> {

    @Query(value = """
            SELECT p.id AS id, p.nombre AS nombre, p.lider_id AS liderId
            FROM proyecto p
            WHERE p.id = :proyectoId
              AND (:esAdmin = TRUE OR p.lider_id = :usuarioId
                   OR EXISTS (SELECT 1 FROM proyecto_miembro m WHERE m.proyecto_id = p.id AND m.usuario_id = :usuarioId))
            """, nativeQuery = true)
    Optional<ProyectoFila> proyectoVisible(@Param("proyectoId") Long proyectoId, @Param("usuarioId") Long usuarioId,
                                           @Param("esAdmin") boolean esAdmin);

    @Query(value = """
            SELECT f.numero AS numero, f.nombre AS nombre, f.estado AS estado, f.fecha_fin AS fechaFin,
                   f.version_release AS versionRelease, f.es_mantenimiento AS mantenimiento
            FROM fase f
            WHERE f.proyecto_id = :proyectoId AND f.estado <> 'CANCELADA'
            ORDER BY f.numero
            """, nativeQuery = true)
    List<FaseFila> fases(@Param("proyectoId") Long proyectoId);

    @Query(value = """
            SELECT m.id AS id, m.tipo AS tipo, m.nombre AS nombre, m.tecnologia AS tecnologia
            FROM modulo m WHERE m.proyecto_id = :proyectoId ORDER BY m.id
            """, nativeQuery = true)
    List<ModuloFila> modulos(@Param("proyectoId") Long proyectoId);

    /** Miembros con cuenta activa y el Líder; el Líder primero. roles = "ADMIN,LIDER". */
    @Query(value = """
            SELECT u.id AS id, u.nombre AS nombre, u.apellidos AS apellidos, GROUP_CONCAT(r.codigo) AS roles
            FROM usuario u
            JOIN proyecto p          ON p.id = :proyectoId
            LEFT JOIN usuario_rol ur ON ur.usuario_id = u.id
            LEFT JOIN rol r          ON r.id = ur.rol_id
            WHERE u.activo = TRUE
              AND (u.id = p.lider_id
                   OR u.id IN (SELECT m.usuario_id FROM proyecto_miembro m WHERE m.proyecto_id = :proyectoId))
            GROUP BY u.id, u.nombre, u.apellidos, p.lider_id
            ORDER BY u.id <> p.lider_id, u.nombre
            """, nativeQuery = true)
    List<PersonaFila> equipo(@Param("proyectoId") Long proyectoId);

    @Modifying
    @Query(value = "UPDATE fase SET figma_url = :figmaUrl, figma_para_todas = :paraTodas WHERE id = :faseId",
            nativeQuery = true)
    void guardarDiseno(@Param("faseId") Long faseId, @Param("figmaUrl") String figmaUrl,
                       @Param("paraTodas") boolean paraTodas);

    @Modifying
    @Query(value = "INSERT INTO fase_modulo (fase_id, modulo_id) VALUES (:faseId, :moduloId)", nativeQuery = true)
    void agregarModulo(@Param("faseId") Long faseId, @Param("moduloId") Long moduloId);

    @Modifying
    @Query(value = "DELETE FROM fase_modulo WHERE fase_id = :faseId AND modulo_id = :moduloId", nativeQuery = true)
    void quitarModulo(@Param("faseId") Long faseId, @Param("moduloId") Long moduloId);

    @Query(value = """
            SELECT f.id AS id, f.numero AS numero, f.estado AS estado, f.nombre AS nombre, f.objetivo AS objetivo,
                   f.fecha_inicio AS fechaInicio, f.fecha_fin AS fechaFin, f.responsable_id AS responsableId,
                   f.version_release AS versionRelease, f.prioridad AS prioridad
            FROM fase f
            WHERE f.proyecto_id = :proyectoId AND f.id = :faseId
            """, nativeQuery = true)
    Optional<FaseGuardadaFila> faseGuardada(@Param("proyectoId") Long proyectoId, @Param("faseId") Long faseId);

    @Query(value = "SELECT fm.modulo_id FROM fase_modulo fm WHERE fm.fase_id = :faseId", nativeQuery = true)
    List<Long> modulosDeFase(@Param("faseId") Long faseId);

    @Query(value = """
            SELECT c.descripcion FROM fase_criterio_cierre c
            WHERE c.fase_id = :faseId AND c.tipo = 'MANUAL'
            ORDER BY c.id
            """, nativeQuery = true)
    List<String> criteriosManuales(@Param("faseId") Long faseId);

    /** Los módulos que ya usan los tickets o las historias (no descartadas) de la fase. */
    @Query(value = """
            SELECT t.modulo_id FROM ticket t WHERE t.fase_id = :faseId AND t.modulo_id IS NOT NULL
            UNION
            SELECT hm.modulo_id FROM historia_modulo hm
            JOIN historia h ON h.id = hm.historia_id
            WHERE h.fase_id = :faseId AND h.estado <> 'DESCARTADA'
            """, nativeQuery = true)
    List<Long> modulosEnUso(@Param("faseId") Long faseId);

    @Modifying
    @Query(value = """
            DELETE FROM fase_criterio_cierre
            WHERE fase_id = :faseId AND tipo = 'MANUAL' AND descripcion = :texto
            """, nativeQuery = true)
    void quitarCriterioManual(@Param("faseId") Long faseId, @Param("texto") String texto);

    /** codigo: el del criterio automático (HISTORIAS_ACEPTADAS...); null en los manuales. */
    @Modifying
    @Query(value = """
            INSERT INTO fase_criterio_cierre (fase_id, descripcion, tipo, codigo)
            VALUES (:faseId, :texto, :tipo, :codigo)
            """, nativeQuery = true)
    void agregarCriterio(@Param("faseId") Long faseId, @Param("texto") String texto, @Param("tipo") String tipo,
                         @Param("codigo") String codigo);

    @Modifying
    @Query(value = """
            INSERT INTO historial_estado (entidad, entidad_id, estado_anterior, estado_nuevo, automatico, usuario_id, creado_en)
            VALUES ('FASE', :faseId, NULL, :estado, FALSE, :usuarioId, :fecha)
            """, nativeQuery = true)
    void registrarCreacion(@Param("faseId") Long faseId, @Param("estado") String estado,
                           @Param("usuarioId") Long usuarioId, @Param("fecha") LocalDateTime fecha);

    interface ProyectoFila {
        Long getId();
        String getNombre();
        Long getLiderId();
    }

    interface FaseFila {
        Integer getNumero();
        String getNombre();
        String getEstado();
        LocalDate getFechaFin();
        String getVersionRelease();
        Boolean getMantenimiento();
    }

    interface FaseGuardadaFila {
        Long getId();
        Integer getNumero();
        String getEstado();
        String getNombre();
        String getObjetivo();
        LocalDate getFechaInicio();
        LocalDate getFechaFin();
        Long getResponsableId();
        String getVersionRelease();
        String getPrioridad();
    }

    interface ModuloFila {
        Long getId();
        String getTipo();
        String getNombre();
        String getTecnologia();
    }

    interface PersonaFila {
        Long getId();
        String getNombre();
        String getApellidos();
        String getRoles();
    }
}
