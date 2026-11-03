package com.team.gestorproyectos.proyecto.infrastructure.persistence.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import com.team.gestorproyectos.proyecto.infrastructure.persistence.entity.ProyectoEntity;

/** El ícono del proyecto (columnas icono_ruta e icono_tipo). */
public interface IconoProyectoJpaRepository extends Repository<ProyectoEntity, Long> {

    @Query(value = """
            SELECT p.lider_id AS liderId, p.icono_ruta AS ruta, p.icono_tipo AS tipoMime
            FROM proyecto p
            WHERE p.id = :proyectoId
              AND (:esAdmin = TRUE OR p.lider_id = :usuarioId
                   OR EXISTS (SELECT 1 FROM proyecto_miembro m WHERE m.proyecto_id = p.id AND m.usuario_id = :usuarioId))
            """, nativeQuery = true)
    Optional<IconoFila> visible(@Param("proyectoId") Long proyectoId, @Param("usuarioId") Long usuarioId,
                                @Param("esAdmin") boolean esAdmin);

    @Modifying
    @Query(value = "UPDATE proyecto SET icono_ruta = :ruta, icono_tipo = :tipo WHERE id = :proyectoId",
            nativeQuery = true)
    void guardar(@Param("proyectoId") Long proyectoId, @Param("ruta") String ruta, @Param("tipo") String tipo);

    interface IconoFila {
        Long getLiderId();
        String getRuta();
        String getTipoMime();
    }
}
