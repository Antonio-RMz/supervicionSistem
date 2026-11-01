package com.team.gestorproyectos.fase.infrastructure.persistence.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import com.team.gestorproyectos.proyecto.infrastructure.persistence.entity.FaseEntity;

/** Detalle de la fase y sus cambios de estado con todo lo que arrastran (PDF pág. 20, 25 y 32). */
public interface FaseGestionJpaRepository extends Repository<FaseEntity, Long> {

    // ─── Lecturas ───────────────────────────────────────────────────────────

    @Query(value = "SELECT f.id FROM fase f WHERE f.proyecto_id = :proyectoId ORDER BY f.numero", nativeQuery = true)
    List<Long> fases(@Param("proyectoId") Long proyectoId);

    @Query(value = """
            SELECT f.id AS id, f.proyecto_id AS proyectoId, p.nombre AS proyectoNombre, p.codigo AS proyectoCodigo,
                   COALESCE(c.nombre_corto, 'Interno') AS clienteNombre, f.numero AS numero, f.nombre AS nombre, f.objetivo AS objetivo,
                   f.estado AS estado, f.prioridad AS prioridad, f.fecha_inicio AS fechaInicio,
                   f.fecha_fin AS fechaFin, p.lider_id AS liderId, f.responsable_id AS responsableId,
                   CONCAT(u.nombre, COALESCE(CONCAT(' ', u.apellidos), '')) AS responsableNombre,
                   f.version_release AS versionRelease, f.es_mantenimiento AS mantenimiento,
                   f.motivo_estado AS motivoEstado, f.reanudar_el AS reanudarEl,
                   f.figma_url AS figmaUrl, f.figma_para_todas AS figmaParaTodas,
                   b.id AS bugId, b.tipo AS bugTipo, b.numero AS bugNumero, b.estado AS bugEstado
            FROM fase f
            JOIN proyecto p    ON p.id = f.proyecto_id
            LEFT JOIN cliente c ON c.id = p.cliente_id
            JOIN usuario u     ON u.id = f.responsable_id
            LEFT JOIN ticket b ON b.id = f.bug_reversion_id
            WHERE f.id = :faseId AND f.proyecto_id = :proyectoId
            """, nativeQuery = true)
    Optional<FaseFila> fase(@Param("proyectoId") Long proyectoId, @Param("faseId") Long faseId);

    /** La otra fase que ocupa el turno, si la hay: "Fase 4 · Portal móvil". */
    @Query(value = """
            SELECT CONCAT('Fase ', f.numero, ' · ', f.nombre)
            FROM fase f
            WHERE f.proyecto_id = :proyectoId AND f.id <> :faseId AND f.es_mantenimiento = FALSE
              AND f.estado IN ('EN_CURSO', 'PAUSADA', 'LISTA_PARA_RELEASE', 'EN_PUBLICACION')
            ORDER BY f.numero
            LIMIT 1
            """, nativeQuery = true)
    Optional<String> otraFaseActiva(@Param("proyectoId") Long proyectoId, @Param("faseId") Long faseId);

    /** Releases de fase publicados, del más reciente al más antiguo. */
    @Query(value = """
            SELECT r.fase_id AS faseId, r.version AS version
            FROM version_release r
            WHERE r.proyecto_id = :proyectoId AND r.tipo = 'FASE' AND r.estado = 'PUBLICADA'
            ORDER BY r.publicado_en DESC, r.id DESC
            """, nativeQuery = true)
    List<ReleaseFila> publicados(@Param("proyectoId") Long proyectoId);

    @Query(value = """
            SELECT c.id AS id, c.descripcion AS descripcion, c.codigo AS codigo, c.cumplido AS cumplido,
                   u.nombre AS cumplidoPor, c.cumplido_en AS cumplidoEn
            FROM fase_criterio_cierre c
            LEFT JOIN usuario u ON u.id = c.cumplido_por
            WHERE c.fase_id = :faseId
            ORDER BY c.tipo = 'MANUAL', c.id
            """, nativeQuery = true)
    List<CriterioFila> criterios(@Param("faseId") Long faseId);

