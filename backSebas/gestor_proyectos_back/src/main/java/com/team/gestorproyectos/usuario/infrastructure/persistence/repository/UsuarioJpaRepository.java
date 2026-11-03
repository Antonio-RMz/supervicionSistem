package com.team.gestorproyectos.usuario.infrastructure.persistence.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.team.gestorproyectos.usuario.infrastructure.persistence.entity.UsuarioEntity;

public interface UsuarioJpaRepository extends JpaRepository<UsuarioEntity, Long> {

    @EntityGraph(attributePaths = {"cliente", "roles"})
    Optional<UsuarioEntity> findByCorreo(String correo);

    @EntityGraph(attributePaths = {"cliente", "roles"})
    Optional<UsuarioEntity> findByIdAndTipo(Long id, String tipo);

    @EntityGraph(attributePaths = "roles")
    List<UsuarioEntity> findByTipoOrderByNombreAscApellidosAsc(String tipo);

    boolean existsByCorreo(String correo);

    @Modifying
    @Query("update UsuarioEntity u set u.ultimoAcceso = :fecha where u.id = :id")
    void actualizarUltimoAcceso(@Param("id") Long id, @Param("fecha") LocalDateTime fecha);

    @Modifying(clearAutomatically = true)
    @Query("update UsuarioEntity u set u.activo = :activo where u.id = :id")
    void actualizarActivo(@Param("id") Long id, @Param("activo") boolean activo);

    @Modifying(clearAutomatically = true)
    @Query("update UsuarioEntity u set u.usuarioGit = :usuarioGit where u.id = :id")
    void actualizarUsuarioGit(@Param("id") Long id, @Param("usuarioGit") String usuarioGit);

    @Modifying
    @Query(value = "INSERT INTO proyecto_miembro (proyecto_id, usuario_id) VALUES (:proyectoId, :usuarioId)",
            nativeQuery = true)
    void agregarAProyecto(@Param("proyectoId") Long proyectoId, @Param("usuarioId") Long usuarioId);

    /** Proyectos donde cada persona es miembro o Líder. Quien no tiene ninguno no aparece. */
    @Query(value = """
            SELECT u.id AS usuarioId, COUNT(p.id) AS proyectos
            FROM usuario u
            JOIN proyecto p ON p.lider_id = u.id
                OR EXISTS (SELECT 1 FROM proyecto_miembro m WHERE m.proyecto_id = p.id AND m.usuario_id = u.id)
            WHERE u.tipo = 'EQUIPO'
            GROUP BY u.id
            """, nativeQuery = true)
    List<ProyectosPorUsuarioFila> contarProyectos();

    interface ProyectosPorUsuarioFila {
        Long getUsuarioId();
        Long getProyectos();
    }
}
