package com.team.gestorproyectos.ticket.infrastructure.persistence.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import com.team.gestorproyectos.proyecto.infrastructure.persistence.entity.TicketEntity;

/** Lo que hace falta para mover un ticket de estado y dejarlo en el historial. */
public interface TicketMovimientoJpaRepository extends Repository<TicketEntity, Long> {

    @Query(value = """
            SELECT t.id AS id, t.tipo AS tipo, t.numero AS numero, t.titulo AS titulo, t.estado AS estado,
                   t.prioridad AS prioridad, t.responsable_id AS responsableId, t.tester_id AS testerId,
                   t.creado_por AS creadoPorId, h.estado AS historiaEstado, h.numero AS historiaNumero,
                   t.creado_en AS creadoEn, t.actualizado_en AS actualizadoEn,
                   t.proyecto_id AS proyectoId, p.lider_id AS liderId, f.estado AS faseEstado,
                   f.es_mantenimiento AS faseMantenimiento, t.severidad AS severidad, t.rama AS rama,
                   p.permitir_autovalidacion AS permitirAutovalidacion,
                   p.comentario_al_rechazar AS comentarioAlRechazar,
                   p.rama_al_iniciar AS ramaAlIniciar
            FROM ticket t
            JOIN proyecto p      ON p.id = t.proyecto_id
            JOIN fase f          ON f.id = t.fase_id
            LEFT JOIN historia h ON h.id = t.historia_id
            WHERE t.id = :ticketId
            """, nativeQuery = true)
    Optional<TicketParaMoverFila> buscar(@Param("ticketId") Long ticketId);

    /** Códigos ("ACTV-29") de los tickets de los que depende y que aún no se cierran. */
    @Query(value = """
            SELECT CONCAT(d.tipo, '-', LPAD(d.numero, 2, '0'))
            FROM ticket_dependencia td
            JOIN ticket d ON d.id = td.depende_de_id
            WHERE td.ticket_id = :ticketId
              AND d.estado NOT IN ('CERRADA', 'CERRADO', 'CANCELADA')
            ORDER BY d.tipo, d.numero
            """, nativeQuery = true)
    List<String> dependenciasAbiertas(@Param("ticketId") Long ticketId);

    /**
     * Solo si sigue en el estado que se validó: si alguien más lo movió a la vez, no se pisa.
     * Un hotfix que llega a QA sin quién lo pruebe queda con un Analista del equipo (de preferencia,
     * no quien lo corrigió).
     */
    @Modifying
    @Query(value = """
            UPDATE ticket SET estado = :nuevo, rama = COALESCE(:rama, rama), bloqueado_por_id = :bloqueadoPor,
                              severidad = COALESCE(:severidad, severidad),
                              responsable_id = COALESCE(:responsableId, responsable_id),
                              tester_id = CASE WHEN :nuevo = 'EN_QA' AND tester_id IS NULL THEN
                                  (SELECT u.id FROM usuario u
                                   JOIN usuario_rol ur ON ur.usuario_id = u.id
                                   JOIN rol r ON r.id = ur.rol_id AND r.codigo = 'ANALISTA'
                                   JOIN proyecto p ON p.id = ticket.proyecto_id
                                   WHERE u.activo = TRUE AND (u.id = p.lider_id OR u.id IN
                                       (SELECT m.usuario_id FROM proyecto_miembro m WHERE m.proyecto_id = p.id))
                                   ORDER BY u.id = ticket.responsable_id, u.id LIMIT 1)
                                  ELSE tester_id END
            WHERE id = :ticketId AND estado = :anterior
            """, nativeQuery = true)
    int cambiarEstado(@Param("ticketId") Long ticketId, @Param("anterior") String anterior,
                      @Param("nuevo") String nuevo, @Param("rama") String rama,
                      @Param("bloqueadoPor") Long bloqueadoPor, @Param("severidad") String severidad,
                      @Param("responsableId") Long responsableId);

    @Query(value = "SELECT t.estado FROM ticket t WHERE t.id = :ticketId AND t.proyecto_id = :proyectoId",
            nativeQuery = true)
    Optional<String> estadoDelTicket(@Param("proyectoId") Long proyectoId, @Param("ticketId") Long ticketId);

    @Query(value = """
            SELECT CONCAT(u.nombre, COALESCE(CONCAT(' ', SUBSTRING_INDEX(u.apellidos, ' ', 1)), ''))
            FROM usuario u WHERE u.id = :usuarioId
            """, nativeQuery = true)
    Optional<String> nombreDe(@Param("usuarioId") Long usuarioId);

    @Query(value = """
            SELECT COUNT(*) FROM usuario u
            JOIN proyecto p     ON p.id = :proyectoId
            JOIN usuario_rol ur ON ur.usuario_id = u.id
            JOIN rol r          ON r.id = ur.rol_id AND r.codigo = 'DESARROLLADOR'
            WHERE u.id = :usuarioId AND u.activo = TRUE
              AND (u.id = p.lider_id
                   OR u.id IN (SELECT m.usuario_id FROM proyecto_miembro m WHERE m.proyecto_id = :proyectoId))
            """, nativeQuery = true)
    long esDesarrolladorDelEquipo(@Param("proyectoId") Long proyectoId, @Param("usuarioId") Long usuarioId);

