package com.team.gestorproyectos.ticket.infrastructure.persistence.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import com.team.gestorproyectos.proyecto.infrastructure.persistence.entity.TicketEntity;

/** Lo que muestra el detalle del ticket (PDF pág. 29): descripción, subtareas, PR, criterios, historial... */
public interface TicketDetalleJpaRepository extends Repository<TicketEntity, Long> {

    @Query(value = """
            SELECT t.id AS id, t.proyecto_id AS proyectoId, p.nombre AS proyectoNombre, f.id AS faseId,
                   f.numero AS faseNumero, f.nombre AS faseNombre, h.id AS historiaId, h.numero AS historiaNumero,
                   h.titulo AS historiaTitulo, h.estado AS historiaEstado, h.como AS como, h.quiero AS quiero,
                   h.para AS para, t.tipo AS tipo, t.numero AS numero, t.titulo AS titulo,
                   t.descripcion AS descripcion, t.estado AS estado, t.prioridad AS prioridad, t.capas AS capas,
                   m.tipo AS moduloTipo, m.nombre AS moduloNombre, m.tecnologia AS moduloTecnologia,
                   m.repositorio AS moduloRepositorio, m.rama_base AS moduloRamaBase,
                   r.id AS responsableId, CONCAT(r.nombre, COALESCE(CONCAT(' ', SUBSTRING_INDEX(r.apellidos, ' ', 1)), '')) AS responsableNombre,
                   q.id AS testerId, CONCAT(q.nombre, COALESCE(CONCAT(' ', SUBSTRING_INDEX(q.apellidos, ' ', 1)), '')) AS testerNombre,
                   c.id AS creadoPorId, CONCAT(c.nombre, COALESCE(CONCAT(' ', SUBSTRING_INDEX(c.apellidos, ' ', 1)), '')) AS creadoPorNombre,
                   t.estimacion_horas AS estimacionHoras, t.horas_registradas AS horasRegistradas,
                   t.fecha_limite AS fechaLimite, t.rama AS rama,
                   CASE WHEN b.id IS NULL THEN NULL ELSE CONCAT(b.tipo, '-', LPAD(b.numero, 2, '0')) END AS bloqueadoPor,
                   (SELECT COUNT(*) FROM historial_estado he
                    WHERE he.entidad = 'TICKET' AND he.entidad_id = t.id
                      AND he.estado_anterior = 'EN_QA'
                      AND he.estado_nuevo IN ('EN_PROGRESO', 'EN_CORRECCION')
                      AND he.usuario_id <> t.responsable_id) AS rechazosEnQa
            FROM ticket t
            JOIN proyecto p      ON p.id = t.proyecto_id
            JOIN fase f          ON f.id = t.fase_id
            JOIN usuario c       ON c.id = t.creado_por
            LEFT JOIN historia h ON h.id = t.historia_id
            LEFT JOIN modulo m   ON m.id = t.modulo_id
            LEFT JOIN usuario r  ON r.id = t.responsable_id
            LEFT JOIN usuario q  ON q.id = t.tester_id
            LEFT JOIN ticket b   ON b.id = t.bloqueado_por_id
            WHERE t.id = :ticketId AND t.proyecto_id = :proyectoId
            """, nativeQuery = true)
    Optional<TicketFila> ticket(@Param("proyectoId") Long proyectoId, @Param("ticketId") Long ticketId);

    @Query(value = """
            SELECT CONCAT(d.tipo, '-', LPAD(d.numero, 2, '0')) AS codigo, d.titulo AS titulo, d.estado AS estado
            FROM ticket_dependencia td JOIN ticket d ON d.id = td.depende_de_id
            WHERE td.ticket_id = :ticketId
            ORDER BY d.tipo, d.numero
            """, nativeQuery = true)
    List<DependenciaFila> dependencias(@Param("ticketId") Long ticketId);

    @Query(value = """
            SELECT s.id AS id, s.descripcion AS descripcion, s.hecha AS hecha
            FROM ticket_subtarea s WHERE s.ticket_id = :ticketId ORDER BY s.orden, s.id
            """, nativeQuery = true)
    List<SubtareaFila> subtareas(@Param("ticketId") Long ticketId);

    @Query(value = """
            SELECT pr.numero AS numero, pr.rama_origen AS ramaOrigen, pr.rama_destino AS ramaDestino,
                   pr.estado AS estado, pr.ci_estado AS ciEstado, pr.url AS url
            FROM pull_request pr WHERE pr.ticket_id = :ticketId ORDER BY pr.numero
            """, nativeQuery = true)
    List<PullRequestFila> pullRequests(@Param("ticketId") Long ticketId);

    /** Los criterios que cubre y si este es el único ticket (no cancelado) que los cubre. */
    @Query(value = """
            SELECT c.descripcion AS descripcion, c.verificado AS verificado,
                   NOT EXISTS (SELECT 1 FROM ticket_criterio otro JOIN ticket o ON o.id = otro.ticket_id
                               WHERE otro.criterio_id = c.id AND otro.ticket_id <> :ticketId
                                 AND o.estado NOT IN ('CANCELADA', 'NO_REPRODUCIBLE', 'RECHAZADA')) AS soloEste
            FROM ticket_criterio tc JOIN historia_criterio c ON c.id = tc.criterio_id
            WHERE tc.ticket_id = :ticketId
            ORDER BY c.orden, c.id
            """, nativeQuery = true)
    List<CriterioFila> criterios(@Param("ticketId") Long ticketId);

