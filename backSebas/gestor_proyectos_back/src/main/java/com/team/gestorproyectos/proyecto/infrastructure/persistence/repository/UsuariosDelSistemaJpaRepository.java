package com.team.gestorproyectos.proyecto.infrastructure.persistence.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import com.team.gestorproyectos.proyecto.infrastructure.persistence.entity.ProyectoEntity;

/** Los usuarios del sistema de un proyecto (tabla proyecto_usuario_sistema). */
public interface UsuariosDelSistemaJpaRepository extends Repository<ProyectoEntity, Long> {

    @Query(value = """
            SELECT u.nombre AS nombre, u.descripcion AS descripcion
            FROM proyecto_usuario_sistema u
            WHERE u.proyecto_id = :proyectoId
            ORDER BY u.orden, u.id
            """, nativeQuery = true)
    List<UsuarioFila> usuarios(@Param("proyectoId") Long proyectoId);

    @Modifying
    @Query(value = "DELETE FROM proyecto_usuario_sistema WHERE proyecto_id = :proyectoId", nativeQuery = true)
    void quitarTodos(@Param("proyectoId") Long proyectoId);

    @Modifying
    @Query(value = """
            INSERT INTO proyecto_usuario_sistema (proyecto_id, nombre, descripcion, orden)
            VALUES (:proyectoId, :nombre, :descripcion, :orden)
            """, nativeQuery = true)
    void agregar(@Param("proyectoId") Long proyectoId, @Param("nombre") String nombre,
                 @Param("descripcion") String descripcion, @Param("orden") int orden);

    interface UsuarioFila {
        String getNombre();
        String getDescripcion();
    }
}
