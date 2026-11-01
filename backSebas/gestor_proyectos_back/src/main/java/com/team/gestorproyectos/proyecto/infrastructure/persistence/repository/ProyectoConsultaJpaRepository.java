package com.team.gestorproyectos.proyecto.infrastructure.persistence.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import com.team.gestorproyectos.proyecto.infrastructure.persistence.entity.ProyectoEntity;

/**
 * Consultas de solo lectura de la lista y el detalle de proyectos.
 * "Abierto" = no cerrado, cancelado, no reproducible, rechazado, publicado ni revisado (igual que el Dashboard).
 */
public interface ProyectoConsultaJpaRepository extends Repository<ProyectoEntity, Long> {

    @Query(value = """
            SELECT COUNT(*) FROM proyecto p
            WHERE p.id = :proyectoId
              AND (:esAdmin = TRUE OR p.lider_id = :usuarioId
                   OR EXISTS (SELECT 1 FROM proyecto_miembro m WHERE m.proyecto_id = p.id AND m.usuario_id = :usuarioId))
            """, nativeQuery = true)
    long visible(@Param("proyectoId") Long proyectoId, @Param("usuarioId") Long usuarioId,
                 @Param("esAdmin") boolean esAdmin);

    /** usuarioId NULL = todos (Administrador). */
    @Query(value = """
            SELECT p.id AS id, p.codigo AS codigo, p.nombre AS nombre, p.estado AS estado, p.prioridad AS prioridad,
                   p.cliente_id AS clienteId, COALESCE(c.nombre_corto, 'Interno') AS clienteNombre, l.nombre AS liderNombre,
                   (SELECT COUNT(*) FROM fase f
                    WHERE f.proyecto_id = p.id AND f.es_mantenimiento = FALSE AND f.estado <> 'CANCELADA') AS fases,
                   (SELECT COUNT(*) FROM historia h
                    WHERE h.proyecto_id = p.id AND h.estado <> 'DESCARTADA') AS historias
            FROM proyecto p
            LEFT JOIN cliente c ON c.id = p.cliente_id
            JOIN usuario l ON l.id = p.lider_id
            WHERE :usuarioId IS NULL OR p.lider_id = :usuarioId
               OR EXISTS (SELECT 1 FROM proyecto_miembro m WHERE m.proyecto_id = p.id AND m.usuario_id = :usuarioId)
            ORDER BY FIELD(p.estado, 'EN_CURSO', 'PLANEACION', 'EN_PAUSA', 'TERMINADO', 'CANCELADO'), p.nombre
            """, nativeQuery = true)
    List<ProyectoFila> proyectos(@Param("usuarioId") Long usuarioId);

    @Query(value = """
            SELECT t.proyecto_id AS proyectoId, t.fase_id AS faseId, t.modulo_id AS moduloId, t.estado AS estado,
                   COUNT(*) AS cantidad
            FROM ticket t
            JOIN fase f ON f.id = t.fase_id AND f.es_mantenimiento = FALSE
            WHERE t.proyecto_id IN (:proyectoIds)
            GROUP BY t.proyecto_id, t.fase_id, t.modulo_id, t.estado
            """, nativeQuery = true)
    List<ConteoFila> conteos(@Param("proyectoIds") Collection<Long> proyectoIds);

    /** Cada módulo con el CI de su pull request más reciente. */
    @Query(value = """
            SELECT m.proyecto_id AS proyectoId, m.id AS id, m.tipo AS tipo, m.nombre AS nombre,
                   m.tecnologia AS tecnologia, m.repositorio AS repositorio, m.rama_base AS ramaBase,
                   (SELECT pr.ci_estado FROM pull_request pr WHERE pr.modulo_id = m.id
                    ORDER BY pr.actualizado_en DESC, pr.id DESC LIMIT 1) AS ciEstado,
                   (SELECT pr.actualizado_en FROM pull_request pr WHERE pr.modulo_id = m.id
                    ORDER BY pr.actualizado_en DESC, pr.id DESC LIMIT 1) AS ciFecha
            FROM modulo m
            WHERE m.proyecto_id IN (:proyectoIds)
            ORDER BY m.proyecto_id, m.id
            """, nativeQuery = true)
    List<ModuloFila> modulos(@Param("proyectoIds") Collection<Long> proyectoIds);