    /** Para los criterios automáticos, para saber si aún se edita y si ya se inicia. Las descartadas no cuentan. */
    @Query(value = """
            SELECT (SELECT COUNT(*) FROM historia h WHERE h.fase_id = :faseId AND h.estado <> 'DESCARTADA') AS historias,
                   (SELECT COUNT(*) FROM historia h WHERE h.fase_id = :faseId AND h.estado = 'LISTA_DESARROLLO') AS historiasListas,
                   (SELECT COUNT(*) FROM historia h WHERE h.fase_id = :faseId
                       AND h.estado IN ('ACEPTADA', 'INTEGRADA', 'CERRADA')) AS historiasAceptadas,
                   (SELECT COUNT(*) FROM ticket t WHERE t.fase_id = :faseId AND t.tipo = 'BUGS'
                       AND (t.prioridad = 'ALTA' OR t.severidad IN ('CRITICA', 'ALTA'))
                       AND t.estado IN ('REPORTADO', 'CONFIRMADO', 'EN_CORRECCION', 'EN_QA')) AS bugsAltaAbiertos,
                   (SELECT COUNT(*) FROM ticket t WHERE t.fase_id = :faseId
                       AND t.estado NOT IN ('CANCELADA', 'NO_REPRODUCIBLE', 'RECHAZADA')) AS tickets
            """, nativeQuery = true)
    ConteosFila conteos(@Param("faseId") Long faseId);

    @Query(value = """
            SELECT m.tipo AS tipo, m.nombre AS nombre
            FROM fase_modulo fm JOIN modulo m ON m.id = fm.modulo_id
            WHERE fm.fase_id = :faseId
            ORDER BY m.id
            """, nativeQuery = true)
    List<ModuloFila> modulos(@Param("faseId") Long faseId);

    @Query(value = """
            SELECT t.historia_id AS historiaId, t.tipo AS tipo, t.estado AS estado, COUNT(*) AS cantidad
            FROM ticket t
            WHERE t.fase_id = :faseId
            GROUP BY t.historia_id, t.tipo, t.estado
            """, nativeQuery = true)
    List<TicketsFila> tickets(@Param("faseId") Long faseId);

    @Query(value = """
            SELECT h.id AS id, h.numero AS numero, h.titulo AS titulo, h.estado AS estado,
                   u.nombre AS analistaNombre, u.apellidos AS analistaApellidos,
                   (h.como IS NULL OR h.quiero IS NULL OR h.para IS NULL
                       OR NOT EXISTS (SELECT 1 FROM historia_criterio c WHERE c.historia_id = h.id)
                       OR NOT EXISTS (SELECT 1 FROM historia_modulo hm WHERE hm.historia_id = h.id)) AS incompleta,
                   h.figma_url IS NOT NULL AS disenoPropio,
                   NOT EXISTS (SELECT 1 FROM ticket t
                               WHERE t.historia_id = h.id OR t.historia_origen_id = h.id) AS sinTickets
            FROM historia h
            LEFT JOIN usuario u ON u.id = h.analista_id
            WHERE h.fase_id = :faseId
            ORDER BY h.numero
            """, nativeQuery = true)
    List<HistoriaFila> historias(@Param("faseId") Long faseId);

    /** Lo último que pasó en la fase, sus historias y sus tickets. */
    @Query(value = """
            SELECT u.nombre AS usuario, h.entidad AS entidad,
                   CASE h.entidad WHEN 'TICKET' THEN t.tipo WHEN 'HISTORIA' THEN 'HIST' ELSE 'FASE' END AS tipo,
                   COALESCE(t.numero, hi.numero, f.numero) AS numero,
                   COALESCE(t.titulo, hi.titulo, f.nombre) AS titulo,
                   h.estado_anterior AS estadoAnterior, h.estado_nuevo AS estadoNuevo,
                   h.automatico AS automatico, h.creado_en AS fecha
            FROM historial_estado h
            LEFT JOIN ticket t    ON h.entidad = 'TICKET'   AND t.id = h.entidad_id
            LEFT JOIN historia hi ON h.entidad = 'HISTORIA' AND hi.id = h.entidad_id
            LEFT JOIN fase f      ON h.entidad = 'FASE'     AND f.id = h.entidad_id
            LEFT JOIN usuario u   ON u.id = h.usuario_id
            WHERE COALESCE(t.fase_id, hi.fase_id, f.id) = :faseId
            ORDER BY h.creado_en DESC, h.id DESC
            LIMIT :limite
            """, nativeQuery = true)
    List<MovimientoFila> actividad(@Param("faseId") Long faseId, @Param("limite") int limite);

