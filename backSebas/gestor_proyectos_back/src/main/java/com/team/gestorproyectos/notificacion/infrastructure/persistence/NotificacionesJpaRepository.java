package com.team.gestorproyectos.notificacion.infrastructure.persistence;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import com.team.gestorproyectos.proyecto.infrastructure.persistence.entity.TicketEntity;

/** La tabla notificacion, con consultas nativas. */
public interface NotificacionesJpaRepository extends Repository<TicketEntity, Long> {

    @Query(value = """
            SELECT n.id AS id, n.tipo AS tipo, n.texto AS texto, n.proyecto_id AS proyectoId,
                   n.ticket_id AS ticketId, n.historia_id AS historiaId, n.leida AS leida, n.creado_en AS creadoEn
            FROM notificacion n
            WHERE n.usuario_id = :usuarioId
            ORDER BY n.id DESC
            LIMIT :limite
            """, nativeQuery = true)
    List<NotificacionFila> recientes(@Param("usuarioId") Long usuarioId, @Param("limite") int limite);

    @Query(value = "SELECT COUNT(*) FROM notificacion n WHERE n.usuario_id = :usuarioId AND n.leida = FALSE",
            nativeQuery = true)
    long noLeidas(@Param("usuarioId") Long usuarioId);

    @Modifying
    @Query(value = "UPDATE notificacion SET leida = TRUE WHERE id = :id AND usuario_id = :usuarioId",
            nativeQuery = true)
    void marcarLeida(@Param("usuarioId") Long usuarioId, @Param("id") Long id);

    @Modifying
    @Query(value = "UPDATE notificacion SET leida = TRUE WHERE usuario_id = :usuarioId AND leida = FALSE",
            nativeQuery = true)
    void marcarTodasLeidas(@Param("usuarioId") Long usuarioId);

    /** Lo que el texto necesita del ticket: código, título, severidad y proyecto. */
    @Query(value = """
            SELECT t.proyecto_id AS proyectoId, t.tipo AS tipo, t.numero AS numero, t.titulo AS titulo,
                   t.severidad AS severidad
            FROM ticket t WHERE t.id = :ticketId
            """, nativeQuery = true)
    Optional<TicketFila> ticket(@Param("ticketId") Long ticketId);

    @Query(value = """
            SELECT CONCAT(u.nombre, COALESCE(CONCAT(' ', SUBSTRING_INDEX(u.apellidos, ' ', 1)), ''))
            FROM usuario u WHERE u.id = :usuarioId
            """, nativeQuery = true)
    Optional<String> nombre(@Param("usuarioId") Long usuarioId);

    @Modifying
    @Query(value = """
            INSERT INTO notificacion (usuario_id, tipo, texto, proyecto_id, ticket_id, creado_en)
            VALUES (:usuarioId, :tipo, :texto, :proyectoId, :ticketId, :fecha)
            """, nativeQuery = true)
    void guardar(@Param("usuarioId") Long usuarioId, @Param("tipo") String tipo, @Param("texto") String texto,
                 @Param("proyectoId") Long proyectoId, @Param("ticketId") Long ticketId,
                 @Param("fecha") LocalDateTime fecha);

    @Query(value = "SELECT h.proyecto_id AS proyectoId, h.numero AS numero, h.titulo AS titulo FROM historia h WHERE h.id = :historiaId",
            nativeQuery = true)
    Optional<HistoriaFila> historia(@Param("historiaId") Long historiaId);

    @Modifying
    @Query(value = """
            INSERT INTO notificacion (usuario_id, tipo, texto, proyecto_id, historia_id, creado_en)
            VALUES (:usuarioId, 'ASIGNACION', :texto, :proyectoId, :historiaId, :fecha)
            """, nativeQuery = true)
    void guardarDeHistoria(@Param("usuarioId") Long usuarioId, @Param("texto") String texto,
                           @Param("proyectoId") Long proyectoId, @Param("historiaId") Long historiaId,
                           @Param("fecha") LocalDateTime fecha);

    interface HistoriaFila {
        Long getProyectoId();
        Integer getNumero();
        String getTitulo();
    }

    interface NotificacionFila {
        Long getId();
        String getTipo();
        String getTexto();
        Long getProyectoId();
        Long getTicketId();
        Long getHistoriaId();
        Boolean getLeida();
        LocalDateTime getCreadoEn();
    }

    interface TicketFila {
        Long getProyectoId();
        String getTipo();
        Integer getNumero();
        String getTitulo();
        String getSeveridad();
    }
}