    /** Miembros y Líder de cada proyecto, el Líder primero. roles = "ADMIN,LIDER". */
    @Query(value = """
            SELECT x.proyecto_id AS proyectoId, u.id AS id, u.nombre AS nombre, u.apellidos AS apellidos,
                   GROUP_CONCAT(r.codigo) AS roles
            FROM (SELECT m.proyecto_id, m.usuario_id FROM proyecto_miembro m WHERE m.proyecto_id IN (:proyectoIds)
                  UNION
                  SELECT p.id, p.lider_id FROM proyecto p WHERE p.id IN (:proyectoIds)) x
            JOIN proyecto p          ON p.id = x.proyecto_id
            JOIN usuario u           ON u.id = x.usuario_id
            LEFT JOIN usuario_rol ur ON ur.usuario_id = u.id
            LEFT JOIN rol r          ON r.id = ur.rol_id
            GROUP BY x.proyecto_id, u.id, u.nombre, u.apellidos, p.lider_id
            ORDER BY x.proyecto_id, u.id <> p.lider_id, u.nombre
            """, nativeQuery = true)
    List<MiembroFila> equipos(@Param("proyectoIds") Collection<Long> proyectoIds);

    @Query(value = """
            SELECT p.id AS id, p.codigo AS codigo, p.nombre AS nombre, p.descripcion AS descripcion, p.estado AS estado,
                   p.cliente_id AS clienteId, COALESCE(c.nombre_corto, 'Interno') AS clienteNombre,
                   p.prioridad AS prioridad,
                   p.fecha_inicio AS fechaInicio, p.fecha_entrega AS fechaEntrega, p.lider_id AS liderId,
                   TRIM(CONCAT(l.nombre, ' ', COALESCE(l.apellidos, ''))) AS liderNombre,
                   p.permitir_autovalidacion AS permitirAutovalidacion,
                   p.comentario_al_rechazar AS comentarioAlRechazar,
                   p.rama_al_iniciar AS ramaAlIniciar, p.notificar_cliente_fase AS notificarClienteFase
            FROM proyecto p
            LEFT JOIN cliente c ON c.id = p.cliente_id
            JOIN usuario l ON l.id = p.lider_id
            WHERE p.id = :proyectoId
            """, nativeQuery = true)
    Optional<InfoFila> info(@Param("proyectoId") Long proyectoId);

    @Query(value = """
            SELECT f.id AS id, f.numero AS numero, f.nombre AS nombre, f.estado AS estado,
                   f.fecha_inicio AS fechaInicio, f.fecha_fin AS fechaFin, f.version_release AS versionRelease,
                   f.es_mantenimiento AS mantenimiento,
                   (SELECT GROUP_CONCAT(DISTINCT m.tipo
                           ORDER BY FIELD(m.tipo, 'API', 'WEB', 'MOVIL', 'ESCRITORIO', 'BASE_DATOS', 'INFRAESTRUCTURA'))
                    FROM fase_modulo fm JOIN modulo m ON m.id = fm.modulo_id WHERE fm.fase_id = f.id) AS modulos,
                   (SELECT COUNT(*) FROM historia h WHERE h.fase_id = f.id AND h.estado <> 'DESCARTADA') AS historias,
                   (SELECT COUNT(*) FROM ticket t
                    WHERE t.fase_id = f.id AND t.estado NOT IN ('CANCELADA', 'NO_REPRODUCIBLE', 'RECHAZADA')) AS tickets
            FROM fase f
            WHERE f.proyecto_id = :proyectoId AND f.estado <> 'CANCELADA'
            ORDER BY f.es_mantenimiento, f.numero
            """, nativeQuery = true)
    List<FaseFila> fases(@Param("proyectoId") Long proyectoId);

    @Query(value = """
            SELECT COALESCE(SUM(t.estado NOT IN (:cerrados)), 0) AS ticketsAbiertos,
                   COALESCE(SUM(t.tipo = 'BUGS' AND t.estado NOT IN (:cerrados)), 0) AS bugsAbiertos,
                   COALESCE(SUM(t.tipo = 'BUGS' AND f.es_mantenimiento AND t.estado NOT IN (:cerrados)), 0) AS bugsEnProduccion,
                   COALESCE(SUM(f.es_mantenimiento AND t.estado NOT IN (:cerrados)), 0) AS mantenimientoAbiertos
            FROM ticket t
            JOIN fase f ON f.id = t.fase_id
            WHERE t.proyecto_id = :proyectoId
            """, nativeQuery = true)
    MetricasFila metricas(@Param("proyectoId") Long proyectoId, @Param("cerrados") Collection<String> cerrados);

