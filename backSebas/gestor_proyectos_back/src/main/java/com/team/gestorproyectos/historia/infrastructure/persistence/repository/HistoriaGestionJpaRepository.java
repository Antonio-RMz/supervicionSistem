package com.team.gestorproyectos.historia.infrastructure.persistence.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import com.team.gestorproyectos.proyecto.infrastructure.persistence.entity.HistoriaEntity;

/** Detalle de la historia y sus cambios de estado con lo que arrastran (PDF pág. 21, 24 y 33). */
public interface HistoriaGestionJpaRepository extends Repository<HistoriaEntity, Long> {

    // ─── Lecturas ───────────────────────────────────────────────────────────

    @Query(value = """
            SELECT h.id FROM historia h
            WHERE h.proyecto_id = :proyectoId AND h.estado IN ('BORRADOR', 'EN_PLANEACION', 'LISTA', 'EN_VALIDACION', 'ACEPTADA',
                                                           'LISTA_DESARROLLO', 'EN_DESARROLLO', 'EN_REVISION')
            ORDER BY h.numero
            """, nativeQuery = true)
    List<Long> historiasPorDecidir(@Param("proyectoId") Long proyectoId);

    @Query(value = """
            SELECT h.id AS id, h.proyecto_id AS proyectoId, p.nombre AS proyectoNombre,
                   p.forma_trabajo AS formaTrabajo, p.requiere_pr AS requierePr, h.numero AS numero,
                   h.titulo AS titulo, h.como AS como, h.quiero AS quiero, h.para AS para, h.estado AS estado,
                   h.prioridad AS prioridad, h.puntos AS puntos, h.analista_id AS analistaId,
                   CONCAT(a.nombre, COALESCE(CONCAT(' ', a.apellidos), '')) AS analistaNombre,
                   p.lider_id AS liderId, f.id AS faseId, f.numero AS faseNumero, f.nombre AS faseNombre,
                   f.estado AS faseEstado, f.fecha_inicio AS faseInicio, f.fecha_fin AS faseFin,
                   COALESCE(h.figma_url, IF(f.figma_para_todas, f.figma_url, NULL)) AS figmaUrl,
                   (h.figma_url IS NULL AND f.figma_para_todas AND f.figma_url IS NOT NULL) AS disenoDeFase,
                   h.diseno_aprobado AS disenoAprobado,
                   CONCAT(d.nombre, COALESCE(CONCAT(' ', d.apellidos), '')) AS disenoAprobadoPor,
                   h.diseno_aprobado_en AS disenoAprobadoEn,
                   (SELECT COUNT(*) FROM historia_criterio c WHERE c.historia_id = h.id) AS criterios,
                   (SELECT COUNT(*) FROM historia_modulo hm WHERE hm.historia_id = h.id) AS modulos
            FROM historia h
            JOIN proyecto p                ON p.id = h.proyecto_id
            JOIN fase f                    ON f.id = h.fase_id
            JOIN usuario a                 ON a.id = h.analista_id
            LEFT JOIN usuario d            ON d.id = h.diseno_aprobado_por
            WHERE h.id = :historiaId AND h.proyecto_id = :proyectoId
            """, nativeQuery = true)
    Optional<HistoriaFila> historia(@Param("proyectoId") Long proyectoId, @Param("historiaId") Long historiaId);

    @Query(value = """
            SELECT t.id AS id, t.tipo AS tipo, t.numero AS numero, t.titulo AS titulo, t.estado AS estado,
                   t.responsable_id AS responsableId, t.tester_id AS testerId, m.tipo AS moduloTipo,
                   CONCAT(u.nombre, COALESCE(CONCAT(' ', u.apellidos), '')) AS responsableNombre,
                   (SELECT GROUP_CONCAT(CONCAT(d.tipo, '-', LPAD(d.numero, 2, '0')) ORDER BY d.tipo, d.numero)
                    FROM ticket_dependencia td JOIN ticket d ON d.id = td.depende_de_id
                    WHERE td.ticket_id = t.id) AS dependeDe
            FROM ticket t
            LEFT JOIN modulo m  ON m.id = t.modulo_id
            LEFT JOIN usuario u ON u.id = t.responsable_id
            WHERE t.historia_id = :historiaId
            ORDER BY t.tipo = 'ACTV', t.tipo, t.numero
            """, nativeQuery = true)
    List<TicketFila> tickets(@Param("historiaId") Long historiaId);