    /** Cuándo y quién la dejó en su estado actual. */
    @Query(value = """
            SELECT h.creado_en AS fecha, u.nombre AS usuario
            FROM historial_estado h
            LEFT JOIN usuario u ON u.id = h.usuario_id
            WHERE h.entidad = 'FASE' AND h.entidad_id = :faseId AND h.estado_nuevo = :estado
            ORDER BY h.creado_en DESC, h.id DESC
            LIMIT 1
            """, nativeQuery = true)
    Optional<CambioFila> ultimoCambio(@Param("faseId") Long faseId, @Param("estado") String estado);

    @Query(value = "SELECT COUNT(*) FROM historia h WHERE h.fase_id = :faseId AND h.estado IN (:estados)",
            nativeQuery = true)
    long contarHistorias(@Param("faseId") Long faseId, @Param("estados") Collection<String> estados);

    @Query(value = "SELECT COUNT(*) FROM ticket t WHERE t.fase_id = :faseId AND t.estado NOT IN (:estados)",
            nativeQuery = true)
    long contarTicketsFuera(@Param("faseId") Long faseId, @Param("estados") Collection<String> estados);

    @Query(value = """
            SELECT COALESCE(MAX(t.numero), 0) + 1 FROM ticket t WHERE t.proyecto_id = :proyectoId AND t.tipo = 'BUGS'
            """, nativeQuery = true)
    int siguienteNumeroBug(@Param("proyectoId") Long proyectoId);

    @Query(value = "SELECT MIN(fm.modulo_id) FROM fase_modulo fm WHERE fm.fase_id = :faseId", nativeQuery = true)
    Long primerModulo(@Param("faseId") Long faseId);

    // ─── Release (PDF pág. 27) ──────────────────────────────────────────────

    @Query(value = """
            SELECT r.estado AS estado, r.notas AS notas, r.enviar_notas AS enviarNotas, r.publicado_en AS publicadoEn,
                   CONCAT(u.nombre, COALESCE(CONCAT(' ', SUBSTRING_INDEX(u.apellidos, ' ', 1)), '')) AS publicadoPor
            FROM version_release r
            LEFT JOIN usuario u ON u.id = r.publicado_por
            WHERE r.proyecto_id = :proyectoId AND r.version = :version
            """, nativeQuery = true)
    Optional<DatosReleaseFila> release(@Param("proyectoId") Long proyectoId, @Param("version") String version);

    /** La versión publicada más reciente: la que está hoy en producción. */
    @Query(value = """
            SELECT r.version AS version, r.publicado_en AS fecha
            FROM version_release r
            WHERE r.proyecto_id = :proyectoId AND r.estado = 'PUBLICADA'
            ORDER BY r.publicado_en DESC, r.id DESC
            LIMIT 1
            """, nativeQuery = true)
    Optional<VersionFila> enProduccion(@Param("proyectoId") Long proyectoId);

    @Query(value = """
            SELECT CONCAT(u.nombre, COALESCE(CONCAT(' ', SUBSTRING_INDEX(u.apellidos, ' ', 1)), ''))
            FROM proyecto p JOIN usuario u ON u.id = p.lider_id
            WHERE p.id = :proyectoId
            """, nativeQuery = true)
    String liderNombre(@Param("proyectoId") Long proyectoId);

