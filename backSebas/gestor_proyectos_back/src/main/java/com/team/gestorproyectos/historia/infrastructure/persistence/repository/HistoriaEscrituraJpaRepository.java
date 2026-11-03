package com.team.gestorproyectos.historia.infrastructure.persistence.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import com.team.gestorproyectos.proyecto.infrastructure.persistence.entity.HistoriaEntity;

/** Lecturas para preparar una historia nueva y las tablas que la acompañan (criterios y módulos). */
public interface HistoriaEscrituraJpaRepository extends Repository<HistoriaEntity, Long> {

    @Query(value = """
            SELECT p.nombre FROM proyecto p
            WHERE p.id = :proyectoId
              AND (:esAdmin = TRUE OR p.lider_id = :usuarioId
                   OR EXISTS (SELECT 1 FROM proyecto_miembro m WHERE m.proyecto_id = p.id AND m.usuario_id = :usuarioId))
            """, nativeQuery = true)
    Optional<String> proyectoVisible(@Param("proyectoId") Long proyectoId, @Param("usuarioId") Long usuarioId,
                                     @Param("esAdmin") boolean esAdmin);

    @Query(value = "SELECT COALESCE(MAX(h.numero), 0) + 1 FROM historia h WHERE h.proyecto_id = :proyectoId",
            nativeQuery = true)
    int siguienteNumero(@Param("proyectoId") Long proyectoId);

    @Query(value = """
            SELECT f.id AS id, f.numero AS numero, f.nombre AS nombre, f.estado AS estado,
                   f.es_mantenimiento AS mantenimiento
            FROM fase f WHERE f.proyecto_id = :proyectoId ORDER BY f.numero
            """, nativeQuery = true)
    List<FaseFila> fases(@Param("proyectoId") Long proyectoId);

    @Query(value = """
            SELECT fm.fase_id AS faseId, m.id AS id, m.tipo AS tipo, m.nombre AS nombre
            FROM fase_modulo fm JOIN modulo m ON m.id = fm.modulo_id
            JOIN fase f ON f.id = fm.fase_id
            WHERE f.proyecto_id = :proyectoId
            ORDER BY FIELD(m.tipo, 'API', 'WEB', 'MOVIL', 'ESCRITORIO', 'BASE_DATOS', 'INFRAESTRUCTURA'), m.id
            """, nativeQuery = true)
    List<OpcionFila> modulos(@Param("proyectoId") Long proyectoId);

    /** Miembros con cuenta activa y el Líder. roles = "ANALISTA,DESARROLLADOR". */
    @Query(value = """
            SELECT u.id AS id, u.nombre AS nombre, u.apellidos AS apellidos, GROUP_CONCAT(r.codigo) AS roles
            FROM usuario u
            JOIN proyecto p          ON p.id = :proyectoId
            LEFT JOIN usuario_rol ur ON ur.usuario_id = u.id
            LEFT JOIN rol r          ON r.id = ur.rol_id
            WHERE u.activo = TRUE
              AND (u.id = p.lider_id
                   OR u.id IN (SELECT m.usuario_id FROM proyecto_miembro m WHERE m.proyecto_id = :proyectoId))
            GROUP BY u.id, u.nombre, u.apellidos
            ORDER BY u.nombre
            """, nativeQuery = true)
    List<PersonaFila> equipo(@Param("proyectoId") Long proyectoId);

    @Query(value = """
            SELECT u.nombre AS nombre, u.descripcion AS descripcion
            FROM proyecto_usuario_sistema u
            WHERE u.proyecto_id = :proyectoId
            ORDER BY u.orden, u.id
            """, nativeQuery = true)
    List<UsuarioSistemaFila> usuariosDelSistema(@Param("proyectoId") Long proyectoId);

    interface UsuarioSistemaFila {
        String getNombre();
        String getDescripcion();
    }

    @Modifying
    @Query(value = "INSERT INTO historia_criterio (historia_id, descripcion, orden) VALUES (:historiaId, :texto, :orden)",
            nativeQuery = true)
    void agregarCriterio(@Param("historiaId") Long historiaId, @Param("texto") String texto, @Param("orden") int orden);

    @Modifying
    @Query(value = "INSERT INTO historia_modulo (historia_id, modulo_id) VALUES (:historiaId, :moduloId)",
            nativeQuery = true)
    void agregarModulo(@Param("historiaId") Long historiaId, @Param("moduloId") Long moduloId);

    @Query(value = "SELECT c.descripcion FROM historia_criterio c WHERE c.historia_id = :historiaId ORDER BY c.orden, c.id",
            nativeQuery = true)
    List<String> criterios(@Param("historiaId") Long historiaId);

    @Query(value = "SELECT hm.modulo_id FROM historia_modulo hm WHERE hm.historia_id = :historiaId", nativeQuery = true)
    List<Long> modulosDeHistoria(@Param("historiaId") Long historiaId);

    @Modifying
    @Query(value = "DELETE FROM historia_criterio WHERE historia_id = :historiaId", nativeQuery = true)
    void quitarCriterios(@Param("historiaId") Long historiaId);

    @Modifying
    @Query(value = "DELETE FROM historia_modulo WHERE historia_id = :historiaId", nativeQuery = true)
    void quitarModulos(@Param("historiaId") Long historiaId);

    // ─── Desarrolladores de la historia ─────────────────────────────────────

    @Modifying
    @Query(value = """
            INSERT INTO historia_desarrollador (historia_id, usuario_id, modulo_id, terminado)
            VALUES (:historiaId, :usuarioId, :moduloId, FALSE)
            """, nativeQuery = true)
    void agregarDesarrollador(@Param("historiaId") Long historiaId, @Param("usuarioId") Long usuarioId,
                              @Param("moduloId") Long moduloId);

    @Modifying
    @Query(value = "DELETE FROM historia_desarrollador WHERE historia_id = :historiaId", nativeQuery = true)
    void quitarDesarrolladores(@Param("historiaId") Long historiaId);

    @Query(value = """
            SELECT d.usuario_id AS usuarioId, d.modulo_id AS moduloId
            FROM historia_desarrollador d WHERE d.historia_id = :historiaId ORDER BY d.usuario_id
            """, nativeQuery = true)
    List<DesarrolladorFila> desarrolladores(@Param("historiaId") Long historiaId);

    interface DesarrolladorFila {
        Long getUsuarioId();
        Long getModuloId();
    }

    @Modifying
    @Query(value = """
            INSERT INTO historial_estado (entidad, entidad_id, estado_anterior, estado_nuevo, automatico, usuario_id, creado_en)
            VALUES ('HISTORIA', :historiaId, NULL, 'BORRADOR', FALSE, :usuarioId, :fecha)
            """, nativeQuery = true)
    void registrarCreacion(@Param("historiaId") Long historiaId, @Param("usuarioId") Long usuarioId,
                           @Param("fecha") LocalDateTime fecha);

    interface FaseFila {
        Long getId();
        Integer getNumero();
        String getNombre();
        String getEstado();
        Boolean getMantenimiento();
    }

    interface OpcionFila {
        Long getFaseId();
        Long getId();
        String getTipo();
        String getNombre();
    }

    interface PersonaFila {
        Long getId();
        String getNombre();
        String getApellidos();
        String getRoles();
    }
}
