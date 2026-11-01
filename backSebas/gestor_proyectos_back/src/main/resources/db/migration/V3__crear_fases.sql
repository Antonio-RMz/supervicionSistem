-- =====================================================================
-- V3 · Fases: requerimientos, reglas de negocio y criterios de cierre
-- =====================================================================

-- Una fase a la vez, en orden. Cerrar la fase es publicar su release.
-- estado: PLANEADA, EN_CURSO, PAUSADA, LISTA_PARA_RELEASE, EN_PUBLICACION, PUBLICADA, REVERTIDA, CANCELADA
-- es_mantenimiento: fase permanente donde viven los hotfix (numero = 0).
CREATE TABLE fase (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    proyecto_id      BIGINT       NOT NULL,
    numero           INT          NOT NULL,
    nombre           VARCHAR(120) NOT NULL,
    objetivo         TEXT,
    estado           VARCHAR(25)  NOT NULL DEFAULT 'PLANEADA',
    prioridad        VARCHAR(10)  NOT NULL DEFAULT 'MEDIA',
    fecha_inicio     DATE,
    fecha_fin        DATE,
    responsable_id   BIGINT       NOT NULL,
    version_release  VARCHAR(20),
    es_mantenimiento BOOLEAN      NOT NULL DEFAULT FALSE,
    motivo_estado    VARCHAR(500),
    reanudar_el      DATE,
    creado_en        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_fase_numero (proyecto_id, numero),
    CONSTRAINT fk_fase_proyecto    FOREIGN KEY (proyecto_id)    REFERENCES proyecto (id),
    CONSTRAINT fk_fase_responsable FOREIGN KEY (responsable_id) REFERENCES usuario (id)
);

-- Qué módulos se trabajan en la fase. Sus tickets solo se asignan a estos módulos.
CREATE TABLE fase_modulo (
    fase_id   BIGINT NOT NULL,
    modulo_id BIGINT NOT NULL,
    PRIMARY KEY (fase_id, modulo_id),
    CONSTRAINT fk_fase_modulo_fase   FOREIGN KEY (fase_id)   REFERENCES fase (id),
    CONSTRAINT fk_fase_modulo_modulo FOREIGN KEY (modulo_id) REFERENCES modulo (id)
);

-- Lo que el cliente pidió para la fase. El Analista los desglosa en historias.
CREATE TABLE fase_requerimiento (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    fase_id     BIGINT       NOT NULL,
    descripcion VARCHAR(300) NOT NULL,
    orden       INT          NOT NULL DEFAULT 0,
    CONSTRAINT fk_requerimiento_fase FOREIGN KEY (fase_id) REFERENCES fase (id)
);

-- Condiciones que todo el desarrollo de la fase debe respetar.
CREATE TABLE fase_regla_negocio (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    fase_id     BIGINT       NOT NULL,
    descripcion VARCHAR(300) NOT NULL,
    orden       INT          NOT NULL DEFAULT 0,
    CONSTRAINT fk_regla_fase FOREIGN KEY (fase_id) REFERENCES fase (id)
);

-- El Líder solo cierra la fase cuando se cumplen todos.
-- tipo AUTOMATICO usa codigo: HISTORIAS_ACEPTADAS, SIN_BUGS_ALTA, MODULOS_EN_STAGING
-- tipo MANUAL lo marca una persona (ej. visto bueno del cliente en la demo).
CREATE TABLE fase_criterio_cierre (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    fase_id      BIGINT       NOT NULL,
    descripcion  VARCHAR(300) NOT NULL,
    tipo         VARCHAR(12)  NOT NULL,
    codigo       VARCHAR(40),
    cumplido     BOOLEAN      NOT NULL DEFAULT FALSE,
    cumplido_por BIGINT,
    cumplido_en  DATETIME,
    CONSTRAINT fk_criterio_cierre_fase    FOREIGN KEY (fase_id)      REFERENCES fase (id),
    CONSTRAINT fk_criterio_cierre_usuario FOREIGN KEY (cumplido_por) REFERENCES usuario (id),
    CONSTRAINT chk_criterio_cierre_tipo   CHECK (tipo IN ('AUTOMATICO', 'MANUAL'))
);
