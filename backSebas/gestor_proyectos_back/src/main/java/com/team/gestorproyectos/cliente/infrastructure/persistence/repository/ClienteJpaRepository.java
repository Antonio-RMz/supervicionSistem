package com.team.gestorproyectos.cliente.infrastructure.persistence.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.team.gestorproyectos.cliente.infrastructure.persistence.entity.ClienteEntity;

public interface ClienteJpaRepository extends JpaRepository<ClienteEntity, Long> {

    List<ClienteEntity> findAllByOrderByActivoDescNombreCortoAsc();

    @Query("""
            select count(c) > 0 from ClienteEntity c
            where lower(c.nombreCorto) = lower(:nombreCorto) and (:exceptoId is null or c.id <> :exceptoId)
            """)
    boolean existeNombreCorto(@Param("nombreCorto") String nombreCorto, @Param("exceptoId") Long exceptoId);

    @Modifying(clearAutomatically = true)
    @Query("update ClienteEntity c set c.activo = :activo where c.id = :id")
    void actualizarActivo(@Param("id") Long id, @Param("activo") boolean activo);

    /** Proyectos de cada cliente, del más antiguo al más nuevo. */
    @Query(value = """
            SELECT p.cliente_id AS clienteId, p.id AS id, p.nombre AS nombre
            FROM proyecto p
            WHERE p.cliente_id IN (:clienteIds)
            ORDER BY p.fecha_inicio, p.id
            """, nativeQuery = true)
    List<ProyectoFila> proyectosDe(@Param("clienteIds") Collection<Long> clienteIds);

    interface ProyectoFila {
        Long getClienteId();
        Long getId();
        String getNombre();
    }
}