    /**
     * Otras fases que reciben historias y trabajan todos los módulos de esta historia
     * (sus tickets solo se asignan a módulos de su fase).
     */
    @Query(value = """
            SELECT f.id AS id, CONCAT('Fase ', f.numero, ' · ', f.nombre) AS titulo
            FROM fase f
            WHERE f.proyecto_id = :proyectoId AND f.id <> :faseId AND f.es_mantenimiento = FALSE
              AND f.estado IN ('PLANEADA', 'EN_CURSO', 'PAUSADA')
              AND NOT EXISTS (SELECT 1 FROM historia_modulo hm
                              WHERE hm.historia_id = :historiaId
                                AND hm.modulo_id NOT IN (SELECT fm.modulo_id FROM fase_modulo fm WHERE fm.fase_id = f.id))
            ORDER BY f.numero
            """, nativeQuery = true)
    List<FaseFila> fasesDestino(@Param("proyectoId") Long proyectoId, @Param("faseId") Long faseId,
                                @Param("historiaId") Long historiaId);

    @Query(value = """
            SELECT m.tipo FROM historia_modulo hm JOIN modulo m ON m.id = hm.modulo_id
            WHERE hm.historia_id = :historiaId ORDER BY m.id
            """, nativeQuery = true)
    List<String> modulos(@Param("historiaId") Long historiaId);

    @Query(value = """
            SELECT c.id AS id, c.descripcion AS descripcion, c.verificado AS verificado,
                   GROUP_CONCAT(CONCAT(t.tipo, '-', LPAD(t.numero, 2, '0')) ORDER BY t.tipo, t.numero) AS tickets
            FROM historia_criterio c
            LEFT JOIN ticket_criterio tc ON tc.criterio_id = c.id
            LEFT JOIN ticket t           ON t.id = tc.ticket_id
            WHERE c.historia_id = :historiaId
            GROUP BY c.id, c.descripcion, c.verificado, c.orden
            ORDER BY c.orden, c.id
            """, nativeQuery = true)
    List<CriterioFila> criterios(@Param("historiaId") Long historiaId);

    /** Del más reciente al más antiguo; "Ana López" (nombre y primer apellido). */
    @Query(value = """
            SELECT h.estado_nuevo AS estado, h.motivo AS motivo, h.automatico AS automatico,
                   CONCAT(u.nombre, COALESCE(CONCAT(' ', SUBSTRING_INDEX(u.apellidos, ' ', 1)), '')) AS usuario,
                   h.creado_en AS fecha
            FROM historial_estado h
            LEFT JOIN usuario u ON u.id = h.usuario_id
            WHERE h.entidad = 'HISTORIA' AND h.entidad_id = :historiaId
            ORDER BY h.creado_en DESC, h.id DESC
            """, nativeQuery = true)
    List<CambioFila> historial(@Param("historiaId") Long historiaId);

    @Query(value = """
            SELECT a.id AS id, a.nombre AS nombre, a.tipo_mime AS tipoMime, a.tamano_bytes AS tamanoBytes, a.ruta AS ruta
            FROM adjunto a WHERE a.entidad = 'HISTORIA' AND a.entidad_id = :historiaId ORDER BY a.id
            """, nativeQuery = true)
    List<AdjuntoFila> adjuntos(@Param("historiaId") Long historiaId);

    @Query(value = """
            SELECT CONCAT(u.nombre, COALESCE(CONCAT(' ', SUBSTRING_INDEX(u.apellidos, ' ', 1)), '')) AS usuario,
                   c.texto AS texto, c.creado_en AS fecha
            FROM comentario c JOIN usuario u ON u.id = c.usuario_id
            WHERE c.entidad = 'HISTORIA' AND c.entidad_id = :historiaId
            ORDER BY c.creado_en, c.id
            """, nativeQuery = true)
    List<ComentarioFila> comentarios(@Param("historiaId") Long historiaId);