    /** Los que este ticket tiene bloqueados, con su tipo para saber a qué estado regresan. */
    @Query(value = """
            SELECT t.id AS id, t.tipo AS tipo FROM ticket t
            WHERE t.bloqueado_por_id = :ticketId AND t.estado = 'BLOQUEADA'
            """, nativeQuery = true)
    List<BloqueadoFila> bloqueadosPor(@Param("ticketId") Long ticketId);

    @Modifying
    @Query(value = """
            INSERT INTO historial_estado (entidad, entidad_id, estado_anterior, estado_nuevo, motivo, automatico, usuario_id, creado_en)
            VALUES ('TICKET', :ticketId, 'BLOQUEADA', :nuevo, :motivo, TRUE, NULL, :fecha)
            """, nativeQuery = true)
    void registrarDesbloqueo(@Param("ticketId") Long ticketId, @Param("nuevo") String nuevo,
                             @Param("motivo") String motivo, @Param("fecha") LocalDateTime fecha);

    @Modifying
    @Query(value = """
            INSERT INTO historial_estado (entidad, entidad_id, estado_anterior, estado_nuevo, motivo, automatico, usuario_id, creado_en)
            VALUES ('TICKET', :ticketId, :anterior, :nuevo, :motivo, FALSE, :usuarioId, :fecha)
            """, nativeQuery = true)
    void registrarHistorial(@Param("ticketId") Long ticketId, @Param("anterior") String anterior,
                            @Param("nuevo") String nuevo, @Param("motivo") String motivo,
                            @Param("usuarioId") Long usuarioId, @Param("fecha") LocalDateTime fecha);

    @Query(value = """
            SELECT h.id AS id, h.estado AS estado
            FROM ticket t JOIN historia h ON h.id = t.historia_id
            WHERE t.id = :ticketId
            """, nativeQuery = true)
    Optional<HistoriaFila> historiaDe(@Param("ticketId") Long ticketId);

    @Query(value = "SELECT t.tipo AS tipo, t.estado AS estado FROM ticket t WHERE t.historia_id = :historiaId",
            nativeQuery = true)
    List<TicketEstadoFila> ticketsDeHistoria(@Param("historiaId") Long historiaId);

    @Modifying
    @Query(value = "UPDATE historia SET estado = :nuevo WHERE id = :historiaId AND estado = :anterior",
            nativeQuery = true)
    int cambiarEstadoHistoria(@Param("historiaId") Long historiaId, @Param("anterior") String anterior,
                              @Param("nuevo") String nuevo);

    /** Antes de moverla: queda en el historial como automático. */
    @Modifying
    @Query(value = """
            INSERT INTO historial_estado (entidad, entidad_id, estado_anterior, estado_nuevo, motivo, automatico, usuario_id, creado_en)
            SELECT 'HISTORIA', h.id, 'LISTA', IF(f.estado = 'EN_CURSO', 'EN_DESARROLLO', 'LISTA_DESARROLLO'),
                   'Ya tiene una Actividad Lista', TRUE, :usuarioId, :fecha
            FROM historia h JOIN fase f ON f.id = h.fase_id
            WHERE h.id = :historiaId AND h.estado = 'LISTA'
            """, nativeQuery = true)
    void registrarHistoriaConActividadLista(@Param("historiaId") Long historiaId, @Param("usuarioId") Long usuarioId,
                                       @Param("fecha") LocalDateTime fecha);

    @Modifying
    @Query(value = """
            UPDATE historia h JOIN fase f ON f.id = h.fase_id
            SET h.estado = IF(f.estado = 'EN_CURSO', 'EN_DESARROLLO', 'LISTA_DESARROLLO')
            WHERE h.id = :historiaId AND h.estado = 'LISTA'
            """, nativeQuery = true)
    void historiaConActividadLista(@Param("historiaId") Long historiaId);

    @Modifying
    @Query(value = """
            INSERT INTO historial_estado (entidad, entidad_id, estado_anterior, estado_nuevo, motivo, automatico, usuario_id, creado_en)
            VALUES ('HISTORIA', :historiaId, :anterior, :nuevo, :motivo, TRUE, NULL, :fecha)
            """, nativeQuery = true)
    void registrarHistorialHistoria(@Param("historiaId") Long historiaId, @Param("anterior") String anterior,
                                    @Param("nuevo") String nuevo, @Param("motivo") String motivo,
                                    @Param("fecha") LocalDateTime fecha);

    interface BloqueadoFila {
        Long getId();
        String getTipo();
    }

    interface HistoriaFila {
        Long getId();
        String getEstado();
    }

    interface TicketEstadoFila {
        String getTipo();
        String getEstado();
    }

    interface TicketParaMoverFila {
        Long getId();
        String getTipo();
        Integer getNumero();
        String getTitulo();
        String getEstado();
        String getPrioridad();
        Long getResponsableId();
        Long getTesterId();
        Long getCreadoPorId();
        String getHistoriaEstado();
        Integer getHistoriaNumero();
        LocalDateTime getCreadoEn();
        LocalDateTime getActualizadoEn();
        Long getProyectoId();
        Long getLiderId();
        String getFaseEstado();
        Boolean getFaseMantenimiento();
        String getSeveridad();
        String getRama();
        Boolean getPermitirAutovalidacion();
        Boolean getComentarioAlRechazar();
        Boolean getRamaAlIniciar();
    }
}
