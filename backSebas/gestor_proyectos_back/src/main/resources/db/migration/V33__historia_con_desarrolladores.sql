-- La historia le llega directo a sus Desarrolladores (sin Actividades como ticket).
-- Una historia con Desarrolladores asignados se trabaja así; las que ya tienen Actividades siguen como estaban.

-- Quiénes la desarrollan; opcionalmente, en qué repositorio. Cada uno marca cuando terminó su parte.
CREATE TABLE historia_desarrollador (
    historia_id BIGINT  NOT NULL,
    usuario_id  BIGINT  NOT NULL,
    modulo_id   BIGINT  NULL,
    terminado   BOOLEAN NOT NULL DEFAULT FALSE,
    PRIMARY KEY (historia_id, usuario_id),
    CONSTRAINT fk_historia_dev_historia FOREIGN KEY (historia_id) REFERENCES historia (id),
    CONSTRAINT fk_historia_dev_usuario  FOREIGN KEY (usuario_id)  REFERENCES usuario (id),
    CONSTRAINT fk_historia_dev_modulo   FOREIGN KEY (modulo_id)   REFERENCES modulo (id)
);

-- "Lo que hice": lo que el Desarrollador va haciendo (o la guía que deja la Analista o el Líder para marcar).
CREATE TABLE historia_trabajo (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    historia_id BIGINT       NOT NULL,
    descripcion VARCHAR(300) NOT NULL,
    modulo_id   BIGINT       NULL,
    hecho       BOOLEAN      NOT NULL DEFAULT FALSE,
    orden       INT          NOT NULL DEFAULT 0,
    creado_por  BIGINT       NOT NULL,
    creado_en   DATETIME     NOT NULL,
    CONSTRAINT fk_historia_trabajo_historia FOREIGN KEY (historia_id) REFERENCES historia (id),
    CONSTRAINT fk_historia_trabajo_modulo   FOREIGN KEY (modulo_id)   REFERENCES modulo (id),
    CONSTRAINT fk_historia_trabajo_usuario  FOREIGN KEY (creado_por)  REFERENCES usuario (id)
);
