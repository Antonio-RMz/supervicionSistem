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

/** Lecturas para preparar un ticket nuevo y las tablas que lo acompañan (criterios, subtareas, dependencias). */
public interface TicketEscrituraJpaRepository extends Repository<TicketEntity, Long> {

    @Query(value = """
            SELECT p.id AS id, p.nombre AS nombre, p.lider_id AS liderId
            FROM proyecto p
            WHERE p.id = :proyectoId
              AND (:esAdmin = TRUE OR p.lider_id = :usuarioId
                   OR EXISTS (SELECT 1 FROM proyecto_miembro m WHERE m.proyecto_id = p.id AND m.usuario_id = :usuarioId))
            """, nativeQuery = true)
    Optional<ProyectoFila> proyectoVisible(@Param("proyectoId") Long proyectoId, @Param("usuarioId") Long usuarioId,
                                           @Param("esAdmin") boolean esAdmin);

    @Query(value = """
            SELECT f.id AS id, f.numero AS numero, f.nombre AS nombre
            FROM fase f
            WHERE f.proyecto_id = :proyectoId AND f.es_mantenimiento = FALSE
              AND f.estado IN ('PLANEADA', 'EN_CURSO', 'PAUSADA')
            ORDER BY f.numero
            """, nativeQuery = true)
    List<FaseFila> fases(@Param("proyectoId") Long proyectoId);

    @Query(value = """
            SELECT fm.fase_id AS faseId, m.id AS id, m.tipo AS tipo, m.nombre AS nombre, m.tecnologia AS tecnologia,
                   m.repositorio AS repositorio, m.rama_base AS ramaBase
            FROM fase_modulo fm
            JOIN modulo m ON m.id = fm.modulo_id
            JOIN fase f   ON f.id = fm.fase_id
            WHERE f.proyecto_id = :proyectoId
            ORDER BY FIELD(m.tipo, 'API', 'WEB', 'MOVIL', 'ESCRITORIO', 'BASE_DATOS', 'INFRAESTRUCTURA'), m.id
            """, nativeQuery = true)
    List<ModuloFila> modulos(@Param("proyectoId") Long proyectoId);

    /** Solo las historias que ya pueden tener tickets: Lista o En desarrollo. */
    @Query(value = """
            SELECT h.id AS id, h.fase_id AS faseId, h.numero AS numero, h.titulo AS titulo, h.estado AS estado,
                   h.como AS como, h.quiero AS quiero, h.para AS para,
                   TRIM(CONCAT(u.nombre, ' ', COALESCE(u.apellidos, ''))) AS analistaNombre,
                   (SELECT COUNT(*) FROM ticket t WHERE t.historia_id = h.id) AS tickets,
                   (SELECT GROUP_CONCAT(hm.modulo_id) FROM historia_modulo hm WHERE hm.historia_id = h.id) AS modulos
            FROM historia h
            JOIN usuario u ON u.id = h.analista_id
            WHERE h.proyecto_id = :proyectoId AND h.estado IN ('LISTA', 'LISTA_DESARROLLO', 'EN_DESARROLLO')
            ORDER BY h.numero
            """, nativeQuery = true)
    List<HistoriaFila> historias(@Param("proyectoId") Long proyectoId);

    /** cubiertoPor: "ACTV-42 · portal-app", el primer ticket vigente que ya cubre el criterio. */
    @Query(value = """
            SELECT c.historia_id AS historiaId, c.id AS id, c.descripcion AS descripcion,
                   (SELECT CONCAT(t.tipo, '-', LPAD(t.numero, 2, '0'), COALESCE(CONCAT(' · ', m.nombre), ''))
                    FROM ticket_criterio tc
                    JOIN ticket t      ON t.id = tc.ticket_id
                    LEFT JOIN modulo m ON m.id = t.modulo_id
                    WHERE tc.criterio_id = c.id AND t.estado NOT IN ('CANCELADA', 'NO_REPRODUCIBLE', 'RECHAZADA')
                    ORDER BY t.id LIMIT 1) AS cubiertoPor
            FROM historia_criterio c
            JOIN historia h ON h.id = c.historia_id
            WHERE h.proyecto_id = :proyectoId AND h.estado IN ('LISTA', 'LISTA_DESARROLLO', 'EN_DESARROLLO')
            ORDER BY c.orden, c.id
            """, nativeQuery = true)
    List<CriterioFila> criterios(@Param("proyectoId") Long proyectoId);