    @Query(value = "SELECT c.id FROM historia_criterio c WHERE c.historia_id = :historiaId", nativeQuery = true)
    List<Long> idsDeCriterios(@Param("historiaId") Long historiaId);

    @Query(value = """
            SELECT COALESCE(MAX(t.numero), 0) + 1 FROM ticket t WHERE t.proyecto_id = :proyectoId AND t.tipo = 'BUGS'
            """, nativeQuery = true)
    int siguienteNumeroBug(@Param("proyectoId") Long proyectoId);

    @Query(value = "SELECT MIN(hm.modulo_id) FROM historia_modulo hm WHERE hm.historia_id = :historiaId",
            nativeQuery = true)
    Long primerModulo(@Param("historiaId") Long historiaId);

    // ─── Integrar historia (PDF pág. 28) ────────────────────────────────────

    @Query(value = "SELECT f.version_release FROM fase f WHERE f.id = :faseId", nativeQuery = true)
    String versionDeFase(@Param("faseId") Long faseId);

    /** Quién la aceptó la última vez y cuándo. */
    @Query(value = """
            SELECT CONCAT(u.nombre, COALESCE(CONCAT(' ', SUBSTRING_INDEX(u.apellidos, ' ', 1)), '')) AS usuario,
                   h.creado_en AS fecha
            FROM historial_estado h
            LEFT JOIN usuario u ON u.id = h.usuario_id
            WHERE h.entidad = 'HISTORIA' AND h.entidad_id = :historiaId AND h.estado_nuevo = 'ACEPTADA'
            ORDER BY h.creado_en DESC, h.id DESC
            LIMIT 1
            """, nativeQuery = true)
    Optional<AceptacionFila> aceptacion(@Param("historiaId") Long historiaId);

    /** Cada ticket con su último PR. */
    @Query(value = """
            SELECT t.id AS id, t.tipo AS tipo, t.numero AS numero, t.titulo AS titulo, t.estado AS estado,
                   t.modulo_id AS moduloId, COALESCE(pr.rama_origen, t.rama) AS rama, pr.numero AS prNumero,
                   pr.estado AS prEstado, pr.ci_estado AS prCi, pr.con_conflictos AS prConConflictos,
                   pr.actualizado_en AS prRevisadoEn
            FROM ticket t
            LEFT JOIN pull_request pr ON pr.id = (SELECT MAX(p2.id) FROM pull_request p2 WHERE p2.ticket_id = t.id)
            WHERE t.historia_id = :historiaId
            ORDER BY t.tipo = 'ACTV', t.tipo, t.numero
            """, nativeQuery = true)
    List<TicketPrFila> ticketsConPr(@Param("historiaId") Long historiaId);

    /**
     * Los módulos de sus tickets, con cuántas historias de la fase ya metieron código de ese módulo a develop
     * (de ahí sale el número de la versión candidata).
     */
    @Query(value = """
            SELECT m.id AS id, m.tipo AS tipo, m.nombre AS nombre, m.tecnologia AS tecnologia,
                   (SELECT COUNT(DISTINCT h2.id) FROM historia h2 JOIN ticket t2 ON t2.historia_id = h2.id
                    WHERE h2.fase_id = :faseId AND h2.estado IN ('INTEGRADA', 'CERRADA') AND t2.modulo_id = m.id
                      AND h2.id <> :historiaId) AS integradas
            FROM modulo m
            WHERE m.id IN (SELECT t.modulo_id FROM ticket t WHERE t.historia_id = :historiaId)
            ORDER BY m.id
            """, nativeQuery = true)
    List<ModuloFila> modulosDeTickets(@Param("historiaId") Long historiaId, @Param("faseId") Long faseId);

    @Query(value = """
            SELECT h.id AS id, h.numero AS numero, h.estado AS estado FROM historia h
            WHERE h.fase_id = :faseId AND h.estado <> 'DESCARTADA'
            ORDER BY h.numero
            """, nativeQuery = true)
    List<HistoriaDeFaseFila> historiasDeFase(@Param("faseId") Long faseId);

    @Query(value = """
            SELECT MAX(he.creado_en) FROM historial_estado he JOIN historia h ON h.id = he.entidad_id
            WHERE he.entidad = 'HISTORIA' AND he.estado_nuevo = 'INTEGRADA' AND h.fase_id = :faseId
            """, nativeQuery = true)
    LocalDateTime ultimaIntegracion(@Param("faseId") Long faseId);

