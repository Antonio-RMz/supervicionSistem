-- =====================================================================
-- V2 · Proyectos, equipo, módulos y permisos extra
-- =====================================================================

-- estado: PLANEACION, EN_CURSO, EN_PAUSA, TERMINADO, CANCELADO
-- prioridad: BAJA, MEDIA, ALTA
CREATE TABLE proyecto (
    id                       BIGINT AUTO_INCREMENT PRIMARY KEY,
    codigo                   VARCHAR(5)   NOT NULL UNIQUE,
    nombre                   VARCHAR(120) NOT NULL,
    descripcion              TEXT,
    cliente_id               BIGINT       NOT NULL,
    tipo                     VARCHAR(40)  NOT NULL,
    prioridad                VARCHAR(10)  NOT NULL DEFAULT 'MEDIA',
    estado                   VARCHAR(20)  NOT NULL DEFAULT 'PLANEACION',
    fecha_inicio             DATE,
    fecha_entrega            DATE,
    lider_id                 BIGINT       NOT NULL,
    -- Reglas del flujo
    permitir_autovalidacion  BOOLEAN      NOT NULL DEFAULT FALSE,
    comentario_al_rechazar   BOOLEAN      NOT NULL DEFAULT TRUE,
    rama_al_iniciar          BOOLEAN      NOT NULL DEFAULT TRUE,
    notificar_cliente_fase   BOOLEAN      NOT NULL DEFAULT TRUE,
    creado_en                DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en           DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_proyecto_cliente FOREIGN KEY (cliente_id) REFERENCES cliente (id),
    CONSTRAINT fk_proyecto_lider   FOREIGN KEY (lider_id)   REFERENCES usuario (id)
);

-- Quién entra a cada proyecto. Los roles salen del usuario.
CREATE TABLE proyecto_miembro (
    proyecto_id BIGINT   NOT NULL,
    usuario_id  BIGINT   NOT NULL,
    agregado_en DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (proyecto_id, usuario_id),
    CONSTRAINT fk_miembro_proyecto FOREIGN KEY (proyecto_id) REFERENCES proyecto (id),
    CONSTRAINT fk_miembro_usuario  FOREIGN KEY (usuario_id)  REFERENCES usuario (id)
);

-- Parte del sistema con su propia tecnología y repositorio. Los tickets se asignan a un módulo.
-- tipo: API, WEB, MOVIL, ESCRITORIO, BASE_DATOS, INFRAESTRUCTURA
CREATE TABLE modulo (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    proyecto_id BIGINT       NOT NULL,
    tipo        VARCHAR(20)  NOT NULL,
    nombre      VARCHAR(60)  NOT NULL,
    tecnologia  VARCHAR(80),
    repositorio VARCHAR(200),
    rama_base   VARCHAR(60)  NOT NULL DEFAULT 'develop',
    UNIQUE KEY uk_modulo_nombre (proyecto_id, nombre),
    CONSTRAINT fk_modulo_proyecto FOREIGN KEY (proyecto_id) REFERENCES proyecto (id)
);

-- Ajustes puntuales sobre lo que dan los roles.
-- proyecto_id NULL = aplica en todos los proyectos. otorgado FALSE = permiso quitado.
CREATE TABLE usuario_permiso (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id  BIGINT  NOT NULL,
    permiso_id  BIGINT  NOT NULL,
    proyecto_id BIGINT,
    otorgado    BOOLEAN NOT NULL,
    CONSTRAINT fk_usuario_permiso_usuario  FOREIGN KEY (usuario_id)  REFERENCES usuario (id),
    CONSTRAINT fk_usuario_permiso_permiso  FOREIGN KEY (permiso_id)  REFERENCES permiso (id),
    CONSTRAINT fk_usuario_permiso_proyecto FOREIGN KEY (proyecto_id) REFERENCES proyecto (id)
);