    @Query(value = """
            SELECT m.tipo AS tipo, m.nombre AS nombre, m.tecnologia AS tecnologia
            FROM fase_modulo fm JOIN modulo m ON m.id = fm.modulo_id
            WHERE fm.fase_id = :faseId
            ORDER BY m.id
            """, nativeQuery = true)
    List<ModuloReleaseFila> modulosDelRelease(@Param("faseId") Long faseId);

    /** Actividades, mejoras y bugs de la fase que siguen en pie: lo que se cuenta en las notas. */
    @Query(value = """
            SELECT t.tipo AS tipo, CONCAT(t.tipo, '-', LPAD(t.numero, 2, '0')) AS codigo, t.titulo AS titulo
            FROM ticket t
            WHERE t.fase_id = :faseId AND t.tipo IN ('ACTV', 'MEJR', 'BUGS')
              AND t.estado NOT IN ('CANCELADA', 'NO_REPRODUCIBLE', 'RECHAZADA')
            ORDER BY t.tipo, t.numero
            """, nativeQuery = true)
    List<TicketDeNotasFila> cambios(@Param("faseId") Long faseId);

    @Query(value = """
            SELECT d.estado AS estado,
                   CONCAT(u.nombre, COALESCE(CONCAT(' ', SUBSTRING_INDEX(u.apellidos, ' ', 1)), '')) AS revisadaPor,
                   d.revisada_en AS revisadaEn
            FROM demo d
            LEFT JOIN usuario u ON u.id = d.revisada_por
            WHERE d.fase_id = :faseId
            ORDER BY d.id DESC
            LIMIT 1
            """, nativeQuery = true)
    Optional<DemoFila> demo(@Param("faseId") Long faseId);

    @Modifying
    @Query(value = """
            INSERT INTO version_release (proyecto_id, fase_id, version, tipo, estado, resumen, notas, enviar_notas)
            VALUES (:proyectoId, :faseId, :version, 'FASE', 'PREPARANDO', :resumen, :notas, :enviar)
            ON DUPLICATE KEY UPDATE notas = :notas, enviar_notas = :enviar
            """, nativeQuery = true)
    void guardarNotas(@Param("proyectoId") Long proyectoId, @Param("faseId") Long faseId,
                      @Param("version") String version, @Param("resumen") String resumen,
                      @Param("notas") String notas, @Param("enviar") boolean enviar);

    // ─── Escrituras ─────────────────────────────────────────────────────────

    /** Solo si sigue en el estado que se validó: si alguien más la movió a la vez, no se pisa. */
    @Modifying
    @Query(value = """
            UPDATE fase SET estado = :nuevo, motivo_estado = :motivo, reanudar_el = :reanudarEl
            WHERE id = :faseId AND estado = :anterior
            """, nativeQuery = true)
    int cambiarEstado(@Param("faseId") Long faseId, @Param("anterior") String anterior, @Param("nuevo") String nuevo,
                      @Param("motivo") String motivo, @Param("reanudarEl") LocalDate reanudarEl);

    @Modifying
    @Query(value = """
            INSERT INTO historial_estado (entidad, entidad_id, estado_anterior, estado_nuevo, motivo, automatico, usuario_id, creado_en)
            VALUES (:entidad, :id, :anterior, :nuevo, :motivo, :automatico, :usuarioId, :fecha)
            """, nativeQuery = true)
    void registrarHistorial(@Param("entidad") String entidad, @Param("id") Long id, @Param("anterior") String anterior,
                            @Param("nuevo") String nuevo, @Param("motivo") String motivo,
                            @Param("automatico") boolean automatico, @Param("usuarioId") Long usuarioId,
                            @Param("fecha") LocalDateTime fecha);

    /** Deja en el historial (como automático) las historias que se van a mover junto con la fase. */
    @Modifying
    @Query(value = """
            INSERT INTO historial_estado (entidad, entidad_id, estado_anterior, estado_nuevo, motivo, automatico, usuario_id, creado_en)
            SELECT 'HISTORIA', h.id, h.estado, :nuevo, :motivo, TRUE, :usuarioId, :fecha
            FROM historia h WHERE h.fase_id = :faseId AND h.estado IN (:estados)
            """, nativeQuery = true)
    void registrarHistorias(@Param("faseId") Long faseId, @Param("estados") Collection<String> estados,
                            @Param("nuevo") String nuevo, @Param("motivo") String motivo,
                            @Param("usuarioId") Long usuarioId, @Param("fecha") LocalDateTime fecha);