    @Query(value = """
            SELECT r.version AS version, r.publicado_en AS fecha FROM version_release r
            WHERE r.proyecto_id = :proyectoId AND r.estado = 'PUBLICADA'
            ORDER BY r.publicado_en DESC, r.id DESC
            LIMIT 1
            """, nativeQuery = true)
    Optional<PublicadaFila> enProduccion(@Param("proyectoId") Long proyectoId);

    // ─── Escrituras ─────────────────────────────────────────────────────────

    /** Solo si sigue en el estado que se validó: si alguien más la movió a la vez, no se pisa. */
    @Modifying
    @Query(value = "UPDATE historia SET estado = :nuevo WHERE id = :historiaId AND estado = :anterior",
            nativeQuery = true)
    int cambiarEstado(@Param("historiaId") Long historiaId, @Param("anterior") String anterior,
                      @Param("nuevo") String nuevo);

    @Modifying
    @Query(value = """
            INSERT INTO historial_estado (entidad, entidad_id, estado_anterior, estado_nuevo, motivo, automatico, usuario_id, creado_en)
            VALUES (:entidad, :id, :anterior, :nuevo, :motivo, :automatico, :usuarioId, :fecha)
            """, nativeQuery = true)
    void registrarHistorial(@Param("entidad") String entidad, @Param("id") Long id, @Param("anterior") String anterior,
                            @Param("nuevo") String nuevo, @Param("motivo") String motivo,
                            @Param("automatico") boolean automatico, @Param("usuarioId") Long usuarioId,
                            @Param("fecha") LocalDateTime fecha);

    /** Deja en el historial (como automáticos) los tickets de la historia que van de {@code desde} a {@code hacia}. */
    @Modifying
    @Query(value = """
            INSERT INTO historial_estado (entidad, entidad_id, estado_anterior, estado_nuevo, motivo, automatico, usuario_id, creado_en)
            SELECT 'TICKET', t.id, t.estado, :hacia, :motivo, TRUE, :usuarioId, :fecha
            FROM ticket t
            WHERE t.historia_id = :historiaId AND t.estado = :desde AND NOT (t.tipo = 'MEJR' AND t.estado = 'APROBADA')
            """, nativeQuery = true)
    void registrarTickets(@Param("historiaId") Long historiaId, @Param("desde") String desde,
                          @Param("hacia") String hacia, @Param("motivo") String motivo,
                          @Param("usuarioId") Long usuarioId, @Param("fecha") LocalDateTime fecha);

    @Modifying
    @Query(value = """
            UPDATE ticket t SET t.estado = :hacia
            WHERE t.historia_id = :historiaId AND t.estado = :desde AND NOT (t.tipo = 'MEJR' AND t.estado = 'APROBADA')
            """, nativeQuery = true)
    void moverTickets(@Param("historiaId") Long historiaId, @Param("desde") String desde, @Param("hacia") String hacia);

    /** Los tickets que nadie ha empezado (incluye una mejora "Aprobada", que aún no se trabaja). */
    @Modifying
    @Query(value = """
            INSERT INTO historial_estado (entidad, entidad_id, estado_anterior, estado_nuevo, motivo, automatico, usuario_id, creado_en)
            SELECT 'TICKET', t.id, t.estado, 'CANCELADA', :motivo, TRUE, :usuarioId, :fecha
            FROM ticket t
            WHERE t.historia_id = :historiaId
              AND (t.estado IN ('BORRADOR', 'POR_HACER', 'REPORTADO', 'CONFIRMADO', 'PROPUESTA') OR (t.tipo = 'MEJR' AND t.estado = 'APROBADA'))
            """, nativeQuery = true)
    void registrarCancelacion(@Param("historiaId") Long historiaId, @Param("motivo") String motivo,
                              @Param("usuarioId") Long usuarioId, @Param("fecha") LocalDateTime fecha);

