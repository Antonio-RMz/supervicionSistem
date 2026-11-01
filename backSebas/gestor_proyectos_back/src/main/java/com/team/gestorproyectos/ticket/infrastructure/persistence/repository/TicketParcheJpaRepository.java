package com.team.gestorproyectos.ticket.infrastructure.persistence.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import com.team.gestorproyectos.proyecto.infrastructure.persistence.entity.TicketEntity;

/** Lo necesario para publicar un parche con los hotfix aprobados (PDF pág. 23). */
public interface TicketParcheJpaRepository extends Repository<TicketEntity, Long> {

    @Query(value = """
            SELECT r.version FROM version_release r
            WHERE r.proyecto_id = :proyectoId AND r.estado = 'PUBLICADA'
            ORDER BY r.publicado_en DESC, r.id DESC
            LIMIT 1
            """, nativeQuery = true)
    Optional<String> versionEnProduccion(@Param("proyectoId") Long proyectoId);

    @Query(value = """
            SELECT t.id AS id, CONCAT(t.tipo, '-', LPAD(t.numero, 2, '0')) AS codigo, t.titulo AS titulo
            FROM ticket t JOIN fase f ON f.id = t.fase_id
            WHERE t.proyecto_id = :proyectoId AND t.tipo = 'BUGS' AND t.estado = 'APROBADO'
              AND f.es_mantenimiento = TRUE AND t.severidad IN ('CRITICA', 'ALTA')
            ORDER BY t.numero
            """, nativeQuery = true)
    List<HotfixFila> hotfixAprobados(@Param("proyectoId") Long proyectoId);

    @Modifying
    @Query(value = """
            INSERT INTO version_release (proyecto_id, version, tipo, estado, resumen, publicado_por, publicado_en)
            VALUES (:proyectoId, :version, 'PARCHE', 'PUBLICADA', :resumen, :usuarioId, :fecha)
            """, nativeQuery = true)
    void registrarRelease(@Param("proyectoId") Long proyectoId, @Param("version") String version,
                          @Param("resumen") String resumen, @Param("usuarioId") Long usuarioId,
                          @Param("fecha") LocalDateTime fecha);

    @Modifying
    @Query(value = "UPDATE ticket SET estado = 'PUBLICADO' WHERE id = :ticketId AND estado = 'APROBADO'",
            nativeQuery = true)
    int publicarBug(@Param("ticketId") Long ticketId);

    @Modifying
    @Query(value = """
            INSERT INTO historial_estado (entidad, entidad_id, estado_anterior, estado_nuevo, motivo, automatico, usuario_id, creado_en)
            VALUES ('TICKET', :ticketId, 'APROBADO', 'PUBLICADO', :motivo, FALSE, :usuarioId, :fecha)
            """, nativeQuery = true)
    void registrarHistorial(@Param("ticketId") Long ticketId, @Param("motivo") String motivo,
                            @Param("usuarioId") Long usuarioId, @Param("fecha") LocalDateTime fecha);

    interface HotfixFila {
        Long getId();
        String getCodigo();
        String getTitulo();
    }
}