    /**
     * diasQa: de cada salida de QA en los últimos 30 días, cuánto pasó desde que entró. En días, con decimales.
     * historiasSinTickets: historias listas o en desarrollo que aún no tienen ningún ticket.
     */
    @Query(value = """
            SELECT
              (SELECT COUNT(*) FROM historial_estado he
               JOIN ticket t ON he.entidad = 'TICKET' AND t.id = he.entidad_id
               WHERE t.proyecto_id = :proyectoId AND he.estado_anterior = 'EN_QA'
                 AND he.estado_nuevo IN ('EN_PROGRESO', 'EN_CORRECCION') AND he.creado_en >= :inicioDeMes) AS rechazosQaMes,
              (SELECT AVG(TIMESTAMPDIFF(MINUTE, entrada.creado_en, salida.creado_en)) / 1440
               FROM historial_estado salida
               JOIN ticket t ON salida.entidad = 'TICKET' AND t.id = salida.entidad_id
               JOIN historial_estado entrada ON entrada.id = (
                    SELECT e.id FROM historial_estado e
                    WHERE e.entidad = 'TICKET' AND e.entidad_id = salida.entidad_id AND e.estado_nuevo = 'EN_QA'
                      AND e.creado_en <= salida.creado_en
                    ORDER BY e.creado_en DESC, e.id DESC LIMIT 1)
               WHERE t.proyecto_id = :proyectoId AND salida.estado_anterior = 'EN_QA'
                 AND salida.creado_en >= :desde) AS diasQa,
              (SELECT COALESCE(SUM(t.estimacion_horas), 0) FROM ticket t
               WHERE t.proyecto_id = :proyectoId AND t.estado NOT IN ('CANCELADA', 'NO_REPRODUCIBLE', 'RECHAZADA')) AS horasEstimadas,
              (SELECT COALESCE(SUM(t.horas_registradas), 0) FROM ticket t
               WHERE t.proyecto_id = :proyectoId AND t.estado NOT IN ('CANCELADA', 'NO_REPRODUCIBLE', 'RECHAZADA')) AS horasReales,
              (SELECT COUNT(*) FROM historia h
               WHERE h.proyecto_id = :proyectoId AND h.estado IN ('LISTA', 'EN_DESARROLLO')
                 AND NOT EXISTS (SELECT 1 FROM ticket t WHERE t.historia_id = h.id)) AS historiasSinTickets
            """, nativeQuery = true)
    SaludFila salud(@Param("proyectoId") Long proyectoId, @Param("inicioDeMes") LocalDateTime inicioDeMes,
                    @Param("desde") LocalDateTime desde);

    /** Bloqueados, y en QA desde antes de :antes. desde/motivo salen del último cambio a ese estado. */
    @Query(value = """
            SELECT x.id AS ticketId, x.tipo AS tipo, x.numero AS numero, x.titulo AS titulo, x.estado AS estado,
                   x.desde AS desde, x.motivo AS motivo
            FROM (SELECT t.id, t.tipo, t.numero, t.titulo, t.estado,
                         COALESCE((SELECT he.creado_en FROM historial_estado he
                                   WHERE he.entidad = 'TICKET' AND he.entidad_id = t.id AND he.estado_nuevo = t.estado
                                   ORDER BY he.creado_en DESC, he.id DESC LIMIT 1), t.actualizado_en) AS desde,
                         (SELECT he.motivo FROM historial_estado he
                          WHERE he.entidad = 'TICKET' AND he.entidad_id = t.id AND he.estado_nuevo = t.estado
                          ORDER BY he.creado_en DESC, he.id DESC LIMIT 1) AS motivo
                  FROM ticket t
                  WHERE t.proyecto_id = :proyectoId AND t.estado IN ('BLOQUEADA', 'EN_QA')) x
            WHERE x.estado = 'BLOQUEADA' OR x.desde < :antes
            ORDER BY x.estado = 'BLOQUEADA' DESC, x.desde
            """, nativeQuery = true)
    List<DetenidoFila> detenidos(@Param("proyectoId") Long proyectoId, @Param("antes") LocalDateTime antes);

    @Query(value = """
            SELECT h.id AS id, h.fase_id AS faseId, h.numero AS numero, h.titulo AS titulo, h.estado AS estado,
                   h.analista_id AS analistaId,
                   UPPER(CONCAT(LEFT(u.nombre, 1), LEFT(COALESCE(u.apellidos, ''), 1))) AS analistaIniciales
            FROM historia h
            JOIN usuario u ON u.id = h.analista_id
            WHERE h.proyecto_id = :proyectoId AND h.estado <> 'DESCARTADA'
            """, nativeQuery = true)
    List<HistoriaFila> historias(@Param("proyectoId") Long proyectoId);