    /** Al iniciar o reanudar la fase: las historias Listas para desarrollo (ya tienen sus Actividades). */
    @Modifying
    @Query(value = """
            INSERT INTO historial_estado (entidad, entidad_id, estado_anterior, estado_nuevo, motivo, automatico, usuario_id, creado_en)
            SELECT 'HISTORIA', h.id, 'LISTA_DESARROLLO', 'EN_DESARROLLO', 'Se inició con la fase', TRUE, :usuarioId, :fecha
            FROM historia h WHERE h.fase_id = :faseId AND h.estado = 'LISTA_DESARROLLO'
            """, nativeQuery = true)
    void registrarHistoriasListas(@Param("faseId") Long faseId, @Param("usuarioId") Long usuarioId,
                                  @Param("fecha") LocalDateTime fecha);

    @Modifying
    @Query(value = """
            UPDATE historia h SET h.estado = 'EN_DESARROLLO'
            WHERE h.fase_id = :faseId AND h.estado = 'LISTA_DESARROLLO'
            """, nativeQuery = true)
    int iniciarHistoriasListas(@Param("faseId") Long faseId);

    @Modifying
    @Query(value = "UPDATE historia SET estado = :nuevo WHERE fase_id = :faseId AND estado IN (:estados)",
            nativeQuery = true)
    int moverHistorias(@Param("faseId") Long faseId, @Param("estados") Collection<String> estados,
                       @Param("nuevo") String nuevo);

    /** Igual que con las historias; {@code fuera = true} toma los tickets que NO están en {@code estados}. */
    @Modifying
    @Query(value = """
            INSERT INTO historial_estado (entidad, entidad_id, estado_anterior, estado_nuevo, motivo, automatico, usuario_id, creado_en)
            SELECT 'TICKET', t.id, t.estado, :nuevo, :motivo, TRUE, :usuarioId, :fecha
            FROM ticket t WHERE t.fase_id = :faseId AND (t.estado IN (:estados)) <> :fuera
            """, nativeQuery = true)
    void registrarTickets(@Param("faseId") Long faseId, @Param("estados") Collection<String> estados,
                          @Param("fuera") boolean fuera, @Param("nuevo") String nuevo, @Param("motivo") String motivo,
                          @Param("usuarioId") Long usuarioId, @Param("fecha") LocalDateTime fecha);

    @Modifying
    @Query(value = "UPDATE ticket SET estado = :nuevo WHERE fase_id = :faseId AND (estado IN (:estados)) <> :fuera",
            nativeQuery = true)
    int moverTickets(@Param("faseId") Long faseId, @Param("estados") Collection<String> estados,
                     @Param("fuera") boolean fuera, @Param("nuevo") String nuevo);

    /** El release de la fase; si ya existía (al publicar de nuevo), vuelve a prepararse. */
    @Modifying
    @Query(value = """
            INSERT INTO version_release (proyecto_id, fase_id, version, tipo, estado, resumen)
            VALUES (:proyectoId, :faseId, :version, 'FASE', 'PREPARANDO', :resumen)
            ON DUPLICATE KEY UPDATE estado = 'PREPARANDO', publicado_por = NULL, publicado_en = NULL
            """, nativeQuery = true)
    void prepararRelease(@Param("proyectoId") Long proyectoId, @Param("faseId") Long faseId,
                         @Param("version") String version, @Param("resumen") String resumen);