    @Modifying
    @Query(value = """
            UPDATE ticket t SET t.estado = 'CANCELADA'
            WHERE t.historia_id = :historiaId
              AND (t.estado IN ('BORRADOR', 'POR_HACER', 'REPORTADO', 'CONFIRMADO', 'PROPUESTA') OR (t.tipo = 'MEJR' AND t.estado = 'APROBADA'))
            """, nativeQuery = true)
    void cancelarSinEmpezar(@Param("historiaId") Long historiaId);

    @Modifying
    @Query(value = "UPDATE ticket SET estado = :nuevo WHERE id = :ticketId AND historia_id = :historiaId",
            nativeQuery = true)
    int reabrirTicket(@Param("historiaId") Long historiaId, @Param("ticketId") Long ticketId,
                      @Param("nuevo") String nuevo);

    @Query(value = "SELECT t.estado FROM ticket t WHERE t.id = :ticketId", nativeQuery = true)
    String estadoDeTicket(@Param("ticketId") Long ticketId);

    /** Al integrar la historia, sus PR aprobados se mezclan a develop. */
    @Modifying
    @Query(value = """
            UPDATE pull_request pr JOIN ticket t ON t.id = pr.ticket_id
            SET pr.estado = 'INTEGRADO'
            WHERE t.historia_id = :historiaId AND pr.estado = 'APROBADO'
            """, nativeQuery = true)
    void integrarPullRequests(@Param("historiaId") Long historiaId);

    @Modifying
    @Query(value = "UPDATE historia SET fase_id = :faseId WHERE id = :historiaId",
            nativeQuery = true)
    void cambiarFase(@Param("historiaId") Long historiaId, @Param("faseId") Long faseId);

    @Modifying
    @Query(value = "UPDATE ticket SET fase_id = :faseId WHERE historia_id = :historiaId", nativeQuery = true)
    void moverTicketsDeFase(@Param("historiaId") Long historiaId, @Param("faseId") Long faseId);

    @Modifying
    @Query(value = "UPDATE historia_criterio SET verificado = :verificado WHERE id = :criterioId", nativeQuery = true)
    void verificarCriterio(@Param("criterioId") Long criterioId, @Param("verificado") boolean verificado);

    @Modifying
    @Query(value = """
            INSERT INTO comentario (entidad, entidad_id, usuario_id, texto, creado_en)
            VALUES ('HISTORIA', :historiaId, :usuarioId, :texto, :fecha)
            """, nativeQuery = true)
    void comentar(@Param("historiaId") Long historiaId, @Param("usuarioId") Long usuarioId,
                  @Param("texto") String texto, @Param("fecha") LocalDateTime fecha);

    @Modifying
    @Query(value = """
            INSERT INTO adjunto (entidad, entidad_id, nombre, tipo_mime, tamano_bytes, ruta, subido_por)
            VALUES ('HISTORIA', :historiaId, :nombre, :tipoMime, :tamanoBytes, :ruta, :usuarioId)
            """, nativeQuery = true)
    void agregarAdjunto(@Param("historiaId") Long historiaId, @Param("nombre") String nombre,
                        @Param("tipoMime") String tipoMime, @Param("tamanoBytes") long tamanoBytes,
                        @Param("ruta") String ruta, @Param("usuarioId") Long usuarioId);

    @Query(value = "SELECT COUNT(*) FROM ticket t WHERE t.historia_origen_id = :historiaId", nativeQuery = true)
    long ticketsQueVienenDe(@Param("historiaId") Long historiaId);

    // Eliminar la historia: primero lo que cuelga de ella y al final la historia.
    @Modifying
    @Query(value = "DELETE FROM adjunto WHERE entidad = 'HISTORIA' AND entidad_id = :historiaId", nativeQuery = true)
    void quitarAdjuntos(@Param("historiaId") Long historiaId);

    @Modifying
    @Query(value = "DELETE FROM comentario WHERE entidad = 'HISTORIA' AND entidad_id = :historiaId", nativeQuery = true)
    void quitarComentarios(@Param("historiaId") Long historiaId);

    @Modifying
    @Query(value = "DELETE FROM historial_estado WHERE entidad = 'HISTORIA' AND entidad_id = :historiaId",
            nativeQuery = true)
    void quitarHistorial(@Param("historiaId") Long historiaId);