    /** Del más reciente al más antiguo. */
    @Query(value = """
            SELECT h.estado_anterior AS estadoAnterior, h.estado_nuevo AS estado, h.motivo AS motivo,
                   h.automatico AS automatico,
                   CONCAT(u.nombre, COALESCE(CONCAT(' ', SUBSTRING_INDEX(u.apellidos, ' ', 1)), '')) AS usuario,
                   h.creado_en AS fecha
            FROM historial_estado h
            LEFT JOIN usuario u ON u.id = h.usuario_id
            WHERE h.entidad = 'TICKET' AND h.entidad_id = :ticketId
            ORDER BY h.creado_en DESC, h.id DESC
            """, nativeQuery = true)
    List<CambioFila> historial(@Param("ticketId") Long ticketId);

    @Query(value = """
            SELECT CONCAT(u.nombre, COALESCE(CONCAT(' ', SUBSTRING_INDEX(u.apellidos, ' ', 1)), '')) AS usuario,
                   c.texto AS texto, c.creado_en AS fecha
            FROM comentario c JOIN usuario u ON u.id = c.usuario_id
            WHERE c.entidad = 'TICKET' AND c.entidad_id = :ticketId
            ORDER BY c.creado_en, c.id
            """, nativeQuery = true)
    List<ComentarioFila> comentarios(@Param("ticketId") Long ticketId);

    @Modifying
    @Query(value = "UPDATE ticket_subtarea SET hecha = :hecha WHERE id = :subtareaId AND ticket_id = :ticketId",
            nativeQuery = true)
    int marcarSubtarea(@Param("ticketId") Long ticketId, @Param("subtareaId") Long subtareaId,
                       @Param("hecha") boolean hecha);

    @Modifying
    @Query(value = """
            INSERT INTO ticket_subtarea (ticket_id, descripcion, orden)
            SELECT :ticketId, :descripcion, COALESCE(MAX(s.orden), 0) + 1 FROM ticket_subtarea s WHERE s.ticket_id = :ticketId
            """, nativeQuery = true)
    void agregarSubtarea(@Param("ticketId") Long ticketId, @Param("descripcion") String descripcion);

    @Modifying
    @Query(value = "DELETE FROM ticket_subtarea WHERE id = :subtareaId AND ticket_id = :ticketId", nativeQuery = true)
    int quitarSubtarea(@Param("ticketId") Long ticketId, @Param("subtareaId") Long subtareaId);

    @Modifying
    @Query(value = """
            INSERT INTO comentario (entidad, entidad_id, usuario_id, texto, creado_en)
            VALUES ('TICKET', :ticketId, :usuarioId, :texto, :fecha)
            """, nativeQuery = true)
    void comentar(@Param("ticketId") Long ticketId, @Param("usuarioId") Long usuarioId, @Param("texto") String texto,
                  @Param("fecha") LocalDateTime fecha);

    interface TicketFila {
        Long getId();
        Long getProyectoId();
        String getProyectoNombre();
        Long getFaseId();
        Integer getFaseNumero();
        String getFaseNombre();
        Long getHistoriaId();
        Integer getHistoriaNumero();
        String getHistoriaTitulo();
        String getHistoriaEstado();
        String getComo();
        String getQuiero();
        String getPara();
        String getTipo();
        Integer getNumero();
        String getTitulo();
        String getDescripcion();
        String getEstado();
        String getPrioridad();
        String getCapas();
        String getModuloTipo();
        String getModuloNombre();
        String getModuloTecnologia();
        String getModuloRepositorio();
        String getModuloRamaBase();
        Long getResponsableId();
        String getResponsableNombre();
        Long getTesterId();
        String getTesterNombre();
        Long getCreadoPorId();
        String getCreadoPorNombre();
        BigDecimal getEstimacionHoras();
        BigDecimal getHorasRegistradas();
        LocalDate getFechaLimite();
        String getRama();
        String getBloqueadoPor();
        Long getRechazosEnQa();
    }

    interface DependenciaFila {
        String getCodigo();
        String getTitulo();
        String getEstado();
    }

    interface SubtareaFila {
        Long getId();
        String getDescripcion();
        Boolean getHecha();
    }

    interface PullRequestFila {
        Integer getNumero();
        String getRamaOrigen();
        String getRamaDestino();
        String getEstado();
        String getCiEstado();
        String getUrl();
    }

    interface CriterioFila {
        String getDescripcion();
        Boolean getVerificado();
        Long getSoloEste();
    }

    interface CambioFila {
        String getEstadoAnterior();
        String getEstado();
        String getMotivo();
        Boolean getAutomatico();
        String getUsuario();
        LocalDateTime getFecha();
    }

    interface ComentarioFila {
        String getUsuario();
        String getTexto();
        LocalDateTime getFecha();
    }
}
