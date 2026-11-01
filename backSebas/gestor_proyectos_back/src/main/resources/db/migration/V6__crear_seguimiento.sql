-- =====================================================================
-- V6 · Historial de estados, comentarios y adjuntos
-- =====================================================================
-- entidad: FASE, HISTORIA, TICKET (las tres tablas comparten historial y comentarios)

-- Todo cambio de estado queda con quién, cuándo y el motivo.
CREATE TABLE historial_estado (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    entidad         VARCHAR(10)   NOT NULL,
    entidad_id      BIGINT        NOT NULL,
    estado_anterior VARCHAR(25),
    estado_nuevo    VARCHAR(25)   NOT NULL,
    motivo          VARCHAR(1000),
    automatico      BOOLEAN       NOT NULL DEFAULT FALSE,
    usuario_id      BIGINT,
    creado_en       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_historial_entidad (entidad, entidad_id),
    CONSTRAINT fk_historial_usuario FOREIGN KEY (usuario_id) REFERENCES usuario (id),
    CONSTRAINT chk_historial_entidad CHECK (entidad IN ('FASE', 'HISTORIA', 'TICKET'))
);

CREATE TABLE comentario (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    entidad    VARCHAR(10) NOT NULL,
    entidad_id BIGINT      NOT NULL,
    usuario_id BIGINT      NOT NULL,
    texto      TEXT        NOT NULL,
    creado_en  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_comentario_entidad (entidad, entidad_id),
    CONSTRAINT fk_comentario_usuario FOREIGN KEY (usuario_id) REFERENCES usuario (id),
    CONSTRAINT chk_comentario_entidad CHECK (entidad IN ('FASE', 'HISTORIA', 'TICKET'))
);

CREATE TABLE adjunto (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    entidad       VARCHAR(10)  NOT NULL,
    entidad_id    BIGINT       NOT NULL,
    nombre        VARCHAR(200) NOT NULL,
    tipo_mime     VARCHAR(100),
    tamano_bytes  BIGINT,
    ruta          VARCHAR(400) NOT NULL,
    subido_por    BIGINT       NOT NULL,
    creado_en     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_adjunto_entidad (entidad, entidad_id),
    CONSTRAINT fk_adjunto_usuario FOREIGN KEY (subido_por) REFERENCES usuario (id),
    CONSTRAINT chk_adjunto_entidad CHECK (entidad IN ('FASE', 'HISTORIA', 'TICKET'))
);