    @Modifying
    @Query(value = "DELETE FROM historia_criterio WHERE historia_id = :historiaId", nativeQuery = true)
    void quitarCriterios(@Param("historiaId") Long historiaId);

    @Modifying
    @Query(value = "DELETE FROM historia_modulo WHERE historia_id = :historiaId", nativeQuery = true)
    void quitarModulos(@Param("historiaId") Long historiaId);

    @Modifying
    @Query(value = "DELETE FROM historia WHERE id = :historiaId", nativeQuery = true)
    void eliminarHistoria(@Param("historiaId") Long historiaId);

    @Modifying
    @Query(value = "DELETE FROM adjunto WHERE id = :adjuntoId AND entidad = 'HISTORIA'", nativeQuery = true)
    void quitarAdjunto(@Param("adjuntoId") Long adjuntoId);

    // ─── Filas ──────────────────────────────────────────────────────────────

    // ─── Historia con Desarrolladores ───────────────────────────────────────

    @Query(value = """
            SELECT d.usuario_id AS usuarioId, TRIM(CONCAT(u.nombre, ' ', COALESCE(u.apellidos, ''))) AS nombre,
                   d.modulo_id AS moduloId, m.nombre AS moduloNombre, d.terminado AS terminado
            FROM historia_desarrollador d
            JOIN usuario u      ON u.id = d.usuario_id
            LEFT JOIN modulo m  ON m.id = d.modulo_id
            WHERE d.historia_id = :historiaId
            ORDER BY u.nombre
            """, nativeQuery = true)
    List<DesarrolladorFila> desarrolladores(@Param("historiaId") Long historiaId);

    @Query(value = """
            SELECT t.id AS id, t.descripcion AS descripcion, t.modulo_id AS moduloId, m.nombre AS moduloNombre,
                   t.hecho AS hecho, TRIM(CONCAT(u.nombre, ' ', COALESCE(u.apellidos, ''))) AS creadoPor,
                   t.creado_en AS creadoEn
            FROM historia_trabajo t
            JOIN usuario u     ON u.id = t.creado_por
            LEFT JOIN modulo m ON m.id = t.modulo_id
            WHERE t.historia_id = :historiaId
            ORDER BY t.orden, t.id
            """, nativeQuery = true)
    List<TrabajoFila> trabajo(@Param("historiaId") Long historiaId);

    @Query(value = """
            SELECT m.id AS id, m.tipo AS tipo, m.nombre AS nombre, m.tecnologia AS tecnologia, m.rama_base AS ramaBase,
                   m.repositorio AS url
            FROM historia_modulo hm JOIN modulo m ON m.id = hm.modulo_id
            WHERE hm.historia_id = :historiaId
            ORDER BY m.tipo, m.nombre
            """, nativeQuery = true)
    List<RepositorioFila> repositorios(@Param("historiaId") Long historiaId);

    @Modifying
    @Query(value = """
            UPDATE historia_desarrollador SET terminado = :terminado
            WHERE historia_id = :historiaId AND usuario_id = :usuarioId
            """, nativeQuery = true)
    int marcarParte(@Param("historiaId") Long historiaId, @Param("usuarioId") Long usuarioId,
                    @Param("terminado") boolean terminado);

    /** Al devolverla o pedir cambios: todos vuelven a trabajar en ella. */
    @Modifying
    @Query(value = "UPDATE historia_desarrollador SET terminado = FALSE WHERE historia_id = :historiaId",
            nativeQuery = true)
    void reiniciarPartes(@Param("historiaId") Long historiaId);

    @Modifying
    @Query(value = """
            INSERT INTO historia_trabajo (historia_id, descripcion, modulo_id, hecho, orden, creado_por, creado_en)
            SELECT :historiaId, :descripcion, :moduloId, FALSE,
                   COALESCE((SELECT MAX(t.orden) FROM historia_trabajo t WHERE t.historia_id = :historiaId), 0) + 1,
                   :usuarioId, :fecha
            """, nativeQuery = true)
    void agregarTrabajo(@Param("historiaId") Long historiaId, @Param("descripcion") String descripcion,
                        @Param("moduloId") Long moduloId, @Param("usuarioId") Long usuarioId,
                        @Param("fecha") LocalDateTime fecha);