    @Query(value = """
            SELECT t.id AS id, t.tipo AS tipo, t.numero AS numero, t.titulo AS titulo, t.estado AS estado,
                   t.prioridad AS prioridad, t.responsable_id AS responsableId, t.tester_id AS testerId,
                   t.creado_por AS creadoPorId, h.estado AS historiaEstado,
                   t.creado_en AS creadoEn, t.actualizado_en AS actualizadoEn,
                   p.lider_id AS liderId,
                   (SELECT he.estado_anterior FROM historial_estado he
                    WHERE he.entidad = 'TICKET' AND he.entidad_id = t.id
                    ORDER BY he.creado_en DESC, he.id DESC LIMIT 1) AS ultimoEstadoAnterior,
                   t.fase_id AS faseId, t.historia_id AS historiaId, m.tipo AS moduloTipo,
                   UPPER(CONCAT(LEFT(r.nombre, 1), LEFT(COALESCE(r.apellidos, ''), 1))) AS responsableIniciales
            FROM ticket t
            JOIN proyecto p      ON p.id = t.proyecto_id
            LEFT JOIN historia h ON h.id = t.historia_id
            LEFT JOIN modulo m   ON m.id = t.modulo_id
            LEFT JOIN usuario r  ON r.id = t.responsable_id
            WHERE t.proyecto_id = :proyectoId AND t.estado NOT IN ('CANCELADA', 'NO_REPRODUCIBLE', 'RECHAZADA')
            """, nativeQuery = true)
    List<TicketFila> tickets(@Param("proyectoId") Long proyectoId);

    @Query(value = "SELECT u.id AS id, u.nombre AS nombre FROM usuario u WHERE u.tipo = 'EQUIPO'", nativeQuery = true)
    List<PersonaFila> personasDelEquipo();

    interface ProyectoFila {
        Long getId();
        String getCodigo();
        String getNombre();
        String getEstado();
        String getPrioridad();
        Long getClienteId();
        String getClienteNombre();
        String getLiderNombre();
        Long getFases();
        Long getHistorias();
    }

    interface ConteoFila {
        Long getProyectoId();
        Long getFaseId();
        Long getModuloId();
        String getEstado();
        Long getCantidad();
    }

    interface ModuloFila {
        Long getProyectoId();
        Long getId();
        String getTipo();
        String getNombre();
        String getTecnologia();
        String getRepositorio();
        String getRamaBase();
        String getCiEstado();
        LocalDateTime getCiFecha();
    }

    interface MiembroFila {
        Long getProyectoId();
        Long getId();
        String getNombre();
        String getApellidos();
        String getRoles();
    }

    interface InfoFila {
        Long getId();
        String getCodigo();
        String getNombre();
        String getDescripcion();
        String getEstado();
        Long getClienteId();
        String getClienteNombre();
        String getPrioridad();
        LocalDate getFechaInicio();
        LocalDate getFechaEntrega();
        Long getLiderId();
        String getLiderNombre();
        Boolean getPermitirAutovalidacion();
        Boolean getComentarioAlRechazar();
        Boolean getRamaAlIniciar();
        Boolean getNotificarClienteFase();
    }

    interface FaseFila {
        Long getId();
        Integer getNumero();
        String getNombre();
        String getEstado();
        LocalDate getFechaInicio();
        LocalDate getFechaFin();
        String getVersionRelease();
        Boolean getMantenimiento();
        String getModulos();
        Long getHistorias();
        Long getTickets();
    }

    interface MetricasFila {
        Long getTicketsAbiertos();
        Long getBugsAbiertos();
        Long getBugsEnProduccion();
        Long getMantenimientoAbiertos();
    }

    interface SaludFila {
        Long getRechazosQaMes();
        BigDecimal getDiasQa();
        BigDecimal getHorasEstimadas();
        BigDecimal getHorasReales();
        Long getHistoriasSinTickets();
    }

    interface DetenidoFila {
        Long getTicketId();
        String getTipo();
        Integer getNumero();
        String getTitulo();
        String getEstado();
        LocalDateTime getDesde();
        String getMotivo();
    }

    interface HistoriaFila {
        Long getId();
        Long getFaseId();
        Integer getNumero();
        String getTitulo();
        String getEstado();
        Long getAnalistaId();
        String getAnalistaIniciales();
    }

    interface TicketFila {
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
        LocalDateTime getCreadoEn();
        LocalDateTime getActualizadoEn();
        Long getLiderId();
        String getUltimoEstadoAnterior();
        Long getFaseId();
        Long getHistoriaId();
        String getModuloTipo();
        String getResponsableIniciales();
    }

    interface PersonaFila {
        Long getId();
        String getNombre();
    }
}
