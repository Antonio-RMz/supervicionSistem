package com.team.gestorproyectos.proyecto.infrastructure.persistence.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.team.gestorproyectos.proyecto.infrastructure.persistence.entity.ProyectoEntity;

public interface ProyectoJpaRepository extends JpaRepository<ProyectoEntity, Long> {

    List<ProyectoEntity> findByClienteIdOrderByFechaInicio(Long clienteId);

    Optional<ProyectoEntity> findByIdAndClienteId(Long id, Long clienteId);

    /** Analista del equipo del proyecto: es el contacto del cliente. */
    @Query(value = """
            SELECT CONCAT(u.nombre, ' ', SUBSTRING_INDEX(COALESCE(u.apellidos, ''), ' ', 1)) AS nombre,
                   'Analista' AS rol,
                   u.correo AS correo
            FROM proyecto_miembro m
            JOIN usuario u      ON u.id = m.usuario_id
            JOIN usuario_rol ur ON ur.usuario_id = u.id
            JOIN rol r          ON r.id = ur.rol_id
            WHERE m.proyecto_id = :proyectoId AND r.codigo = 'ANALISTA' AND u.activo = TRUE
            ORDER BY m.agregado_en
            LIMIT 1
            """, nativeQuery = true)
    Optional<ContactoFila> buscarAnalista(@Param("proyectoId") Long proyectoId);

    /** usuarioId NULL = todos los proyectos (Administrador). */
    @Query(value = """
            SELECT p.id AS id, p.codigo AS codigo, p.nombre AS nombre, p.estado AS estado,
                   COALESCE(c.nombre_corto, 'Interno') AS clienteNombre
            FROM proyecto p
            LEFT JOIN cliente c ON c.id = p.cliente_id
            WHERE :usuarioId IS NULL
               OR p.id IN (SELECT m.proyecto_id FROM proyecto_miembro m WHERE m.usuario_id = :usuarioId)
            ORDER BY FIELD(p.estado, 'EN_CURSO', 'PLANEACION', 'EN_PAUSA', 'TERMINADO', 'CANCELADO'), p.nombre
            """, nativeQuery = true)
    List<ProyectoResumenFila> listarResumenes(@Param("usuarioId") Long usuarioId);

    @Query("select p.codigo from ProyectoEntity p")
    Set<String> codigos();

    @Query(value = "SELECT COUNT(*) FROM cliente c WHERE c.id = :clienteId AND c.activo = TRUE", nativeQuery = true)
    long clienteActivo(@Param("clienteId") Long clienteId);

    @Query(value = """
            SELECT u.id FROM usuario u
            WHERE u.id IN (:usuarioIds) AND u.tipo = 'EQUIPO' AND u.activo = TRUE
            """, nativeQuery = true)
    Set<Long> personasActivas(@Param("usuarioIds") Collection<Long> usuarioIds);

    @Query(value = """
            SELECT COUNT(*) FROM usuario_rol ur JOIN rol r ON r.id = ur.rol_id
            WHERE ur.usuario_id = :usuarioId AND r.codigo = 'LIDER'
            """, nativeQuery = true)
    long esLider(@Param("usuarioId") Long usuarioId);

    @Query(value = "SELECT m.usuario_id FROM proyecto_miembro m WHERE m.proyecto_id = :proyectoId", nativeQuery = true)
    Set<Long> miembros(@Param("proyectoId") Long proyectoId);

    @Modifying
    @Query(value = "INSERT INTO proyecto_miembro (proyecto_id, usuario_id) VALUES (:proyectoId, :usuarioId)",
            nativeQuery = true)
    void agregarMiembro(@Param("proyectoId") Long proyectoId, @Param("usuarioId") Long usuarioId);

    @Modifying
    @Query(value = "DELETE FROM proyecto_miembro WHERE proyecto_id = :proyectoId AND usuario_id IN (:usuarioIds)",
            nativeQuery = true)
    void quitarMiembros(@Param("proyectoId") Long proyectoId, @Param("usuarioIds") Collection<Long> usuarioIds);

    /** Módulos del proyecto que ya tienen tickets o están en alguna fase. */
    @Query(value = """
            SELECT m.id FROM modulo m
            WHERE m.proyecto_id = :proyectoId
              AND (EXISTS (SELECT 1 FROM ticket t WHERE t.modulo_id = m.id)
                   OR EXISTS (SELECT 1 FROM fase_modulo fm WHERE fm.modulo_id = m.id)
                   OR EXISTS (SELECT 1 FROM pull_request pr WHERE pr.modulo_id = m.id))
            """, nativeQuery = true)
    Set<Long> modulosConTrabajo(@Param("proyectoId") Long proyectoId);

    interface ProyectoResumenFila {
        Long getId();
        String getCodigo();
        String getNombre();
        String getEstado();
        String getClienteNombre();
    }

    interface ContactoFila {
        String getNombre();
        String getRol();
        String getCorreo();
    }
}