    @Modifying
    @Query(value = """
            UPDATE version_release
            SET estado = :estado,
                publicado_por = CASE WHEN :estado = 'PUBLICADA' THEN :usuarioId ELSE publicado_por END,
                publicado_en  = CASE WHEN :estado = 'PUBLICADA' THEN :fecha ELSE publicado_en END
            WHERE proyecto_id = :proyectoId AND version = :version
            """, nativeQuery = true)
    void cambiarRelease(@Param("proyectoId") Long proyectoId, @Param("version") String version,
                        @Param("estado") String estado, @Param("usuarioId") Long usuarioId,
                        @Param("fecha") LocalDateTime fecha);

    @Modifying
    @Query(value = "UPDATE fase SET bug_reversion_id = :bugId WHERE id = :faseId", nativeQuery = true)
    void guardarBugReversion(@Param("faseId") Long faseId, @Param("bugId") Long bugId);

    @Modifying
    @Query(value = """
            UPDATE fase_criterio_cierre SET cumplido = TRUE, cumplido_por = :usuarioId, cumplido_en = :fecha
            WHERE id = :criterioId AND fase_id = :faseId AND cumplido = FALSE
            """, nativeQuery = true)
    int marcarCriterio(@Param("faseId") Long faseId, @Param("criterioId") Long criterioId,
                       @Param("usuarioId") Long usuarioId, @Param("fecha") LocalDateTime fecha);

    // ─── Filas ──────────────────────────────────────────────────────────────

    interface FaseFila {
        Long getId();
        Long getProyectoId();
        String getProyectoNombre();
        String getProyectoCodigo();
        String getClienteNombre();
        Integer getNumero();
        String getNombre();
        String getObjetivo();
        String getEstado();
        String getPrioridad();
        LocalDate getFechaInicio();
        LocalDate getFechaFin();
        Long getLiderId();
        Long getResponsableId();
        String getResponsableNombre();
        String getVersionRelease();
        Boolean getMantenimiento();
        String getMotivoEstado();
        LocalDate getReanudarEl();
        String getFigmaUrl();
        Boolean getFigmaParaTodas();
        Long getBugId();
        String getBugTipo();
        Integer getBugNumero();
        String getBugEstado();
    }

    interface ReleaseFila {
        Long getFaseId();
        String getVersion();
    }

    interface CriterioFila {
        Long getId();
        String getDescripcion();
        String getCodigo();
        Boolean getCumplido();
        String getCumplidoPor();
        LocalDateTime getCumplidoEn();
    }

    /** Lo que impide eliminar la fase: historias ya iniciadas, tickets o algo que ya salió de ella. */
    @Query(value = """
            SELECT (SELECT COUNT(*) FROM historia h WHERE h.fase_id = :faseId
                       AND h.estado NOT IN ('BORRADOR', 'EN_PLANEACION', 'LISTA', 'DESCARTADA')) AS historiasIniciadas,
                   (SELECT COUNT(*) FROM ticket t WHERE t.fase_id = :faseId
                       OR t.historia_id IN (SELECT h.id FROM historia h WHERE h.fase_id = :faseId)
                       OR t.historia_origen_id IN (SELECT h.id FROM historia h WHERE h.fase_id = :faseId)) AS tickets,
                   (SELECT COUNT(*) FROM solicitud s WHERE s.fase_id = :faseId)
                       + (SELECT COUNT(*) FROM demo d WHERE d.fase_id = :faseId)
                       + (SELECT COUNT(*) FROM version_release r WHERE r.fase_id = :faseId) AS otros
            """, nativeQuery = true)
    BloqueosFila bloqueosParaEliminar(@Param("faseId") Long faseId);

    @Query(value = """
            SELECT a.ruta FROM adjunto a
            WHERE a.entidad = 'HISTORIA' AND a.entidad_id IN (SELECT h.id FROM historia h WHERE h.fase_id = :faseId)
            """, nativeQuery = true)
    List<String> rutasDeImagenes(@Param("faseId") Long faseId);

    // Eliminar la fase: primero lo de sus historias, luego las historias y al final la fase.
    @Modifying
    @Query(value = """
            DELETE FROM adjunto
            WHERE entidad = 'HISTORIA' AND entidad_id IN (SELECT h.id FROM historia h WHERE h.fase_id = :faseId)
            """, nativeQuery = true)
    void quitarImagenesDeHistorias(@Param("faseId") Long faseId);

