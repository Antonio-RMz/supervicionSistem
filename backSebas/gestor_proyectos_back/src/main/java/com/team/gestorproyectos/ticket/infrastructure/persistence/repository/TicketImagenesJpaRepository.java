package com.team.gestorproyectos.ticket.infrastructure.persistence.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import com.team.gestorproyectos.proyecto.infrastructure.persistence.entity.TicketEntity;

/** Imágenes de un ticket en la tabla adjunto (entidad TICKET). */
public interface TicketImagenesJpaRepository extends Repository<TicketEntity, Long> {

    @Query(value = "SELECT COUNT(*) FROM ticket t WHERE t.id = :ticketId AND t.proyecto_id = :proyectoId",
            nativeQuery = true)
    long contar(@Param("proyectoId") Long proyectoId, @Param("ticketId") Long ticketId);

    @Query(value = """
            SELECT a.id AS id, a.nombre AS nombre, a.tipo_mime AS tipoMime, a.tamano_bytes AS tamanoBytes, a.ruta AS ruta
            FROM adjunto a WHERE a.entidad = 'TICKET' AND a.entidad_id = :ticketId ORDER BY a.id
            """, nativeQuery = true)
    List<AdjuntoFila> adjuntos(@Param("ticketId") Long ticketId);

    @Modifying
    @Query(value = """
            INSERT INTO adjunto (entidad, entidad_id, nombre, tipo_mime, tamano_bytes, ruta, subido_por)
            VALUES ('TICKET', :ticketId, :nombre, :tipoMime, :tamanoBytes, :ruta, :usuarioId)
            """, nativeQuery = true)
    void agregar(@Param("ticketId") Long ticketId, @Param("nombre") String nombre,
                 @Param("tipoMime") String tipoMime, @Param("tamanoBytes") long tamanoBytes,
                 @Param("ruta") String ruta, @Param("usuarioId") Long usuarioId);

    interface AdjuntoFila {
        Long getId();
        String getNombre();
        String getTipoMime();
        Long getTamanoBytes();
        String getRuta();
    }
}
