-- =====================================================================
-- V5 · Tickets de trabajo: ACTV, BUGS, MEJR, TECN, INVS, DOCT
-- =====================================================================
-- Estados por tipo (ver PDF "Acciones por estado"):
--   ACTV: POR_HACER, EN_PROGRESO, BLOQUEADA, EN_QA, APROBADA, INTEGRADA, CERRADA, CANCELADA
--   BUGS: REPORTADO, CONFIRMADO, EN_CORRECCION, EN_QA, APROBADO, INTEGRADO, PUBLICADO, CERRADO, NO_REPRODUCIBLE
--   MEJR: PROPUESTA, APROBADA, EN_PROGRESO, EN_QA, APROBADA_QA, INTEGRADA, CERRADA, RECHAZADA
--   TECN: POR_HACER, EN_PROGRESO, EN_REVISION, HECHA, INTEGRADA, CERRADA
--   INVS: POR_HACER, INVESTIGANDO, CONCLUSIONES, REVISADA
--   DOCT: POR_HACER, REDACTANDO, EN_REVISION, PUBLICADA

CREATE TABLE ticket (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    proyecto_id       BIGINT       NOT NULL,
    fase_id           BIGINT       NOT NULL,
    historia_id       BIGINT,
    tipo              VARCHAR(4)   NOT NULL,
    numero            INT          NOT NULL,
    titulo            VARCHAR(200) NOT NULL,
    descripcion       TEXT,
    estado            VARCHAR(20)  NOT NULL,
    prioridad         VARCHAR(10)  NOT NULL DEFAULT 'MEDIA',
    severidad         VARCHAR(10),
    modulo_id         BIGINT,
    responsable_id    BIGINT,
    tester_id         BIGINT,
    creado_por        BIGINT       NOT NULL,
    estimacion_horas  DECIMAL(5, 1),
    horas_registradas DECIMAL(5, 1) NOT NULL DEFAULT 0,
    fecha_limite      DATE,
    rama              VARCHAR(150),
    capas             VARCHAR(200),
    conclusiones      TEXT,
    visible_cliente   BOOLEAN      NOT NULL DEFAULT FALSE,
    origen_ticket_id  BIGINT,
    creado_en         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_ticket_numero (proyecto_id, tipo, numero),
    INDEX idx_ticket_fase_estado (fase_id, estado),
    INDEX idx_ticket_responsable (responsable_id, estado),
    CONSTRAINT fk_ticket_proyecto    FOREIGN KEY (proyecto_id)      REFERENCES proyecto (id),
    CONSTRAINT fk_ticket_fase        FOREIGN KEY (fase_id)          REFERENCES fase (id),
    CONSTRAINT fk_ticket_historia    FOREIGN KEY (historia_id)      REFERENCES historia (id),
    CONSTRAINT fk_ticket_modulo      FOREIGN KEY (modulo_id)        REFERENCES modulo (id),
    CONSTRAINT fk_ticket_responsable FOREIGN KEY (responsable_id)   REFERENCES usuario (id),
    CONSTRAINT fk_ticket_tester      FOREIGN KEY (tester_id)        REFERENCES usuario (id),
    CONSTRAINT fk_ticket_creado_por  FOREIGN KEY (creado_por)       REFERENCES usuario (id),
    CONSTRAINT fk_ticket_origen      FOREIGN KEY (origen_ticket_id) REFERENCES ticket (id),
    CONSTRAINT chk_ticket_tipo CHECK (tipo IN ('ACTV', 'BUGS', 'MEJR', 'TECN', 'INVS', 'DOCT'))
);

-- Criterios de la historia que cubre el ticket.
CREATE TABLE ticket_criterio (
    ticket_id   BIGINT NOT NULL,
    criterio_id BIGINT NOT NULL,
    PRIMARY KEY (ticket_id, criterio_id),
    CONSTRAINT fk_ticket_criterio_ticket   FOREIGN KEY (ticket_id)   REFERENCES ticket (id),
    CONSTRAINT fk_ticket_criterio_criterio FOREIGN KEY (criterio_id) REFERENCES historia_criterio (id)
);

-- Pasos para quien lo desarrolla. No cambian el estado del ticket.
CREATE TABLE ticket_subtarea (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    ticket_id   BIGINT       NOT NULL,
    descripcion VARCHAR(300) NOT NULL,
    hecha       BOOLEAN      NOT NULL DEFAULT FALSE,
    orden       INT          NOT NULL DEFAULT 0,
    CONSTRAINT fk_subtarea_ticket FOREIGN KEY (ticket_id) REFERENCES ticket (id)
);

-- No se puede iniciar un ticket hasta que se cierre aquel del que depende.
CREATE TABLE ticket_dependencia (
    ticket_id     BIGINT NOT NULL,
    depende_de_id BIGINT NOT NULL,
    PRIMARY KEY (ticket_id, depende_de_id),
    CONSTRAINT fk_dependencia_ticket     FOREIGN KEY (ticket_id)     REFERENCES ticket (id),
    CONSTRAINT fk_dependencia_depende_de FOREIGN KEY (depende_de_id) REFERENCES ticket (id)
);

-- PR de la rama del ticket hacia develop (o main en hotfix).
-- estado: ABIERTO, APROBADO, INTEGRADO, CERRADO   ci_estado: PENDIENTE, PASO, FALLO
CREATE TABLE pull_request (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    ticket_id     BIGINT       NOT NULL,
    modulo_id     BIGINT       NOT NULL,
    numero        INT          NOT NULL,
    rama_origen   VARCHAR(150) NOT NULL,
    rama_destino  VARCHAR(60)  NOT NULL,
    estado        VARCHAR(12)  NOT NULL DEFAULT 'ABIERTO',
    ci_estado     VARCHAR(10)  NOT NULL DEFAULT 'PENDIENTE',
    url           VARCHAR(300),
    creado_en     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_pr_ticket FOREIGN KEY (ticket_id) REFERENCES ticket (id),
    CONSTRAINT fk_pr_modulo FOREIGN KEY (modulo_id) REFERENCES modulo (id)
);