    @Modifying
    @Query(value = """
            DELETE FROM comentario
            WHERE entidad = 'HISTORIA' AND entidad_id IN (SELECT h.id FROM historia h WHERE h.fase_id = :faseId)
            """, nativeQuery = true)
    void quitarComentariosDeHistorias(@Param("faseId") Long faseId);

    @Modifying
    @Query(value = """
            DELETE FROM historial_estado
            WHERE (entidad = 'HISTORIA' AND entidad_id IN (SELECT h.id FROM historia h WHERE h.fase_id = :faseId))
               OR (entidad = 'FASE' AND entidad_id = :faseId)
            """, nativeQuery = true)
    void quitarHistorial(@Param("faseId") Long faseId);

    @Modifying
    @Query(value = """
            DELETE FROM historia_criterio
            WHERE historia_id IN (SELECT h.id FROM historia h WHERE h.fase_id = :faseId)
            """, nativeQuery = true)
    void quitarCriteriosDeHistorias(@Param("faseId") Long faseId);

    @Modifying
    @Query(value = """
            DELETE FROM historia_modulo
            WHERE historia_id IN (SELECT h.id FROM historia h WHERE h.fase_id = :faseId)
            """, nativeQuery = true)
    void quitarModulosDeHistorias(@Param("faseId") Long faseId);

    @Modifying
    @Query(value = "DELETE FROM historia WHERE fase_id = :faseId", nativeQuery = true)
    void quitarHistorias(@Param("faseId") Long faseId);

    @Modifying
    @Query(value = "DELETE FROM fase_criterio_cierre WHERE fase_id = :faseId", nativeQuery = true)
    void quitarCriteriosDeCierre(@Param("faseId") Long faseId);

    @Modifying
    @Query(value = "DELETE FROM fase_modulo WHERE fase_id = :faseId", nativeQuery = true)
    void quitarModulos(@Param("faseId") Long faseId);

    @Modifying
    @Query(value = "DELETE FROM fase WHERE id = :faseId", nativeQuery = true)
    void eliminarFase(@Param("faseId") Long faseId);

    interface BloqueosFila {
        Long getHistoriasIniciadas();
        Long getTickets();
        Long getOtros();
    }

    interface ConteosFila {
        Long getHistorias();
        Long getHistoriasListas();
        Long getHistoriasAceptadas();
        Long getBugsAltaAbiertos();
        Long getTickets();
    }

    interface ModuloFila {
        String getTipo();
        String getNombre();
    }

    interface TicketsFila {
        Long getHistoriaId();
        String getTipo();
        String getEstado();
        Long getCantidad();
    }

    interface HistoriaFila {
        Long getId();
        Integer getNumero();
        String getTitulo();
        String getEstado();
        String getAnalistaNombre();
        String getAnalistaApellidos();
        Number getIncompleta();
        Number getDisenoPropio();
        Number getSinTickets();
    }

    interface MovimientoFila {
        String getUsuario();
        String getEntidad();
        String getTipo();
        Integer getNumero();
        String getTitulo();
        String getEstadoAnterior();
        String getEstadoNuevo();
        Boolean getAutomatico();
        LocalDateTime getFecha();
    }

    interface CambioFila {
        LocalDateTime getFecha();
        String getUsuario();
    }

    interface DatosReleaseFila {
        String getEstado();
        String getNotas();
        Boolean getEnviarNotas();
        LocalDateTime getPublicadoEn();
        String getPublicadoPor();
    }

    interface VersionFila {
        String getVersion();
        LocalDateTime getFecha();
    }

    interface TicketDeNotasFila {
        String getTipo();
        String getCodigo();
        String getTitulo();
    }

    interface ModuloReleaseFila {
        String getTipo();
        String getNombre();
        String getTecnologia();
    }

    interface DemoFila {
        String getEstado();
        String getRevisadaPor();
        LocalDateTime getRevisadaEn();
    }
}
