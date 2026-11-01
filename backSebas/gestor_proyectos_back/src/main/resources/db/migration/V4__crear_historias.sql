-- =====================================================================
-- V4 · Historias de usuario (HIST)
-- =====================================================================

-- estado: BORRADOR, LISTA, EN_DESARROLLO, EN_VALIDACION, ACEPTADA, INTEGRADA, CERRADA, DESCARTADA
CREATE TABLE historia (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    proyecto_id         BIGINT       NOT NULL,
    fase_id             BIGINT       NOT NULL,
    numero              INT          NOT NULL,
    titulo              VARCHAR(150) NOT NULL,
    como                VARCHAR(200),
    quiero              VARCHAR(300),
    para                VARCHAR(300),
    estado              VARCHAR(20)  NOT NULL DEFAULT 'BORRADOR',
    prioridad           VARCHAR(10)  NOT NULL DEFAULT 'MEDIA',
    puntos              INT,
    analista_id         BIGINT       NOT NULL,
    requerimiento_id    BIGINT,
    figma_url           VARCHAR(300),
    diseno_aprobado     BOOLEAN      NOT NULL DEFAULT FALSE,
    diseno_aprobado_por BIGINT,
    diseno_aprobado_en  DATETIME,
    creado_por          BIGINT       NOT NULL,
    creado_en           DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_historia_numero (proyecto_id, numero),
    CONSTRAINT fk_historia_proyecto      FOREIGN KEY (proyecto_id)         REFERENCES proyecto (id),
    CONSTRAINT fk_historia_fase          FOREIGN KEY (fase_id)             REFERENCES fase (id),
    CONSTRAINT fk_historia_analista      FOREIGN KEY (analista_id)         REFERENCES usuario (id),
    CONSTRAINT fk_historia_requerimiento FOREIGN KEY (requerimiento_id)    REFERENCES fase_requerimiento (id),
    CONSTRAINT fk_historia_diseno_por    FOREIGN KEY (diseno_aprobado_por) REFERENCES usuario (id),
    CONSTRAINT fk_historia_creado_por    FOREIGN KEY (creado_por)          REFERENCES usuario (id)
);

CREATE TABLE historia_modulo (
    historia_id BIGINT NOT NULL,
    modulo_id   BIGINT NOT NULL,
    PRIMARY KEY (historia_id, modulo_id),
    CONSTRAINT fk_historia_modulo_historia FOREIGN KEY (historia_id) REFERENCES historia (id),
    CONSTRAINT fk_historia_modulo_modulo   FOREIGN KEY (modulo_id)   REFERENCES modulo (id)
);

-- El Analista los usa para aceptar o devolver la historia.
CREATE TABLE historia_criterio (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    historia_id BIGINT       NOT NULL,
    descripcion VARCHAR(300) NOT NULL,
    verificado  BOOLEAN      NOT NULL DEFAULT FALSE,
    orden       INT          NOT NULL DEFAULT 0,
    CONSTRAINT fk_criterio_historia FOREIGN KEY (historia_id) REFERENCES historia (id)
);

-- Reglas de negocio de la fase que esta historia debe respetar.
CREATE TABLE historia_regla (
    historia_id BIGINT NOT NULL,
    regla_id    BIGINT NOT NULL,
    PRIMARY KEY (historia_id, regla_id),
    CONSTRAINT fk_historia_regla_historia FOREIGN KEY (historia_id) REFERENCES historia (id),
    CONSTRAINT fk_historia_regla_regla    FOREIGN KEY (regla_id)    REFERENCES fase_regla_negocio (id)
);