    @Modifying
    @Query(value = "UPDATE historia_trabajo SET hecho = :hecho WHERE id = :trabajoId AND historia_id = :historiaId",
            nativeQuery = true)
    int marcarTrabajo(@Param("historiaId") Long historiaId, @Param("trabajoId") Long trabajoId,
                      @Param("hecho") boolean hecho);

    @Modifying
    @Query(value = "DELETE FROM historia_trabajo WHERE id = :trabajoId AND historia_id = :historiaId",
            nativeQuery = true)
    int quitarTrabajo(@Param("historiaId") Long historiaId, @Param("trabajoId") Long trabajoId);

    @Modifying
    @Query(value = "DELETE FROM historia_trabajo WHERE historia_id = :historiaId", nativeQuery = true)
    void quitarTodoElTrabajo(@Param("historiaId") Long historiaId);

    @Modifying
    @Query(value = "DELETE FROM historia_desarrollador WHERE historia_id = :historiaId", nativeQuery = true)
    void quitarDesarrolladores(@Param("historiaId") Long historiaId);

    interface DesarrolladorFila {
        Long getUsuarioId();
        String getNombre();
        Long getModuloId();
        String getModuloNombre();
        Boolean getTerminado();
    }

    interface TrabajoFila {
        Long getId();
        String getDescripcion();
        Long getModuloId();
        String getModuloNombre();
        Boolean getHecho();
        String getCreadoPor();
        LocalDateTime getCreadoEn();
    }

    interface RepositorioFila {
        Long getId();
        String getTipo();
        String getNombre();
        String getTecnologia();
        String getRamaBase();
        String getUrl();
    }

    interface HistoriaFila {
        Long getId();
        Long getProyectoId();
        String getProyectoNombre();
        String getFormaTrabajo();
        Boolean getRequierePr();
        Integer getNumero();
        String getTitulo();
        String getComo();
        String getQuiero();
        String getPara();
        String getEstado();
        String getPrioridad();
        Integer getPuntos();
        Long getAnalistaId();
        String getAnalistaNombre();
        Long getLiderId();
        Long getFaseId();
        Integer getFaseNumero();
        String getFaseNombre();
        String getFaseEstado();
        LocalDate getFaseInicio();
        LocalDate getFaseFin();
        String getFigmaUrl();
        Number getDisenoDeFase();
        Boolean getDisenoAprobado();
        String getDisenoAprobadoPor();
        LocalDateTime getDisenoAprobadoEn();
        Long getCriterios();
        Long getModulos();
    }

    interface TicketFila {
        Long getId();
        String getTipo();
        Integer getNumero();
        String getTitulo();
        String getEstado();
        Long getResponsableId();
        Long getTesterId();
        String getModuloTipo();
        String getResponsableNombre();
        String getDependeDe();
    }

    interface FaseFila {
        Long getId();
        String getTitulo();
    }

    interface CriterioFila {
        Long getId();
        String getDescripcion();
        Boolean getVerificado();
        String getTickets();
    }

    interface CambioFila {
        String getEstado();
        String getMotivo();
        Boolean getAutomatico();
        String getUsuario();
        LocalDateTime getFecha();
    }

    interface AceptacionFila {
        String getUsuario();
        LocalDateTime getFecha();
    }

    interface TicketPrFila {
        Long getId();
        String getTipo();
        Integer getNumero();
        String getTitulo();
        String getEstado();
        Long getModuloId();
        String getRama();
        Integer getPrNumero();
        String getPrEstado();
        String getPrCi();
        Boolean getPrConConflictos();
        LocalDateTime getPrRevisadoEn();
    }

    interface ModuloFila {
        Long getId();
        String getTipo();
        String getNombre();
        String getTecnologia();
        Long getIntegradas();
    }

    interface HistoriaDeFaseFila {
        Long getId();
        Integer getNumero();
        String getEstado();
    }

    interface PublicadaFila {
        String getVersion();
        LocalDateTime getFecha();
    }

    interface AdjuntoFila {
        Long getId();
        String getNombre();
        String getTipoMime();
        Long getTamanoBytes();
        String getRuta();
    }

    interface ComentarioFila {
        String getUsuario();
        String getTexto();
        LocalDateTime getFecha();
    }
}