    @Query(value = """
            SELECT u.id AS id, u.nombre AS nombre, u.apellidos AS apellidos, GROUP_CONCAT(r.codigo) AS roles
            FROM usuario u
            JOIN proyecto p          ON p.id = :proyectoId
            LEFT JOIN usuario_rol ur ON ur.usuario_id = u.id
            LEFT JOIN rol r          ON r.id = ur.rol_id
            WHERE u.activo = TRUE
              AND (u.id = p.lider_id
                   OR u.id IN (SELECT m.usuario_id FROM proyecto_miembro m WHERE m.proyecto_id = :proyectoId))
            GROUP BY u.id, u.nombre, u.apellidos
            ORDER BY u.nombre
            """, nativeQuery = true)
    List<PersonaFila> equipo(@Param("proyectoId") Long proyectoId);

    @Query(value = """
            SELECT t.tipo AS tipo, MAX(t.numero) AS maximo FROM ticket t
            WHERE t.proyecto_id = :proyectoId GROUP BY t.tipo
            """, nativeQuery = true)
    List<MaximoFila> maximos(@Param("proyectoId") Long proyectoId);

    @Query(value = """
            SELECT t.id AS id, t.tipo AS tipo, t.numero AS numero, t.titulo AS titulo, t.estado AS estado
            FROM ticket t
            WHERE t.proyecto_id = :proyectoId
              AND t.estado NOT IN ('CERRADA', 'CERRADO', 'CANCELADA', 'NO_REPRODUCIBLE', 'RECHAZADA')
            ORDER BY t.tipo, t.numero
            """, nativeQuery = true)
    List<AbiertoFila> abiertos(@Param("proyectoId") Long proyectoId);

    @Query(value = """
            SELECT t.id AS id, t.tipo AS tipo, t.numero AS numero, t.estado AS estado, t.creado_por AS creadoPorId,
                   p.lider_id AS liderId, t.fase_id AS faseId, t.historia_id AS historiaId, t.titulo AS titulo,
                   t.descripcion AS descripcion, t.modulo_id AS moduloId, t.capas AS capas,
                   t.responsable_id AS responsableId, t.tester_id AS testerId, t.prioridad AS prioridad,
                   t.estimacion_horas AS estimacionHoras, t.fecha_limite AS fechaLimite,
                   (SELECT GROUP_CONCAT(tc.criterio_id) FROM ticket_criterio tc WHERE tc.ticket_id = t.id) AS criterioIds,
                   (SELECT GROUP_CONCAT(td.depende_de_id) FROM ticket_dependencia td WHERE td.ticket_id = t.id) AS dependeDe
            FROM ticket t
            JOIN proyecto p ON p.id = t.proyecto_id
            WHERE t.id = :ticketId AND t.proyecto_id = :proyectoId AND t.historia_id IS NOT NULL
            """, nativeQuery = true)
    Optional<EditableFila> editable(@Param("proyectoId") Long proyectoId, @Param("ticketId") Long ticketId);

    @Modifying
    @Query(value = """
            UPDATE ticket SET titulo = :titulo, descripcion = :descripcion, modulo_id = :moduloId, capas = :capas,
                   responsable_id = :responsableId, tester_id = :testerId, prioridad = :prioridad,
                   estimacion_horas = :estimacionHoras, fecha_limite = :fechaLimite
            WHERE id = :ticketId
            """, nativeQuery = true)
    void actualizar(@Param("ticketId") Long ticketId, @Param("titulo") String titulo,
                    @Param("descripcion") String descripcion, @Param("moduloId") Long moduloId,
                    @Param("capas") String capas, @Param("responsableId") Long responsableId,
                    @Param("testerId") Long testerId, @Param("prioridad") String prioridad,
                    @Param("estimacionHoras") BigDecimal estimacionHoras,
                    @Param("fechaLimite") LocalDate fechaLimite);

