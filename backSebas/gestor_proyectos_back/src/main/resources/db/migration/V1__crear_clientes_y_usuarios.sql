-- =====================================================================
-- V1 · Clientes, usuarios, roles y permisos
-- =====================================================================

-- Empresas para las que trabaja el equipo. Cada proyecto pertenece a un cliente.
CREATE TABLE cliente (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    razon_social      VARCHAR(150) NOT NULL,
    nombre_corto      VARCHAR(80)  NOT NULL,
    rfc               VARCHAR(13),
    ciudad            VARCHAR(80),
    contacto_nombre   VARCHAR(120) NOT NULL,
    contacto_correo   VARCHAR(120),
    contacto_telefono VARCHAR(20),
    notas             TEXT,
    activo            BOOLEAN      NOT NULL DEFAULT TRUE,
    creado_en         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Administrador, Líder, Analista, Desarrollador, Tester.
CREATE TABLE rol (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    codigo      VARCHAR(30)  NOT NULL UNIQUE,
    nombre      VARCHAR(60)  NOT NULL,
    descripcion VARCHAR(200)
);

-- Acciones del sistema, ej. release.publicar, historia.aceptar.
CREATE TABLE permiso (
    id     BIGINT AUTO_INCREMENT PRIMARY KEY,
    codigo VARCHAR(60)  NOT NULL UNIQUE,
    nombre VARCHAR(120) NOT NULL,
    grupo  VARCHAR(40)  NOT NULL
);

CREATE TABLE rol_permiso (
    rol_id     BIGINT NOT NULL,
    permiso_id BIGINT NOT NULL,
    PRIMARY KEY (rol_id, permiso_id),
    CONSTRAINT fk_rol_permiso_rol     FOREIGN KEY (rol_id)     REFERENCES rol (id),
    CONSTRAINT fk_rol_permiso_permiso FOREIGN KEY (permiso_id) REFERENCES permiso (id)
);

-- EQUIPO: personas de Team Dev (entran al Gestor).
-- CLIENTE: contactos de un cliente (entran al Portal de avance y ven los proyectos de su empresa).
CREATE TABLE usuario (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre           VARCHAR(80)  NOT NULL,
    apellidos        VARCHAR(120),
    correo           VARCHAR(120) NOT NULL UNIQUE,
    password_hash    VARCHAR(100) NOT NULL,
    tipo             VARCHAR(10)  NOT NULL,
    cliente_id       BIGINT,
    activo           BOOLEAN      NOT NULL DEFAULT TRUE,
    cambiar_password BOOLEAN      NOT NULL DEFAULT TRUE,
    ultimo_acceso    DATETIME,
    creado_en        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_usuario_cliente  FOREIGN KEY (cliente_id) REFERENCES cliente (id),
    CONSTRAINT chk_usuario_tipo    CHECK (tipo IN ('EQUIPO', 'CLIENTE')),
    CONSTRAINT chk_usuario_cliente CHECK (tipo = 'EQUIPO' OR cliente_id IS NOT NULL)
);

-- Un usuario puede tener varios roles; sus permisos se suman.
CREATE TABLE usuario_rol (
    usuario_id BIGINT NOT NULL,
    rol_id     BIGINT NOT NULL,
    PRIMARY KEY (usuario_id, rol_id),
    CONSTRAINT fk_usuario_rol_usuario FOREIGN KEY (usuario_id) REFERENCES usuario (id),
    CONSTRAINT fk_usuario_rol_rol     FOREIGN KEY (rol_id)     REFERENCES rol (id)
);
