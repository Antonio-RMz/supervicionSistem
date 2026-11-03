package com.team.gestorproyectos.proyecto.infrastructure.persistence.repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import com.team.gestorproyectos.proyecto.infrastructure.persistence.entity.ProyectoEntity;

/** El equipo de un proyecto (PDF pág. 34). */
public interface EquipoProyectoJpaRepository extends Repository<ProyectoEntity, Long> {

    @Query(value = """
            SELECT p.id AS id, p.lider_id AS liderId, p.permitir_autovalidacion AS permitirAutovalidacion,
                   p.comentario_al_rechazar AS comentarioAlRechazar, p.rama_al_iniciar AS ramaAlIniciar,
                   p.notificar_cliente_fase AS notificarClienteFase,
                   p.forma_trabajo AS formaTrabajo, p.requiere_pr AS requierePr
            FROM proyecto p WHERE p.id = :proyectoId
            """, nativeQuery = true)
    Optional<ProyectoFila> proyecto(@Param("proyectoId") Long proyectoId);

    /** Miembros y Líder; roles = "ADMIN,LIDER". */
    @Query(value = """
            SELECT u.id AS id, u.nombre AS nombre, u.apellidos AS apellidos, u.ultimo_acceso AS ultimoAcceso,
                   GROUP_CONCAT(r.codigo ORDER BY r.id) AS roles
            FROM (SELECT m.usuario_id FROM proyecto_miembro m WHERE m.proyecto_id = :proyectoId
                  UNION
                  SELECT p.lider_id FROM proyecto p WHERE p.id = :proyectoId) x
            JOIN usuario u           ON u.id = x.usuario_id
            LEFT JOIN usuario_rol ur ON ur.usuario_id = u.id
            LEFT JOIN rol r          ON r.id = ur.rol_id
            GROUP BY u.id, u.nombre, u.apellidos, u.ultimo_acceso
            ORDER BY u.nombre
            """, nativeQuery = true)
    List<PersonaFila> personas(@Param("proyectoId") Long proyectoId);

    @Query(value = """
            SELECT pe.nombre AS permiso, u.id AS usuarioId,
                   CONCAT(u.nombre, COALESCE(CONCAT(' ', SUBSTRING_INDEX(u.apellidos, ' ', 1)), '')) AS usuario,
                   up.otorgado AS otorgado
            FROM usuario_permiso up
            JOIN permiso pe ON pe.id = up.permiso_id
            JOIN usuario u  ON u.id = up.usuario_id
            WHERE up.proyecto_id = :proyectoId
            ORDER BY u.nombre, pe.id
            """, nativeQuery = true)
    List<PermisoFila> permisosExtra(@Param("proyectoId") Long proyectoId);

    @Query(value = """
            SELECT u.id AS id, u.nombre AS nombre, u.apellidos AS apellidos, NULL AS ultimoAcceso,
                   GROUP_CONCAT(r.codigo ORDER BY r.id) AS roles
            FROM usuario u
            LEFT JOIN usuario_rol ur ON ur.usuario_id = u.id
            LEFT JOIN rol r          ON r.id = ur.rol_id
            WHERE u.tipo = 'EQUIPO' AND u.activo = TRUE
              AND u.id NOT IN (SELECT m.usuario_id FROM proyecto_miembro m WHERE m.proyecto_id = :proyectoId)
              AND u.id <> (SELECT p.lider_id FROM proyecto p WHERE p.id = :proyectoId)
            GROUP BY u.id, u.nombre, u.apellidos
            ORDER BY u.nombre
            """, nativeQuery = true)
    List<PersonaFila> candidatos(@Param("proyectoId") Long proyectoId);

    @Modifying
    @Query(value = "INSERT INTO proyecto_miembro (proyecto_id, usuario_id, agregado_en) VALUES (:proyectoId, :usuarioId, :fecha)",
            nativeQuery = true)
    void agregar(@Param("proyectoId") Long proyectoId, @Param("usuarioId") Long usuarioId,
                 @Param("fecha") LocalDateTime fecha);

    @Modifying
    @Query(value = "DELETE FROM proyecto_miembro WHERE proyecto_id = :proyectoId AND usuario_id = :usuarioId",
            nativeQuery = true)
    void quitar(@Param("proyectoId") Long proyectoId, @Param("usuarioId") Long usuarioId);

    @Modifying
    @Query(value = "UPDATE ticket SET responsable_id = :nuevo WHERE id IN (:ticketIds) AND responsable_id = :de",
            nativeQuery = true)
    void cambiarResponsable(@Param("ticketIds") Collection<Long> ticketIds, @Param("de") Long de,
                            @Param("nuevo") Long nuevo);

    @Modifying
    @Query(value = "UPDATE ticket SET tester_id = :nuevo WHERE id IN (:ticketIds) AND tester_id = :de",
            nativeQuery = true)
    void cambiarTester(@Param("ticketIds") Collection<Long> ticketIds, @Param("de") Long de,
                       @Param("nuevo") Long nuevo);

    @Modifying
    @Query(value = """
            INSERT INTO comentario (entidad, entidad_id, usuario_id, texto, creado_en)
            VALUES ('TICKET', :ticketId, :usuarioId, :texto, :fecha)
            """, nativeQuery = true)
    void comentar(@Param("ticketId") Long ticketId, @Param("usuarioId") Long usuarioId, @Param("texto") String texto,
                  @Param("fecha") LocalDateTime fecha);

    @Query(value = """
            SELECT CONCAT(u.nombre, COALESCE(CONCAT(' ', SUBSTRING_INDEX(u.apellidos, ' ', 1)), ''))
            FROM usuario u WHERE u.id = :usuarioId
            """, nativeQuery = true)
    String nombreCorto(@Param("usuarioId") Long usuarioId);

    interface ProyectoFila {
        Long getId();
        Long getLiderId();
        Boolean getPermitirAutovalidacion();
        Boolean getComentarioAlRechazar();
        Boolean getRamaAlIniciar();
        Boolean getNotificarClienteFase();
        String getFormaTrabajo();
        Boolean getRequierePr();
    }

    interface PersonaFila {
        Long getId();
        String getNombre();
        String getApellidos();
        LocalDateTime getUltimoAcceso();
        String getRoles();
    }

    interface PermisoFila {
        String getPermiso();
        Long getUsuarioId();
        String getUsuario();
        Boolean getOtorgado();
    }
}