    @Modifying
    @Query(value = "DELETE FROM ticket_criterio WHERE ticket_id = :ticketId", nativeQuery = true)
    void quitarCriterios(@Param("ticketId") Long ticketId);

    @Modifying
    @Query(value = "DELETE FROM ticket_dependencia WHERE ticket_id = :ticketId", nativeQuery = true)
    void quitarDependencias(@Param("ticketId") Long ticketId);

    @Modifying
    @Query(value = "INSERT INTO ticket_criterio (ticket_id, criterio_id) VALUES (:ticketId, :criterioId)",
            nativeQuery = true)
    void agregarCriterio(@Param("ticketId") Long ticketId, @Param("criterioId") Long criterioId);

    @Modifying
    @Query(value = "INSERT INTO ticket_subtarea (ticket_id, descripcion, orden) VALUES (:ticketId, :texto, :orden)",
            nativeQuery = true)
    void agregarSubtarea(@Param("ticketId") Long ticketId, @Param("texto") String texto, @Param("orden") int orden);

    @Modifying
    @Query(value = "INSERT INTO ticket_dependencia (ticket_id, depende_de_id) VALUES (:ticketId, :dependeDeId)",
            nativeQuery = true)
    void agregarDependencia(@Param("ticketId") Long ticketId, @Param("dependeDeId") Long dependeDeId);

    @Modifying
    @Query(value = """
            INSERT INTO historial_estado (entidad, entidad_id, estado_anterior, estado_nuevo, automatico, usuario_id, creado_en)
            VALUES ('TICKET', :ticketId, NULL, :estado, FALSE, :usuarioId, :fecha)
            """, nativeQuery = true)
    void registrarCreacion(@Param("ticketId") Long ticketId, @Param("estado") String estado,
                           @Param("usuarioId") Long usuarioId, @Param("fecha") LocalDateTime fecha);

    interface EditableFila {
        Long getId();
        String getTipo();
        Integer getNumero();
        String getEstado();
        Long getCreadoPorId();
        Long getLiderId();
        Long getFaseId();
        Long getHistoriaId();
        String getTitulo();
        String getDescripcion();
        Long getModuloId();
        String getCapas();
        Long getResponsableId();
        Long getTesterId();
        String getPrioridad();
        BigDecimal getEstimacionHoras();
        LocalDate getFechaLimite();
        String getCriterioIds();
        String getDependeDe();
    }

    interface ProyectoFila {
        Long getId();
        String getNombre();
        Long getLiderId();
    }

    interface FaseFila {
        Long getId();
        Integer getNumero();
        String getNombre();
    }

    interface ModuloFila {
        Long getFaseId();
        Long getId();
        String getTipo();
        String getNombre();
        String getTecnologia();
        String getRepositorio();
        String getRamaBase();
    }

    interface HistoriaFila {
        Long getId();
        Long getFaseId();
        Integer getNumero();
        String getTitulo();
        String getEstado();
        String getComo();
        String getQuiero();
        String getPara();
        String getAnalistaNombre();
        Long getTickets();
        String getModulos();
    }

    interface CriterioFila {
        Long getHistoriaId();
        Long getId();
        String getDescripcion();
        String getCubiertoPor();
    }

    interface PersonaFila {
        Long getId();
        String getNombre();
        String getApellidos();
        String getRoles();
    }

    interface MaximoFila {
        String getTipo();
        Integer getMaximo();
    }

    interface AbiertoFila {
        Long getId();
        String getTipo();
        Integer getNumero();
        String getTitulo();
        String getEstado();
    }
}
