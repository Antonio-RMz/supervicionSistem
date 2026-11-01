-- =====================================================================
-- V7 · Releases, demos y solicitudes del cliente (Portal de avance)
-- =====================================================================

-- Versión publicada. FASE = cierre de una fase (v1.3.0); PARCHE = hotfix (v1.3.1).
-- estado: PREPARANDO, PUBLICADA, REVERTIDA
-- (se llama version_release porque RELEASE es palabra reservada en MySQL)
CREATE TABLE version_release (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    proyecto_id   BIGINT       NOT NULL,
    fase_id       BIGINT,
    version       VARCHAR(20)  NOT NULL,
    tipo          VARCHAR(10)  NOT NULL DEFAULT 'FASE',
    estado        VARCHAR(12)  NOT NULL DEFAULT 'PREPARANDO',
    resumen       VARCHAR(200),
    notas         TEXT,
    publicado_por BIGINT,
    publicado_en  DATETIME,
    UNIQUE KEY uk_release_version (proyecto_id, version),
    CONSTRAINT fk_release_proyecto FOREIGN KEY (proyecto_id)   REFERENCES proyecto (id),
    CONSTRAINT fk_release_fase     FOREIGN KEY (fase_id)       REFERENCES fase (id),
    CONSTRAINT fk_release_usuario  FOREIGN KEY (publicado_por) REFERENCES usuario (id)
);

-- Demo que el cliente revisa para dar su visto bueno a la fase.
-- estado: PENDIENTE, APROBADA, CON_CAMBIOS
CREATE TABLE demo (
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    fase_id            BIGINT       NOT NULL,
    titulo             VARCHAR(150) NOT NULL,
    url                VARCHAR(300),
    estado             VARCHAR(12)  NOT NULL DEFAULT 'PENDIENTE',
    comentario_cliente TEXT,
    revisada_por       BIGINT,
    revisada_en        DATETIME,
    creado_en          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_demo_fase    FOREIGN KEY (fase_id)      REFERENCES fase (id),
    CONSTRAINT fk_demo_usuario FOREIGN KEY (revisada_por) REFERENCES usuario (id)
);

-- Lo que el cliente pide desde el portal. Si procede, se planea en una fase o se vuelve ticket.
-- tipo: NUEVA_FUNCION, CAMBIO, ERROR, DUDA
-- estado: RECIBIDA, EN_REVISION, PLANEADA, RECHAZADA, RESUELTA
CREATE TABLE solicitud (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    proyecto_id BIGINT       NOT NULL,
    usuario_id  BIGINT       NOT NULL,
    tipo        VARCHAR(15)  NOT NULL,
    titulo      VARCHAR(150) NOT NULL,
    descripcion TEXT,
    estado      VARCHAR(12)  NOT NULL DEFAULT 'RECIBIDA',
    respuesta   TEXT,
    fase_id     BIGINT,
    ticket_id   BIGINT,
    creado_en   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_solicitud_proyecto FOREIGN KEY (proyecto_id) REFERENCES proyecto (id),
    CONSTRAINT fk_solicitud_usuario  FOREIGN KEY (usuario_id)  REFERENCES usuario (id),
    CONSTRAINT fk_solicitud_fase     FOREIGN KEY (fase_id)     REFERENCES fase (id),
    CONSTRAINT fk_solicitud_ticket   FOREIGN KEY (ticket_id)   REFERENCES ticket (id)
);
