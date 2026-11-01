package com.team.gestorproyectos.usuario.infrastructure.persistence.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.team.gestorproyectos.usuario.infrastructure.persistence.entity.UsuarioPermisoEntity;

public interface UsuarioPermisoJpaRepository extends JpaRepository<UsuarioPermisoEntity, Long> {

    @Query(value = """
            SELECT p.codigo AS permisoCodigo, p.nombre AS permisoNombre,
                   up.proyecto_id AS proyectoId, pr.nombre AS proyectoNombre, up.otorgado AS otorgado
            FROM usuario_permiso up
            JOIN permiso p       ON p.id = up.permiso_id
            LEFT JOIN proyecto pr ON pr.id = up.proyecto_id
            WHERE up.usuario_id = :usuarioId
            ORDER BY up.id
            """, nativeQuery = true)
    List<PermisoExtraFila> buscarDeUsuario(@Param("usuarioId") Long usuarioId);

    /**
     * porRol: 1 si alguno de sus roles lo da. ajuste: permiso extra en "Todos los proyectos"
     * (1 otorgado, 0 quitado) o null. Van como número: MySQL no regresa BOOLEAN desde una subconsulta.
     */
    @Query(value = """
            SELECT EXISTS (SELECT 1 FROM usuario_rol ur
                           JOIN rol_permiso rp ON rp.rol_id = ur.rol_id
                           JOIN permiso p      ON p.id = rp.permiso_id
                           WHERE ur.usuario_id = :usuarioId AND p.codigo = :codigo) AS porRol,
                   (SELECT CAST(up.otorgado AS SIGNED) FROM usuario_permiso up
                    JOIN permiso p ON p.id = up.permiso_id
                    WHERE up.usuario_id = :usuarioId AND p.codigo = :codigo AND up.proyecto_id IS NULL
                    LIMIT 1) AS ajuste
            """, nativeQuery = true)
    PermisoGlobalFila permisoGlobal(@Param("usuarioId") Long usuarioId, @Param("codigo") String codigo);

    @Modifying
    @Query("delete from UsuarioPermisoEntity up where up.usuarioId = :usuarioId")
    void borrarDeUsuario(@Param("usuarioId") Long usuarioId);

    interface PermisoGlobalFila {
        Long getPorRol();
        Long getAjuste();
    }

    interface PermisoExtraFila {
        String getPermisoCodigo();
        String getPermisoNombre();
        Long getProyectoId();
        String getProyectoNombre();
        Boolean getOtorgado();
    }
}
