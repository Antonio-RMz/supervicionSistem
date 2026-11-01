/*
 Navicat Premium Dump SQL

 Source Server         : LOCAL
 Source Server Type    : MySQL
 Source Server Version : 80410 (8.4.10)
 Source Host           : 127.0.0.1:3306
 Source Schema         : gestor_proyectos

 Target Server Type    : MySQL
 Target Server Version : 80410 (8.4.10)
 File Encoding         : 65001

 Date: 29/10/2026 21:41:39
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for adjunto
-- ----------------------------
DROP TABLE IF EXISTS `adjunto`;
CREATE TABLE `adjunto` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `entidad` varchar(10) NOT NULL,
  `entidad_id` bigint NOT NULL,
  `nombre` varchar(200) NOT NULL,
  `tipo_mime` varchar(100) DEFAULT NULL,
  `tamano_bytes` bigint DEFAULT NULL,
  `ruta` varchar(400) NOT NULL,
  `subido_por` bigint NOT NULL,
  `creado_en` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_adjunto_entidad` (`entidad`,`entidad_id`),
  KEY `fk_adjunto_usuario` (`subido_por`),
  CONSTRAINT `fk_adjunto_usuario` FOREIGN KEY (`subido_por`) REFERENCES `usuario` (`id`),
  CONSTRAINT `chk_adjunto_entidad` CHECK ((`entidad` in (_utf8mb4'FASE',_utf8mb4'HISTORIA',_utf8mb4'TICKET')))
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Records of adjunto
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for cliente
-- ----------------------------
DROP TABLE IF EXISTS `cliente`;
CREATE TABLE `cliente` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `razon_social` varchar(150) NOT NULL,
  `nombre_corto` varchar(80) NOT NULL,
  `rfc` varchar(13) DEFAULT NULL,
  `giro` varchar(20) DEFAULT NULL,
  `ciudad` varchar(80) DEFAULT NULL,
  `contacto_nombre` varchar(120) DEFAULT NULL,
  `contacto_correo` varchar(120) DEFAULT NULL,
  `contacto_telefono` varchar(20) DEFAULT NULL,
  `notas` text,
  `activo` tinyint(1) NOT NULL DEFAULT '1',
  `creado_en` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `actualizado_en` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_cliente_nombre_corto` (`nombre_corto`)
) ENGINE=InnoDB AUTO_INCREMENT=1007 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Records of cliente
-- ----------------------------
BEGIN;
INSERT INTO `cliente` (`id`, `razon_social`, `nombre_corto`, `rfc`, `giro`, `ciudad`, `contacto_nombre`, `contacto_correo`, `contacto_telefono`, `notas`, `activo`, `creado_en`, `actualizado_en`) VALUES (1001, 'Supervisión Escolar', 'Supervisión Escolar', NULL, 'EDUCACION', NULL, NULL, NULL, NULL, NULL, 1, '2026-10-18 22:10:25', '2026-10-24 20:27:20');
COMMIT;

-- ----------------------------
-- Table structure for comentario
-- ----------------------------
DROP TABLE IF EXISTS `comentario`;
CREATE TABLE `comentario` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `entidad` varchar(10) NOT NULL,
  `entidad_id` bigint NOT NULL,
  `usuario_id` bigint NOT NULL,
  `texto` text NOT NULL,
  `creado_en` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_comentario_entidad` (`entidad`,`entidad_id`),
  KEY `fk_comentario_usuario` (`usuario_id`),
  CONSTRAINT `fk_comentario_usuario` FOREIGN KEY (`usuario_id`) REFERENCES `usuario` (`id`),
  CONSTRAINT `chk_comentario_entidad` CHECK ((`entidad` in (_utf8mb4'FASE',_utf8mb4'HISTORIA',_utf8mb4'TICKET')))
) ENGINE=InnoDB AUTO_INCREMENT=1019 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Records of comentario
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for demo
-- ----------------------------
DROP TABLE IF EXISTS `demo`;
CREATE TABLE `demo` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `fase_id` bigint NOT NULL,
  `titulo` varchar(150) NOT NULL,
  `url` varchar(300) DEFAULT NULL,
  `estado` varchar(12) NOT NULL DEFAULT 'PENDIENTE',
  `comentario_cliente` text,
  `revisada_por` bigint DEFAULT NULL,
  `revisada_en` datetime DEFAULT NULL,
  `creado_en` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `fk_demo_fase` (`fase_id`),
  KEY `fk_demo_usuario` (`revisada_por`),
  CONSTRAINT `fk_demo_fase` FOREIGN KEY (`fase_id`) REFERENCES `fase` (`id`),
  CONSTRAINT `fk_demo_usuario` FOREIGN KEY (`revisada_por`) REFERENCES `usuario` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1002 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Records of demo
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for fase
-- ----------------------------
DROP TABLE IF EXISTS `fase`;
CREATE TABLE `fase` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `proyecto_id` bigint NOT NULL,
  `numero` int NOT NULL,
  `nombre` varchar(120) NOT NULL,
  `objetivo` text,
  `estado` varchar(25) NOT NULL DEFAULT 'PLANEADA',
  `prioridad` varchar(10) NOT NULL DEFAULT 'MEDIA',
  `fecha_inicio` date DEFAULT NULL,
  `fecha_fin` date DEFAULT NULL,
  `responsable_id` bigint NOT NULL,
  `version_release` varchar(20) DEFAULT NULL,
  `es_mantenimiento` tinyint(1) NOT NULL DEFAULT '0',
  `motivo_estado` varchar(500) DEFAULT NULL,
  `reanudar_el` date DEFAULT NULL,
  `bug_reversion_id` bigint DEFAULT NULL,
  `creado_en` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `actualizado_en` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `figma_url` varchar(300) DEFAULT NULL,
  `figma_para_todas` tinyint(1) NOT NULL DEFAULT '1',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_fase_numero` (`proyecto_id`,`numero`),
  KEY `fk_fase_responsable` (`responsable_id`),
  KEY `fk_fase_bug_reversion` (`bug_reversion_id`),
  CONSTRAINT `fk_fase_bug_reversion` FOREIGN KEY (`bug_reversion_id`) REFERENCES `ticket` (`id`),
  CONSTRAINT `fk_fase_proyecto` FOREIGN KEY (`proyecto_id`) REFERENCES `proyecto` (`id`),
  CONSTRAINT `fk_fase_responsable` FOREIGN KEY (`responsable_id`) REFERENCES `usuario` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1046 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Records of fase
-- ----------------------------
BEGIN;
INSERT INTO `fase` (`id`, `proyecto_id`, `numero`, `nombre`, `objetivo`, `estado`, `prioridad`, `fecha_inicio`, `fecha_fin`, `responsable_id`, `version_release`, `es_mantenimiento`, `motivo_estado`, `reanudar_el`, `bug_reversion_id`, `creado_en`, `actualizado_en`, `figma_url`, `figma_para_todas`) VALUES (1042, 1016, 0, 'Mantenimiento', 'Correcciones urgentes (hotfix) de lo que ya está en producción.', 'EN_CURSO', 'ALTA', NULL, NULL, 1016, NULL, 1, NULL, NULL, NULL, '2026-10-28 21:51:28', '2026-10-28 21:51:28', NULL, 1);
INSERT INTO `fase` (`id`, `proyecto_id`, `numero`, `nombre`, `objetivo`, `estado`, `prioridad`, `fecha_inicio`, `fecha_fin`, `responsable_id`, `version_release`, `es_mantenimiento`, `motivo_estado`, `reanudar_el`, `bug_reversion_id`, `creado_en`, `actualizado_en`, `figma_url`, `figma_para_todas`) VALUES (1043, 1016, 1, 'Primera versión', 'El Administrador da de alta escuelas y sus usuarios; cada escuela entra a su vista y registra sus periodos, generaciones, materias y alumnos.', 'EN_CURSO', 'ALTA', NULL, NULL, 1016, 'v1.0.0', 0, NULL, NULL, NULL, '2026-10-28 21:51:28', '2026-10-29 21:28:28', NULL, 1);
COMMIT;

-- ----------------------------
-- Table structure for fase_criterio_cierre
-- ----------------------------
DROP TABLE IF EXISTS `fase_criterio_cierre`;
CREATE TABLE `fase_criterio_cierre` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `fase_id` bigint NOT NULL,
  `descripcion` varchar(300) NOT NULL,
  `tipo` varchar(12) NOT NULL,
  `codigo` varchar(40) DEFAULT NULL,
  `cumplido` tinyint(1) NOT NULL DEFAULT '0',
  `cumplido_por` bigint DEFAULT NULL,
  `cumplido_en` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_criterio_cierre_fase` (`fase_id`),
  KEY `fk_criterio_cierre_usuario` (`cumplido_por`),
  CONSTRAINT `fk_criterio_cierre_fase` FOREIGN KEY (`fase_id`) REFERENCES `fase` (`id`),
  CONSTRAINT `fk_criterio_cierre_usuario` FOREIGN KEY (`cumplido_por`) REFERENCES `usuario` (`id`),
  CONSTRAINT `chk_criterio_cierre_tipo` CHECK ((`tipo` in (_utf8mb4'AUTOMATICO',_utf8mb4'MANUAL')))
) ENGINE=InnoDB AUTO_INCREMENT=1090 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Records of fase_criterio_cierre
-- ----------------------------
BEGIN;
INSERT INTO `fase_criterio_cierre` (`id`, `fase_id`, `descripcion`, `tipo`, `codigo`, `cumplido`, `cumplido_por`, `cumplido_en`) VALUES (1081, 1043, 'Todas las historias aceptadas por el Analista', 'AUTOMATICO', 'HISTORIAS_ACEPTADAS', 0, NULL, NULL);
INSERT INTO `fase_criterio_cierre` (`id`, `fase_id`, `descripcion`, `tipo`, `codigo`, `cumplido`, `cumplido_por`, `cumplido_en`) VALUES (1082, 1043, 'Sin bugs de prioridad alta abiertos', 'AUTOMATICO', 'SIN_BUGS_ALTA', 0, NULL, NULL);
INSERT INTO `fase_criterio_cierre` (`id`, `fase_id`, `descripcion`, `tipo`, `codigo`, `cumplido`, `cumplido_por`, `cumplido_en`) VALUES (1083, 1043, 'Repositorios involucrados desplegados en staging', 'AUTOMATICO', 'MODULOS_EN_STAGING', 0, NULL, NULL);
COMMIT;

-- ----------------------------
-- Table structure for fase_modulo
-- ----------------------------
DROP TABLE IF EXISTS `fase_modulo`;
CREATE TABLE `fase_modulo` (
  `fase_id` bigint NOT NULL,
  `modulo_id` bigint NOT NULL,
  PRIMARY KEY (`fase_id`,`modulo_id`),
  KEY `fk_fase_modulo_modulo` (`modulo_id`),
  CONSTRAINT `fk_fase_modulo_fase` FOREIGN KEY (`fase_id`) REFERENCES `fase` (`id`),
  CONSTRAINT `fk_fase_modulo_modulo` FOREIGN KEY (`modulo_id`) REFERENCES `modulo` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Records of fase_modulo
-- ----------------------------
BEGIN;
INSERT INTO `fase_modulo` (`fase_id`, `modulo_id`) VALUES (1043, 1023);
INSERT INTO `fase_modulo` (`fase_id`, `modulo_id`) VALUES (1043, 1024);
COMMIT;

-- ----------------------------
-- Table structure for flyway_schema_history
-- ----------------------------
DROP TABLE IF EXISTS `flyway_schema_history`;
CREATE TABLE `flyway_schema_history` (
  `installed_rank` int NOT NULL,
  `version` varchar(50) DEFAULT NULL,
  `description` varchar(200) NOT NULL,
  `type` varchar(20) NOT NULL,
  `script` varchar(1000) NOT NULL,
  `checksum` int DEFAULT NULL,
  `installed_by` varchar(100) NOT NULL,
  `installed_on` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `execution_time` int NOT NULL,
  `success` tinyint(1) NOT NULL,
  PRIMARY KEY (`installed_rank`),
  KEY `flyway_schema_history_s_idx` (`success`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Records of flyway_schema_history
-- ----------------------------
BEGIN;
INSERT INTO `flyway_schema_history` (`installed_rank`, `version`, `description`, `type`, `script`, `checksum`, `installed_by`, `installed_on`, `execution_time`, `success`) VALUES (1, '1', 'crear clientes y usuarios', 'SQL', 'V1__crear_clientes_y_usuarios.sql', 1851648343, 'root', '2026-10-18 22:10:24', 263, 1);
INSERT INTO `flyway_schema_history` (`installed_rank`, `version`, `description`, `type`, `script`, `checksum`, `installed_by`, `installed_on`, `execution_time`, `success`) VALUES (2, '2', 'crear proyectos', 'SQL', 'V2__crear_proyectos.sql', -1129720720, 'root', '2026-10-18 22:10:24', 214, 1);
INSERT INTO `flyway_schema_history` (`installed_rank`, `version`, `description`, `type`, `script`, `checksum`, `installed_by`, `installed_on`, `execution_time`, `success`) VALUES (3, '3', 'crear fases', 'SQL', 'V3__crear_fases.sql', 519321026, 'root', '2026-10-18 22:10:24', 154, 1);
INSERT INTO `flyway_schema_history` (`installed_rank`, `version`, `description`, `type`, `script`, `checksum`, `installed_by`, `installed_on`, `execution_time`, `success`) VALUES (4, '4', 'crear historias', 'SQL', 'V4__crear_historias.sql', -1715923004, 'root', '2026-10-18 22:10:25', 190, 1);
INSERT INTO `flyway_schema_history` (`installed_rank`, `version`, `description`, `type`, `script`, `checksum`, `installed_by`, `installed_on`, `execution_time`, `success`) VALUES (5, '5', 'crear tickets', 'SQL', 'V5__crear_tickets.sql', -1128466909, 'root', '2026-10-18 22:10:25', 237, 1);
INSERT INTO `flyway_schema_history` (`installed_rank`, `version`, `description`, `type`, `script`, `checksum`, `installed_by`, `installed_on`, `execution_time`, `success`) VALUES (6, '6', 'crear seguimiento', 'SQL', 'V6__crear_seguimiento.sql', -1486450530, 'root', '2026-10-18 22:10:25', 123, 1);
INSERT INTO `flyway_schema_history` (`installed_rank`, `version`, `description`, `type`, `script`, `checksum`, `installed_by`, `installed_on`, `execution_time`, `success`) VALUES (7, '7', 'crear releases y portal', 'SQL', 'V7__crear_releases_y_portal.sql', 79129229, 'root', '2026-10-18 22:10:25', 137, 1);
INSERT INTO `flyway_schema_history` (`installed_rank`, `version`, `description`, `type`, `script`, `checksum`, `installed_by`, `installed_on`, `execution_time`, `success`) VALUES (8, '8', 'datos iniciales', 'SQL', 'V8__datos_iniciales.sql', -1664588411, 'root', '2026-10-18 22:10:25', 14, 1);
INSERT INTO `flyway_schema_history` (`installed_rank`, `version`, `description`, `type`, `script`, `checksum`, `installed_by`, `installed_on`, `execution_time`, `success`) VALUES (9, NULL, 'datos demo', 'SQL', 'R__datos_demo.sql', 1241794220, 'root', '2026-10-18 22:10:25', 41, 1);
INSERT INTO `flyway_schema_history` (`installed_rank`, `version`, `description`, `type`, `script`, `checksum`, `installed_by`, `installed_on`, `execution_time`, `success`) VALUES (10, NULL, 'datos demo', 'SQL', 'R__datos_demo.sql', -926675724, 'root', '2026-10-19 19:40:28', 121, 1);
INSERT INTO `flyway_schema_history` (`installed_rank`, `version`, `description`, `type`, `script`, `checksum`, `installed_by`, `installed_on`, `execution_time`, `success`) VALUES (11, '9', 'agregar giro a cliente', 'SQL', 'V9__agregar_giro_a_cliente.sql', 1949937503, 'root', '2026-10-19 19:55:53', 124, 1);
INSERT INTO `flyway_schema_history` (`installed_rank`, `version`, `description`, `type`, `script`, `checksum`, `installed_by`, `installed_on`, `execution_time`, `success`) VALUES (12, NULL, 'datos demo', 'SQL', 'R__datos_demo.sql', 506018520, 'root', '2026-10-19 19:55:53', 109, 1);
INSERT INTO `flyway_schema_history` (`installed_rank`, `version`, `description`, `type`, `script`, `checksum`, `installed_by`, `installed_on`, `execution_time`, `success`) VALUES (13, NULL, 'datos demo', 'SQL', 'R__datos_demo.sql', -596460032, 'root', '2026-10-19 20:52:03', 95, 1);
INSERT INTO `flyway_schema_history` (`installed_rank`, `version`, `description`, `type`, `script`, `checksum`, `installed_by`, `installed_on`, `execution_time`, `success`) VALUES (14, '10', 'agregar bug de reversion a fase', 'SQL', 'V10__agregar_bug_de_reversion_a_fase.sql', 1574803236, 'root', '2026-10-21 20:01:43', 145, 1);
INSERT INTO `flyway_schema_history` (`installed_rank`, `version`, `description`, `type`, `script`, `checksum`, `installed_by`, `installed_on`, `execution_time`, `success`) VALUES (15, NULL, 'datos demo', 'SQL', 'R__datos_demo.sql', -51801197, 'root', '2026-10-21 20:27:41', 77, 1);
INSERT INTO `flyway_schema_history` (`installed_rank`, `version`, `description`, `type`, `script`, `checksum`, `installed_by`, `installed_on`, `execution_time`, `success`) VALUES (16, '11', 'agregar bloqueo por ticket', 'SQL', 'V11__agregar_bloqueo_por_ticket.sql', -1362115940, 'root', '2026-10-21 20:36:52', 151, 1);
INSERT INTO `flyway_schema_history` (`installed_rank`, `version`, `description`, `type`, `script`, `checksum`, `installed_by`, `installed_on`, `execution_time`, `success`) VALUES (17, NULL, 'datos demo', 'SQL', 'R__datos_demo.sql', -806607781, 'root', '2026-10-21 20:37:27', 78, 1);
INSERT INTO `flyway_schema_history` (`installed_rank`, `version`, `description`, `type`, `script`, `checksum`, `installed_by`, `installed_on`, `execution_time`, `success`) VALUES (18, NULL, 'datos demo', 'SQL', 'R__datos_demo.sql', -1421909066, 'root', '2026-10-21 20:52:16', 149, 1);
INSERT INTO `flyway_schema_history` (`installed_rank`, `version`, `description`, `type`, `script`, `checksum`, `installed_by`, `installed_on`, `execution_time`, `success`) VALUES (19, '12', 'agregar envio de notas al release', 'SQL', 'V12__agregar_envio_de_notas_al_release.sql', -1258608304, 'root', '2026-10-21 21:00:07', 53, 1);
INSERT INTO `flyway_schema_history` (`installed_rank`, `version`, `description`, `type`, `script`, `checksum`, `installed_by`, `installed_on`, `execution_time`, `success`) VALUES (20, '13', 'agregar conflictos al pull request', 'SQL', 'V13__agregar_conflictos_al_pull_request.sql', -1533480825, 'root', '2026-10-22 19:48:09', 40, 1);
INSERT INTO `flyway_schema_history` (`installed_rank`, `version`, `description`, `type`, `script`, `checksum`, `installed_by`, `installed_on`, `execution_time`, `success`) VALUES (21, NULL, 'datos demo', 'SQL', 'R__datos_demo.sql', 53297053, 'root', '2026-10-22 19:48:09', 157, 1);
INSERT INTO `flyway_schema_history` (`installed_rank`, `version`, `description`, `type`, `script`, `checksum`, `installed_by`, `installed_on`, `execution_time`, `success`) VALUES (22, '14', 'agregar tipo de documento al ticket', 'SQL', 'V14__agregar_tipo_de_documento_al_ticket.sql', -1425469899, 'root', '2026-10-22 20:00:43', 52, 1);
INSERT INTO `flyway_schema_history` (`installed_rank`, `version`, `description`, `type`, `script`, `checksum`, `installed_by`, `installed_on`, `execution_time`, `success`) VALUES (23, NULL, 'datos demo', 'SQL', 'R__datos_demo.sql', -1468050051, 'root', '2026-10-22 20:04:56', 131, 1);
INSERT INTO `flyway_schema_history` (`installed_rank`, `version`, `description`, `type`, `script`, `checksum`, `installed_by`, `installed_on`, `execution_time`, `success`) VALUES (24, NULL, 'datos demo', 'SQL', 'R__datos_demo.sql', 1507489984, 'root', '2026-10-22 20:18:10', 140, 1);
INSERT INTO `flyway_schema_history` (`installed_rank`, `version`, `description`, `type`, `script`, `checksum`, `installed_by`, `installed_on`, `execution_time`, `success`) VALUES (25, '15', 'agregar responsable al modulo', 'SQL', 'V15__agregar_responsable_al_modulo.sql', -1338280146, 'root', '2026-10-22 20:23:44', 103, 1);
INSERT INTO `flyway_schema_history` (`installed_rank`, `version`, `description`, `type`, `script`, `checksum`, `installed_by`, `installed_on`, `execution_time`, `success`) VALUES (26, NULL, 'datos demo', 'SQL', 'R__datos_demo.sql', -503863781, 'root', '2026-10-22 20:23:44', 98, 1);
INSERT INTO `flyway_schema_history` (`installed_rank`, `version`, `description`, `type`, `script`, `checksum`, `installed_by`, `installed_on`, `execution_time`, `success`) VALUES (27, NULL, 'datos demo', 'SQL', 'R__datos_demo.sql', 951304035, 'root', '2026-10-22 20:42:06', 111, 1);
INSERT INTO `flyway_schema_history` (`installed_rank`, `version`, `description`, `type`, `script`, `checksum`, `installed_by`, `installed_on`, `execution_time`, `success`) VALUES (28, '16', 'agregar origen de falla al ticket', 'SQL', 'V16__agregar_origen_de_falla_al_ticket.sql', 1414254976, 'root', '2026-10-23 08:24:28', 192, 1);
INSERT INTO `flyway_schema_history` (`installed_rank`, `version`, `description`, `type`, `script`, `checksum`, `installed_by`, `installed_on`, `execution_time`, `success`) VALUES (29, '17', 'reglas de negocio en la historia', 'SQL', 'V17__reglas_de_negocio_en_la_historia.sql', 625627202, 'root', '2026-10-23 19:58:09', 237, 1);
INSERT INTO `flyway_schema_history` (`installed_rank`, `version`, `description`, `type`, `script`, `checksum`, `installed_by`, `installed_on`, `execution_time`, `success`) VALUES (30, NULL, 'datos demo', 'SQL', 'R__datos_demo.sql', -920390013, 'root', '2026-10-23 19:58:09', 99, 1);
INSERT INTO `flyway_schema_history` (`installed_rank`, `version`, `description`, `type`, `script`, `checksum`, `installed_by`, `installed_on`, `execution_time`, `success`) VALUES (31, '18', 'contacto del cliente opcional', 'SQL', 'V18__contacto_del_cliente_opcional.sql', 6388257, 'root', '2026-10-24 19:57:05', 78, 1);
INSERT INTO `flyway_schema_history` (`installed_rank`, `version`, `description`, `type`, `script`, `checksum`, `installed_by`, `installed_on`, `execution_time`, `success`) VALUES (32, '19', 'clientes solo para el admin', 'SQL', 'V19__clientes_solo_para_el_admin.sql', 1602966128, 'root', '2026-10-24 20:02:47', 17, 1);
INSERT INTO `flyway_schema_history` (`installed_rank`, `version`, `description`, `type`, `script`, `checksum`, `installed_by`, `installed_on`, `execution_time`, `success`) VALUES (33, '20', 'proyecto interno sin tipo', 'SQL', 'V20__proyecto_interno_sin_tipo.sql', -1648629609, 'root', '2026-10-24 20:17:52', 104, 1);
INSERT INTO `flyway_schema_history` (`installed_rank`, `version`, `description`, `type`, `script`, `checksum`, `installed_by`, `installed_on`, `execution_time`, `success`) VALUES (34, '21', 'ajustar permisos por rol', 'SQL', 'V21__ajustar_permisos_por_rol.sql', -2010029402, 'root', '2026-10-25 14:04:44', 15, 1);
INSERT INTO `flyway_schema_history` (`installed_rank`, `version`, `description`, `type`, `script`, `checksum`, `installed_by`, `installed_on`, `execution_time`, `success`) VALUES (35, '22', 'quitar tecn invs doct', 'SQL', 'V22__quitar_tecn_invs_doct.sql', -1220471829, 'root', '2026-10-25 14:04:45', 312, 1);
INSERT INTO `flyway_schema_history` (`installed_rank`, `version`, `description`, `type`, `script`, `checksum`, `installed_by`, `installed_on`, `execution_time`, `success`) VALUES (36, '23', 'qa por historia y pr por ticket', 'SQL', 'V23__qa_por_historia_y_pr_por_ticket.sql', -863839050, 'root', '2026-10-25 15:29:32', 17, 1);
INSERT INTO `flyway_schema_history` (`installed_rank`, `version`, `description`, `type`, `script`, `checksum`, `installed_by`, `installed_on`, `execution_time`, `success`) VALUES (37, '24', 'lider marca historia lista', 'SQL', 'V24__lider_marca_historia_lista.sql', 98104849, 'root', '2026-10-25 19:51:42', 15, 1);
INSERT INTO `flyway_schema_history` (`installed_rank`, `version`, `description`, `type`, `script`, `checksum`, `installed_by`, `installed_on`, `execution_time`, `success`) VALUES (38, '25', 'historia en planeacion y lista para desarrollo', 'SQL', 'V25__historia_en_planeacion_y_lista_para_desarrollo.sql', -1667029068, 'root', '2026-10-25 21:04:05', 31, 1);
INSERT INTO `flyway_schema_history` (`installed_rank`, `version`, `description`, `type`, `script`, `checksum`, `installed_by`, `installed_on`, `execution_time`, `success`) VALUES (39, '26', 'sin tester valida el analista', 'SQL', 'V26__sin_tester_valida_el_analista.sql', -426914425, 'root', '2026-10-25 21:23:55', 23, 1);
INSERT INTO `flyway_schema_history` (`installed_rank`, `version`, `description`, `type`, `script`, `checksum`, `installed_by`, `installed_on`, `execution_time`, `success`) VALUES (40, '27', 'historia lista con actividad pasa sola', 'SQL', 'V27__historia_lista_con_actividad_pasa_sola.sql', -928792553, 'root', '2026-10-25 21:56:50', 29, 1);
INSERT INTO `flyway_schema_history` (`installed_rank`, `version`, `description`, `type`, `script`, `checksum`, `installed_by`, `installed_on`, `execution_time`, `success`) VALUES (41, '28', 'quitar reglas de negocio', 'SQL', 'V28__quitar_reglas_de_negocio.sql', -683120177, 'root', '2026-10-28 21:40:55', 54, 1);
INSERT INTO `flyway_schema_history` (`installed_rank`, `version`, `description`, `type`, `script`, `checksum`, `installed_by`, `installed_on`, `execution_time`, `success`) VALUES (42, '29', 'diseno de la fase', 'SQL', 'V29__diseno_de_la_fase.sql', -716763980, 'root', '2026-10-29 20:11:16', 81, 1);
INSERT INTO `flyway_schema_history` (`installed_rank`, `version`, `description`, `type`, `script`, `checksum`, `installed_by`, `installed_on`, `execution_time`, `success`) VALUES (43, '30', 'usuarios del sistema', 'SQL', 'V30__usuarios_del_sistema.sql', 915765559, 'root', '2026-10-29 21:13:54', 86, 1);
COMMIT;

-- ----------------------------
-- Table structure for historia
-- ----------------------------
DROP TABLE IF EXISTS `historia`;
CREATE TABLE `historia` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `proyecto_id` bigint NOT NULL,
  `fase_id` bigint NOT NULL,
  `numero` int NOT NULL,
  `titulo` varchar(150) NOT NULL,
  `como` varchar(200) DEFAULT NULL,
  `quiero` varchar(300) DEFAULT NULL,
  `para` varchar(300) DEFAULT NULL,
  `estado` varchar(20) NOT NULL DEFAULT 'BORRADOR',
  `prioridad` varchar(10) NOT NULL DEFAULT 'MEDIA',
  `puntos` int DEFAULT NULL,
  `analista_id` bigint NOT NULL,
  `figma_url` varchar(300) DEFAULT NULL,
  `diseno_aprobado` tinyint(1) NOT NULL DEFAULT '0',
  `diseno_aprobado_por` bigint DEFAULT NULL,
  `diseno_aprobado_en` datetime DEFAULT NULL,
  `creado_por` bigint NOT NULL,
  `creado_en` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `actualizado_en` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_historia_numero` (`proyecto_id`,`numero`),
  KEY `fk_historia_fase` (`fase_id`),
  KEY `fk_historia_analista` (`analista_id`),
  KEY `fk_historia_diseno_por` (`diseno_aprobado_por`),
  KEY `fk_historia_creado_por` (`creado_por`),
  CONSTRAINT `fk_historia_analista` FOREIGN KEY (`analista_id`) REFERENCES `usuario` (`id`),
  CONSTRAINT `fk_historia_creado_por` FOREIGN KEY (`creado_por`) REFERENCES `usuario` (`id`),
  CONSTRAINT `fk_historia_diseno_por` FOREIGN KEY (`diseno_aprobado_por`) REFERENCES `usuario` (`id`),
  CONSTRAINT `fk_historia_fase` FOREIGN KEY (`fase_id`) REFERENCES `fase` (`id`),
  CONSTRAINT `fk_historia_proyecto` FOREIGN KEY (`proyecto_id`) REFERENCES `proyecto` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1068 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Records of historia
-- ----------------------------
BEGIN;
INSERT INTO `historia` (`id`, `proyecto_id`, `fase_id`, `numero`, `titulo`, `como`, `quiero`, `para`, `estado`, `prioridad`, `puntos`, `analista_id`, `figma_url`, `diseno_aprobado`, `diseno_aprobado_por`, `diseno_aprobado_en`, `creado_por`, `creado_en`, `actualizado_en`) VALUES (1058, 1016, 1043, 1, 'Administrador: iniciar sesión y su vista', 'Administrador del sistema', 'entrar con mi cuenta a mi vista de Administrador', 'administrar las escuelas del sistema', 'EN_DESARROLLO', 'ALTA', 3, 1013, NULL, 0, NULL, NULL, 1001, '2026-10-28 21:58:53', '2026-10-29 21:28:28');
INSERT INTO `historia` (`id`, `proyecto_id`, `fase_id`, `numero`, `titulo`, `como`, `quiero`, `para`, `estado`, `prioridad`, `puntos`, `analista_id`, `figma_url`, `diseno_aprobado`, `diseno_aprobado_por`, `diseno_aprobado_en`, `creado_por`, `creado_en`, `actualizado_en`) VALUES (1059, 1016, 1043, 2, 'Escuela: iniciar sesión y su vista', 'usuario de una escuela', 'entrar con mi cuenta a la vista de mi escuela', 'registrar la información de mi escuela', 'BORRADOR', 'ALTA', 3, 1013, NULL, 0, NULL, NULL, 1001, '2026-10-28 21:58:53', '2026-10-28 22:00:52');
INSERT INTO `historia` (`id`, `proyecto_id`, `fase_id`, `numero`, `titulo`, `como`, `quiero`, `para`, `estado`, `prioridad`, `puntos`, `analista_id`, `figma_url`, `diseno_aprobado`, `diseno_aprobado_por`, `diseno_aprobado_en`, `creado_por`, `creado_en`, `actualizado_en`) VALUES (1060, 1016, 1043, 3, 'Registrar escuela', 'Administrador del sistema', 'dar de alta una escuela', 'que pueda usar el sistema', 'BORRADOR', 'ALTA', 3, 1013, NULL, 0, NULL, NULL, 1001, '2026-10-28 22:02:23', '2026-10-28 22:03:05');
INSERT INTO `historia` (`id`, `proyecto_id`, `fase_id`, `numero`, `titulo`, `como`, `quiero`, `para`, `estado`, `prioridad`, `puntos`, `analista_id`, `figma_url`, `diseno_aprobado`, `diseno_aprobado_por`, `diseno_aprobado_en`, `creado_por`, `creado_en`, `actualizado_en`) VALUES (1061, 1016, 1043, 4, 'Registrar usuarios de una escuela', 'Administrador del sistema', 'crear usuarios para una escuela', 'que su personal pueda entrar al sistema', 'BORRADOR', 'ALTA', 3, 1013, NULL, 0, NULL, NULL, 1001, '2026-10-28 22:02:23', '2026-10-28 22:03:05');
INSERT INTO `historia` (`id`, `proyecto_id`, `fase_id`, `numero`, `titulo`, `como`, `quiero`, `para`, `estado`, `prioridad`, `puntos`, `analista_id`, `figma_url`, `diseno_aprobado`, `diseno_aprobado_por`, `diseno_aprobado_en`, `creado_por`, `creado_en`, `actualizado_en`) VALUES (1062, 1016, 1043, 5, 'Registrar periodo', 'usuario de una escuela', 'registrar los periodos escolares', 'organizar a mis alumnos por ciclo', 'BORRADOR', 'ALTA', 2, 1013, NULL, 0, NULL, NULL, 1001, '2026-10-28 22:02:23', '2026-10-28 22:03:05');
INSERT INTO `historia` (`id`, `proyecto_id`, `fase_id`, `numero`, `titulo`, `como`, `quiero`, `para`, `estado`, `prioridad`, `puntos`, `analista_id`, `figma_url`, `diseno_aprobado`, `diseno_aprobado_por`, `diseno_aprobado_en`, `creado_por`, `creado_en`, `actualizado_en`) VALUES (1063, 1016, 1043, 6, 'Registrar generación', 'usuario de una escuela', 'dar de alta una generación en un periodo', 'agrupar a los alumnos que entran juntos', 'BORRADOR', 'ALTA', 3, 1013, NULL, 0, NULL, NULL, 1001, '2026-10-28 22:02:23', '2026-10-28 22:03:05');
INSERT INTO `historia` (`id`, `proyecto_id`, `fase_id`, `numero`, `titulo`, `como`, `quiero`, `para`, `estado`, `prioridad`, `puntos`, `analista_id`, `figma_url`, `diseno_aprobado`, `diseno_aprobado_por`, `diseno_aprobado_en`, `creado_por`, `creado_en`, `actualizado_en`) VALUES (1064, 1016, 1043, 7, 'Registrar materias', 'usuario de una escuela', 'dar de alta las materias de mi escuela', 'saber qué se imparte en cada trimestre', 'BORRADOR', 'MEDIA', 2, 1013, NULL, 0, NULL, NULL, 1001, '2026-10-28 22:02:23', '2026-10-28 22:03:05');
INSERT INTO `historia` (`id`, `proyecto_id`, `fase_id`, `numero`, `titulo`, `como`, `quiero`, `para`, `estado`, `prioridad`, `puntos`, `analista_id`, `figma_url`, `diseno_aprobado`, `diseno_aprobado_por`, `diseno_aprobado_en`, `creado_por`, `creado_en`, `actualizado_en`) VALUES (1065, 1016, 1043, 8, 'Registrar alumnos', 'usuario de una escuela', 'registrar a los alumnos en una generación', 'tener el padrón de mi escuela', 'BORRADOR', 'ALTA', 5, 1013, NULL, 0, NULL, NULL, 1001, '2026-10-28 22:02:23', '2026-10-28 22:03:05');
INSERT INTO `historia` (`id`, `proyecto_id`, `fase_id`, `numero`, `titulo`, `como`, `quiero`, `para`, `estado`, `prioridad`, `puntos`, `analista_id`, `figma_url`, `diseno_aprobado`, `diseno_aprobado_por`, `diseno_aprobado_en`, `creado_por`, `creado_en`, `actualizado_en`) VALUES (1066, 1016, 1043, 9, 'Registrar trimestres', 'usuario de una escuela', 'registrar los trimestres de mi escuela', 'ubicar en qué trimestre van mis materias y alumnos', 'LISTA', 'ALTA', 2, 1013, NULL, 0, NULL, NULL, 1001, '2026-10-28 22:04:38', '2026-10-29 21:12:51');
COMMIT;

-- ----------------------------
-- Table structure for historia_criterio
-- ----------------------------
DROP TABLE IF EXISTS `historia_criterio`;
CREATE TABLE `historia_criterio` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `historia_id` bigint NOT NULL,
  `descripcion` varchar(300) NOT NULL,
  `verificado` tinyint(1) NOT NULL DEFAULT '0',
  `orden` int NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `fk_criterio_historia` (`historia_id`),
  CONSTRAINT `fk_criterio_historia` FOREIGN KEY (`historia_id`) REFERENCES `historia` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1137 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Records of historia_criterio
-- ----------------------------
BEGIN;
INSERT INTO `historia_criterio` (`id`, `historia_id`, `descripcion`, `verificado`, `orden`) VALUES (1094, 1059, 'Entro con correo y contraseña; si están mal veo \"Correo o contraseña incorrectos\"', 0, 1);
INSERT INTO `historia_criterio` (`id`, `historia_id`, `descripcion`, `verificado`, `orden`) VALUES (1095, 1059, 'Si mi usuario o mi escuela están desactivados no puedo entrar', 0, 2);
INSERT INTO `historia_criterio` (`id`, `historia_id`, `descripcion`, `verificado`, `orden`) VALUES (1096, 1059, 'Al entrar llego a la vista de mi escuela y arriba veo su nombre', 0, 3);
INSERT INTO `historia_criterio` (`id`, `historia_id`, `descripcion`, `verificado`, `orden`) VALUES (1097, 1059, 'Solo veo datos de mi escuela', 0, 4);
INSERT INTO `historia_criterio` (`id`, `historia_id`, `descripcion`, `verificado`, `orden`) VALUES (1098, 1059, 'Al cerrar sesión vuelvo a la pantalla de inicio', 0, 5);
INSERT INTO `historia_criterio` (`id`, `historia_id`, `descripcion`, `verificado`, `orden`) VALUES (1099, 1060, 'Pido nombre y clave CCT (obligatorios), dirección, teléfono y correo de contacto', 0, 1);
INSERT INTO `historia_criterio` (`id`, `historia_id`, `descripcion`, `verificado`, `orden`) VALUES (1100, 1060, 'No se puede repetir la clave CCT', 0, 2);
INSERT INTO `historia_criterio` (`id`, `historia_id`, `descripcion`, `verificado`, `orden`) VALUES (1101, 1060, 'Veo la lista de escuelas y la busco por nombre o clave', 0, 3);
INSERT INTO `historia_criterio` (`id`, `historia_id`, `descripcion`, `verificado`, `orden`) VALUES (1102, 1060, 'Puedo editar una escuela y desactivarla sin borrarla', 0, 4);
INSERT INTO `historia_criterio` (`id`, `historia_id`, `descripcion`, `verificado`, `orden`) VALUES (1103, 1061, 'Pido escuela, nombre completo, correo y contraseña inicial', 0, 1);
INSERT INTO `historia_criterio` (`id`, `historia_id`, `descripcion`, `verificado`, `orden`) VALUES (1104, 1061, 'Solo puedo elegir escuelas activas', 0, 2);
INSERT INTO `historia_criterio` (`id`, `historia_id`, `descripcion`, `verificado`, `orden`) VALUES (1105, 1061, 'El correo no se puede repetir', 0, 3);
INSERT INTO `historia_criterio` (`id`, `historia_id`, `descripcion`, `verificado`, `orden`) VALUES (1106, 1061, 'Veo los usuarios filtrados por escuela', 0, 4);
INSERT INTO `historia_criterio` (`id`, `historia_id`, `descripcion`, `verificado`, `orden`) VALUES (1107, 1061, 'Puedo desactivar un usuario y reiniciar su contraseña', 0, 5);
INSERT INTO `historia_criterio` (`id`, `historia_id`, `descripcion`, `verificado`, `orden`) VALUES (1108, 1062, 'Pido nombre (ej. \"2026-2027\"), fecha de inicio y fecha de fin', 0, 1);
INSERT INTO `historia_criterio` (`id`, `historia_id`, `descripcion`, `verificado`, `orden`) VALUES (1109, 1062, 'La fecha de fin debe ser posterior a la de inicio', 0, 2);
INSERT INTO `historia_criterio` (`id`, `historia_id`, `descripcion`, `verificado`, `orden`) VALUES (1110, 1062, 'No se puede repetir el nombre en mi escuela', 0, 3);
INSERT INTO `historia_criterio` (`id`, `historia_id`, `descripcion`, `verificado`, `orden`) VALUES (1111, 1062, 'Puedo marcar un solo periodo como actual', 0, 4);
INSERT INTO `historia_criterio` (`id`, `historia_id`, `descripcion`, `verificado`, `orden`) VALUES (1112, 1063, 'Pido nombre (ej. \"Generación 2026-2029\") y periodo de ingreso', 0, 1);
INSERT INTO `historia_criterio` (`id`, `historia_id`, `descripcion`, `verificado`, `orden`) VALUES (1113, 1063, 'Solo puedo elegir periodos de mi escuela', 0, 2);
INSERT INTO `historia_criterio` (`id`, `historia_id`, `descripcion`, `verificado`, `orden`) VALUES (1114, 1063, 'No se puede repetir el nombre en mi escuela', 0, 3);
INSERT INTO `historia_criterio` (`id`, `historia_id`, `descripcion`, `verificado`, `orden`) VALUES (1115, 1063, 'Veo cuántos alumnos tiene cada generación', 0, 4);
INSERT INTO `historia_criterio` (`id`, `historia_id`, `descripcion`, `verificado`, `orden`) VALUES (1116, 1064, 'Pido clave, nombre y trimestre (del catálogo de trimestres)', 0, 1);
INSERT INTO `historia_criterio` (`id`, `historia_id`, `descripcion`, `verificado`, `orden`) VALUES (1117, 1064, 'No se puede repetir la clave en mi escuela', 0, 2);
INSERT INTO `historia_criterio` (`id`, `historia_id`, `descripcion`, `verificado`, `orden`) VALUES (1118, 1064, 'Veo las materias filtradas por trimestre', 0, 3);
INSERT INTO `historia_criterio` (`id`, `historia_id`, `descripcion`, `verificado`, `orden`) VALUES (1119, 1064, 'Puedo editar y desactivar una materia', 0, 4);
INSERT INTO `historia_criterio` (`id`, `historia_id`, `descripcion`, `verificado`, `orden`) VALUES (1120, 1065, 'Pido nombre, apellidos, CURP, fecha de nacimiento, generación y trimestre actual', 0, 1);
INSERT INTO `historia_criterio` (`id`, `historia_id`, `descripcion`, `verificado`, `orden`) VALUES (1121, 1065, 'La CURP debe ser válida y no repetirse', 0, 2);
INSERT INTO `historia_criterio` (`id`, `historia_id`, `descripcion`, `verificado`, `orden`) VALUES (1122, 1065, 'Solo puedo elegir generaciones de mi escuela', 0, 3);
INSERT INTO `historia_criterio` (`id`, `historia_id`, `descripcion`, `verificado`, `orden`) VALUES (1123, 1065, 'Veo los alumnos filtrados por generación y los busco por nombre o CURP', 0, 4);
INSERT INTO `historia_criterio` (`id`, `historia_id`, `descripcion`, `verificado`, `orden`) VALUES (1124, 1065, 'Puedo editar a un alumno y darlo de baja sin borrarlo', 0, 5);
INSERT INTO `historia_criterio` (`id`, `historia_id`, `descripcion`, `verificado`, `orden`) VALUES (1125, 1066, 'Pido número (1, 2, 3...) y nombre (ej. \"Primer trimestre\")', 0, 1);
INSERT INTO `historia_criterio` (`id`, `historia_id`, `descripcion`, `verificado`, `orden`) VALUES (1126, 1066, 'No se puede repetir el número en mi escuela', 0, 2);
INSERT INTO `historia_criterio` (`id`, `historia_id`, `descripcion`, `verificado`, `orden`) VALUES (1127, 1066, 'Veo los trimestres ordenados por número', 0, 3);
INSERT INTO `historia_criterio` (`id`, `historia_id`, `descripcion`, `verificado`, `orden`) VALUES (1128, 1066, 'Puedo editar y desactivar un trimestre', 0, 4);
INSERT INTO `historia_criterio` (`id`, `historia_id`, `descripcion`, `verificado`, `orden`) VALUES (1133, 1058, 'Entro con correo y contraseña; si están mal veo \"Correo o contraseña incorrectos\"', 0, 1);
INSERT INTO `historia_criterio` (`id`, `historia_id`, `descripcion`, `verificado`, `orden`) VALUES (1134, 1058, 'Al entrar llego a la vista del Administrador', 0, 2);
INSERT INTO `historia_criterio` (`id`, `historia_id`, `descripcion`, `verificado`, `orden`) VALUES (1135, 1058, 'El menú tiene: Inicio, Escuelas, Usuarios y Cerrar sesión', 0, 3);
INSERT INTO `historia_criterio` (`id`, `historia_id`, `descripcion`, `verificado`, `orden`) VALUES (1136, 1058, 'Al cerrar sesión vuelvo a la pantalla de inicio', 0, 4);
COMMIT;

-- ----------------------------
-- Table structure for historia_modulo
-- ----------------------------
DROP TABLE IF EXISTS `historia_modulo`;
CREATE TABLE `historia_modulo` (
  `historia_id` bigint NOT NULL,
  `modulo_id` bigint NOT NULL,
  PRIMARY KEY (`historia_id`,`modulo_id`),
  KEY `fk_historia_modulo_modulo` (`modulo_id`),
  CONSTRAINT `fk_historia_modulo_historia` FOREIGN KEY (`historia_id`) REFERENCES `historia` (`id`),
  CONSTRAINT `fk_historia_modulo_modulo` FOREIGN KEY (`modulo_id`) REFERENCES `modulo` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Records of historia_modulo
-- ----------------------------
BEGIN;
INSERT INTO `historia_modulo` (`historia_id`, `modulo_id`) VALUES (1058, 1023);
INSERT INTO `historia_modulo` (`historia_id`, `modulo_id`) VALUES (1059, 1023);
INSERT INTO `historia_modulo` (`historia_id`, `modulo_id`) VALUES (1060, 1023);
INSERT INTO `historia_modulo` (`historia_id`, `modulo_id`) VALUES (1061, 1023);
INSERT INTO `historia_modulo` (`historia_id`, `modulo_id`) VALUES (1062, 1023);
INSERT INTO `historia_modulo` (`historia_id`, `modulo_id`) VALUES (1063, 1023);
INSERT INTO `historia_modulo` (`historia_id`, `modulo_id`) VALUES (1064, 1023);
INSERT INTO `historia_modulo` (`historia_id`, `modulo_id`) VALUES (1065, 1023);
INSERT INTO `historia_modulo` (`historia_id`, `modulo_id`) VALUES (1066, 1023);
INSERT INTO `historia_modulo` (`historia_id`, `modulo_id`) VALUES (1058, 1024);
INSERT INTO `historia_modulo` (`historia_id`, `modulo_id`) VALUES (1059, 1024);
INSERT INTO `historia_modulo` (`historia_id`, `modulo_id`) VALUES (1060, 1024);
INSERT INTO `historia_modulo` (`historia_id`, `modulo_id`) VALUES (1061, 1024);
INSERT INTO `historia_modulo` (`historia_id`, `modulo_id`) VALUES (1062, 1024);
INSERT INTO `historia_modulo` (`historia_id`, `modulo_id`) VALUES (1063, 1024);
INSERT INTO `historia_modulo` (`historia_id`, `modulo_id`) VALUES (1064, 1024);
INSERT INTO `historia_modulo` (`historia_id`, `modulo_id`) VALUES (1065, 1024);
INSERT INTO `historia_modulo` (`historia_id`, `modulo_id`) VALUES (1066, 1024);
COMMIT;

-- ----------------------------
-- Table structure for historial_estado
-- ----------------------------
DROP TABLE IF EXISTS `historial_estado`;
CREATE TABLE `historial_estado` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `entidad` varchar(10) NOT NULL,
  `entidad_id` bigint NOT NULL,
  `estado_anterior` varchar(25) DEFAULT NULL,
  `estado_nuevo` varchar(25) NOT NULL,
  `motivo` varchar(1000) DEFAULT NULL,
  `automatico` tinyint(1) NOT NULL DEFAULT '0',
  `usuario_id` bigint DEFAULT NULL,
  `creado_en` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_historial_entidad` (`entidad`,`entidad_id`),
  KEY `fk_historial_usuario` (`usuario_id`),
  CONSTRAINT `fk_historial_usuario` FOREIGN KEY (`usuario_id`) REFERENCES `usuario` (`id`),
  CONSTRAINT `chk_historial_entidad` CHECK ((`entidad` in (_utf8mb4'FASE',_utf8mb4'HISTORIA',_utf8mb4'TICKET')))
) ENGINE=InnoDB AUTO_INCREMENT=1500 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Records of historial_estado
-- ----------------------------
BEGIN;
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1462, 'FASE', 1043, NULL, 'PLANEADA', NULL, 0, 1001, '2026-10-28 21:51:28');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1466, 'HISTORIA', 1058, NULL, 'BORRADOR', NULL, 0, 1001, '2026-10-28 21:58:53');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1467, 'HISTORIA', 1059, NULL, 'BORRADOR', NULL, 0, 1001, '2026-10-28 21:58:53');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1469, 'HISTORIA', 1060, NULL, 'BORRADOR', NULL, 0, 1001, '2026-10-28 22:02:23');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1470, 'HISTORIA', 1061, NULL, 'BORRADOR', NULL, 0, 1001, '2026-10-28 22:02:23');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1472, 'HISTORIA', 1062, NULL, 'BORRADOR', NULL, 0, 1001, '2026-10-28 22:02:23');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1473, 'HISTORIA', 1063, NULL, 'BORRADOR', NULL, 0, 1001, '2026-10-28 22:02:23');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1474, 'HISTORIA', 1064, NULL, 'BORRADOR', NULL, 0, 1001, '2026-10-28 22:02:23');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1475, 'HISTORIA', 1065, NULL, 'BORRADOR', NULL, 0, 1001, '2026-10-28 22:02:23');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1476, 'HISTORIA', 1066, NULL, 'BORRADOR', NULL, 0, 1001, '2026-10-28 22:04:38');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1477, 'HISTORIA', 1058, 'BORRADOR', 'EN_PLANEACION', NULL, 0, 1016, '2026-10-29 19:41:59');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1478, 'HISTORIA', 1066, 'BORRADOR', 'EN_PLANEACION', NULL, 0, 1016, '2026-10-29 20:36:05');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1479, 'HISTORIA', 1058, 'EN_PLANEACION', 'LISTA', NULL, 0, 1016, '2026-10-29 20:40:08');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1480, 'HISTORIA', 1066, 'EN_PLANEACION', 'LISTA', NULL, 0, 1016, '2026-10-29 21:12:51');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1483, 'TICKET', 1440, NULL, 'BORRADOR', NULL, 0, 1016, '2026-10-29 21:20:49');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1484, 'TICKET', 1440, 'BORRADOR', 'POR_HACER', NULL, 0, 1016, '2026-10-29 21:21:03');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1485, 'HISTORIA', 1058, 'LISTA', 'LISTA_DESARROLLO', 'Ya tiene una Actividad Lista', 1, 1016, '2026-10-29 21:21:03');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1486, 'HISTORIA', 1058, 'LISTA_DESARROLLO', 'EN_PLANEACION', NULL, 0, 1016, '2026-10-29 21:21:08');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1487, 'HISTORIA', 1058, 'EN_PLANEACION', 'LISTA', NULL, 0, 1016, '2026-10-29 21:21:09');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1488, 'HISTORIA', 1058, 'LISTA', 'EN_PLANEACION', NULL, 0, 1016, '2026-10-29 21:22:04');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1489, 'HISTORIA', 1058, 'EN_PLANEACION', 'LISTA', NULL, 0, 1016, '2026-10-29 21:22:14');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1490, 'HISTORIA', 1058, 'LISTA', 'EN_PLANEACION', NULL, 0, 1016, '2026-10-29 21:25:28');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1491, 'HISTORIA', 1058, 'EN_PLANEACION', 'LISTA', NULL, 0, 1016, '2026-10-29 21:25:30');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1492, 'HISTORIA', 1058, 'LISTA', 'LISTA_DESARROLLO', 'Ya tiene una Actividad Lista', 1, 1016, '2026-10-29 21:25:30');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1493, 'HISTORIA', 1058, 'LISTA_DESARROLLO', 'EN_PLANEACION', NULL, 0, 1016, '2026-10-29 21:25:34');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1494, 'HISTORIA', 1058, 'EN_PLANEACION', 'LISTA', NULL, 0, 1016, '2026-10-29 21:25:52');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1495, 'HISTORIA', 1058, 'LISTA', 'LISTA_DESARROLLO', 'Ya tiene una Actividad Lista', 1, 1016, '2026-10-29 21:25:52');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1496, 'FASE', 1043, 'PLANEADA', 'EN_CURSO', NULL, 0, 1016, '2026-10-29 21:28:28');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1497, 'HISTORIA', 1058, 'LISTA_DESARROLLO', 'EN_DESARROLLO', 'Se inició con la fase', 1, 1016, '2026-10-29 21:28:28');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1498, 'TICKET', 1440, 'POR_HACER', 'EN_PROGRESO', NULL, 0, 1001, '2026-10-29 21:28:49');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1499, 'TICKET', 1440, 'EN_PROGRESO', 'EN_REVISION', 'bhjbjbhj', 0, 1001, '2026-10-29 21:32:32');
COMMIT;

-- ----------------------------
-- Table structure for modulo
-- ----------------------------
DROP TABLE IF EXISTS `modulo`;
CREATE TABLE `modulo` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `proyecto_id` bigint NOT NULL,
  `tipo` varchar(20) NOT NULL,
  `nombre` varchar(60) NOT NULL,
  `tecnologia` varchar(80) DEFAULT NULL,
  `repositorio` varchar(200) DEFAULT NULL,
  `rama_base` varchar(60) NOT NULL DEFAULT 'develop',
  `responsable_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_modulo_nombre` (`proyecto_id`,`nombre`),
  KEY `fk_modulo_responsable` (`responsable_id`),
  CONSTRAINT `fk_modulo_proyecto` FOREIGN KEY (`proyecto_id`) REFERENCES `proyecto` (`id`),
  CONSTRAINT `fk_modulo_responsable` FOREIGN KEY (`responsable_id`) REFERENCES `usuario` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1025 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Records of modulo
-- ----------------------------
BEGIN;
INSERT INTO `modulo` (`id`, `proyecto_id`, `tipo`, `nombre`, `tecnologia`, `repositorio`, `rama_base`, `responsable_id`) VALUES (1023, 1016, 'API', 'API escolar', NULL, NULL, 'develop', NULL);
INSERT INTO `modulo` (`id`, `proyecto_id`, `tipo`, `nombre`, `tecnologia`, `repositorio`, `rama_base`, `responsable_id`) VALUES (1024, 1016, 'WEB', 'Web escolar', NULL, NULL, 'develop', NULL);
COMMIT;

-- ----------------------------
-- Table structure for permiso
-- ----------------------------
DROP TABLE IF EXISTS `permiso`;
CREATE TABLE `permiso` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `codigo` varchar(60) NOT NULL,
  `nombre` varchar(120) NOT NULL,
  `grupo` varchar(40) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `codigo` (`codigo`)
) ENGINE=InnoDB AUTO_INCREMENT=29 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Records of permiso
-- ----------------------------
BEGIN;
INSERT INTO `permiso` (`id`, `codigo`, `nombre`, `grupo`) VALUES (1, 'usuario.gestionar', 'Dar de alta usuarios y asignar roles', 'ADMINISTRACION');
INSERT INTO `permiso` (`id`, `codigo`, `nombre`, `grupo`) VALUES (2, 'cliente.gestionar', 'Crear y editar clientes', 'ADMINISTRACION');
INSERT INTO `permiso` (`id`, `codigo`, `nombre`, `grupo`) VALUES (3, 'proyecto.crear', 'Crear proyectos', 'PROYECTO');
INSERT INTO `permiso` (`id`, `codigo`, `nombre`, `grupo`) VALUES (4, 'proyecto.editar', 'Editar proyecto y sus repositorios', 'PROYECTO');
INSERT INTO `permiso` (`id`, `codigo`, `nombre`, `grupo`) VALUES (5, 'fase.crear', 'Crear fases', 'FASE');
INSERT INTO `permiso` (`id`, `codigo`, `nombre`, `grupo`) VALUES (6, 'fase.gestionar', 'Iniciar, pausar y cancelar fases', 'FASE');
INSERT INTO `permiso` (`id`, `codigo`, `nombre`, `grupo`) VALUES (7, 'fase.reabrir', 'Reabrir una fase cancelada', 'FASE');
INSERT INTO `permiso` (`id`, `codigo`, `nombre`, `grupo`) VALUES (8, 'fase.marcar_criterio', 'Marcar criterios manuales de cierre', 'FASE');
INSERT INTO `permiso` (`id`, `codigo`, `nombre`, `grupo`) VALUES (9, 'historia.crear', 'Crear historias de usuario', 'HISTORIA');
INSERT INTO `permiso` (`id`, `codigo`, `nombre`, `grupo`) VALUES (10, 'historia.marcar_lista', 'Marcar historia como Lista', 'HISTORIA');
INSERT INTO `permiso` (`id`, `codigo`, `nombre`, `grupo`) VALUES (11, 'historia.iniciar', 'Iniciar historia', 'HISTORIA');
INSERT INTO `permiso` (`id`, `codigo`, `nombre`, `grupo`) VALUES (12, 'historia.aceptar', 'Aceptar o devolver historia', 'HISTORIA');
INSERT INTO `permiso` (`id`, `codigo`, `nombre`, `grupo`) VALUES (13, 'historia.descartar', 'Descartar historia', 'HISTORIA');
INSERT INTO `permiso` (`id`, `codigo`, `nombre`, `grupo`) VALUES (14, 'historia.integrar', 'Integrar historia a develop', 'HISTORIA');
INSERT INTO `permiso` (`id`, `codigo`, `nombre`, `grupo`) VALUES (15, 'ticket.crear', 'Crear tickets de trabajo', 'TICKET');
INSERT INTO `permiso` (`id`, `codigo`, `nombre`, `grupo`) VALUES (16, 'ticket.trabajar', 'Iniciar, bloquear y enviar PR', 'TICKET');
INSERT INTO `permiso` (`id`, `codigo`, `nombre`, `grupo`) VALUES (17, 'ticket.aprobar_qa', 'Probar y verificar los hotfix', 'TICKET');
INSERT INTO `permiso` (`id`, `codigo`, `nombre`, `grupo`) VALUES (18, 'ticket.cancelar', 'Cancelar tickets', 'TICKET');
INSERT INTO `permiso` (`id`, `codigo`, `nombre`, `grupo`) VALUES (19, 'bug.reportar', 'Reportar bugs', 'TICKET');
INSERT INTO `permiso` (`id`, `codigo`, `nombre`, `grupo`) VALUES (20, 'bug.confirmar', 'Confirmar bugs y su prioridad', 'TICKET');
INSERT INTO `permiso` (`id`, `codigo`, `nombre`, `grupo`) VALUES (21, 'mejora.proponer', 'Proponer mejoras', 'TICKET');
INSERT INTO `permiso` (`id`, `codigo`, `nombre`, `grupo`) VALUES (22, 'mejora.aprobar', 'Aprobar o rechazar mejoras', 'TICKET');
INSERT INTO `permiso` (`id`, `codigo`, `nombre`, `grupo`) VALUES (25, 'release.publicar', 'Subir a main, crear tag y desplegar', 'RELEASE');
INSERT INTO `permiso` (`id`, `codigo`, `nombre`, `grupo`) VALUES (26, 'release.revertir', 'Revertir a la versión anterior', 'RELEASE');
INSERT INTO `permiso` (`id`, `codigo`, `nombre`, `grupo`) VALUES (27, 'parche.publicar', 'Publicar parches (hotfix)', 'RELEASE');
INSERT INTO `permiso` (`id`, `codigo`, `nombre`, `grupo`) VALUES (28, 'proyecto.equipo', 'Armar el equipo del proyecto', 'PROYECTO');
COMMIT;

-- ----------------------------
-- Table structure for proyecto
-- ----------------------------
DROP TABLE IF EXISTS `proyecto`;
CREATE TABLE `proyecto` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `codigo` varchar(5) NOT NULL,
  `nombre` varchar(120) NOT NULL,
  `descripcion` text,
  `cliente_id` bigint DEFAULT NULL,
  `prioridad` varchar(10) NOT NULL DEFAULT 'MEDIA',
  `estado` varchar(20) NOT NULL DEFAULT 'PLANEACION',
  `fecha_inicio` date DEFAULT NULL,
  `fecha_entrega` date DEFAULT NULL,
  `lider_id` bigint NOT NULL,
  `permitir_autovalidacion` tinyint(1) NOT NULL DEFAULT '0',
  `comentario_al_rechazar` tinyint(1) NOT NULL DEFAULT '1',
  `rama_al_iniciar` tinyint(1) NOT NULL DEFAULT '1',
  `notificar_cliente_fase` tinyint(1) NOT NULL DEFAULT '1',
  `creado_en` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `actualizado_en` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `codigo` (`codigo`),
  KEY `fk_proyecto_cliente` (`cliente_id`),
  KEY `fk_proyecto_lider` (`lider_id`),
  CONSTRAINT `fk_proyecto_cliente` FOREIGN KEY (`cliente_id`) REFERENCES `cliente` (`id`),
  CONSTRAINT `fk_proyecto_lider` FOREIGN KEY (`lider_id`) REFERENCES `usuario` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1017 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Records of proyecto
-- ----------------------------
BEGIN;
INSERT INTO `proyecto` (`id`, `codigo`, `nombre`, `descripcion`, `cliente_id`, `prioridad`, `estado`, `fecha_inicio`, `fecha_entrega`, `lider_id`, `permitir_autovalidacion`, `comentario_al_rechazar`, `rama_al_iniciar`, `notificar_cliente_fase`, `creado_en`, `actualizado_en`) VALUES (1016, 'ESC', 'Sistema Escolar', 'Control escolar para varias escuelas. El Administrador da de alta escuelas y sus usuarios; cada escuela registra sus periodos, generaciones, materias y alumnos.', 1001, 'ALTA', 'PLANEACION', NULL, NULL, 1016, 1, 1, 1, 0, '2026-10-28 21:51:28', '2026-10-28 21:51:28');
COMMIT;

-- ----------------------------
-- Table structure for proyecto_miembro
-- ----------------------------
DROP TABLE IF EXISTS `proyecto_miembro`;
CREATE TABLE `proyecto_miembro` (
  `proyecto_id` bigint NOT NULL,
  `usuario_id` bigint NOT NULL,
  `agregado_en` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`proyecto_id`,`usuario_id`),
  KEY `fk_miembro_usuario` (`usuario_id`),
  CONSTRAINT `fk_miembro_proyecto` FOREIGN KEY (`proyecto_id`) REFERENCES `proyecto` (`id`),
  CONSTRAINT `fk_miembro_usuario` FOREIGN KEY (`usuario_id`) REFERENCES `usuario` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Records of proyecto_miembro
-- ----------------------------
BEGIN;
INSERT INTO `proyecto_miembro` (`proyecto_id`, `usuario_id`, `agregado_en`) VALUES (1016, 1001, '2026-10-28 21:51:28');
INSERT INTO `proyecto_miembro` (`proyecto_id`, `usuario_id`, `agregado_en`) VALUES (1016, 1013, '2026-10-28 21:51:28');
INSERT INTO `proyecto_miembro` (`proyecto_id`, `usuario_id`, `agregado_en`) VALUES (1016, 1014, '2026-10-28 21:51:28');
INSERT INTO `proyecto_miembro` (`proyecto_id`, `usuario_id`, `agregado_en`) VALUES (1016, 1016, '2026-10-28 21:51:28');
COMMIT;

-- ----------------------------
-- Table structure for proyecto_usuario_sistema
-- ----------------------------
DROP TABLE IF EXISTS `proyecto_usuario_sistema`;
CREATE TABLE `proyecto_usuario_sistema` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `proyecto_id` bigint NOT NULL,
  `nombre` varchar(80) NOT NULL,
  `descripcion` varchar(300) DEFAULT NULL,
  `orden` int NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_usuario_sistema_nombre` (`proyecto_id`,`nombre`),
  CONSTRAINT `fk_usuario_sistema_proyecto` FOREIGN KEY (`proyecto_id`) REFERENCES `proyecto` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Records of proyecto_usuario_sistema
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for pull_request
-- ----------------------------
DROP TABLE IF EXISTS `pull_request`;
CREATE TABLE `pull_request` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `ticket_id` bigint NOT NULL,
  `modulo_id` bigint NOT NULL,
  `numero` int NOT NULL,
  `rama_origen` varchar(150) NOT NULL,
  `rama_destino` varchar(60) NOT NULL,
  `estado` varchar(12) NOT NULL DEFAULT 'ABIERTO',
  `ci_estado` varchar(10) NOT NULL DEFAULT 'PENDIENTE',
  `con_conflictos` tinyint(1) NOT NULL DEFAULT '0',
  `url` varchar(300) DEFAULT NULL,
  `creado_en` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `actualizado_en` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `fk_pr_ticket` (`ticket_id`),
  KEY `fk_pr_modulo` (`modulo_id`),
  CONSTRAINT `fk_pr_modulo` FOREIGN KEY (`modulo_id`) REFERENCES `modulo` (`id`),
  CONSTRAINT `fk_pr_ticket` FOREIGN KEY (`ticket_id`) REFERENCES `ticket` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1005 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Records of pull_request
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for rol
-- ----------------------------
DROP TABLE IF EXISTS `rol`;
CREATE TABLE `rol` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `codigo` varchar(30) NOT NULL,
  `nombre` varchar(60) NOT NULL,
  `descripcion` varchar(200) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `codigo` (`codigo`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Records of rol
-- ----------------------------
BEGIN;
INSERT INTO `rol` (`id`, `codigo`, `nombre`, `descripcion`) VALUES (1, 'ADMIN', 'Administrador', 'Acceso total al sistema');
INSERT INTO `rol` (`id`, `codigo`, `nombre`, `descripcion`) VALUES (2, 'LIDER', 'Líder de Proyecto', 'Administra proyectos y equipos');
INSERT INTO `rol` (`id`, `codigo`, `nombre`, `descripcion`) VALUES (3, 'ANALISTA', 'Analista Comercial', 'Gestiona requerimientos e historias');
INSERT INTO `rol` (`id`, `codigo`, `nombre`, `descripcion`) VALUES (4, 'DESARROLLADOR', 'Desarrollador', 'Desarrolla funcionalidades');
COMMIT;

-- ----------------------------
-- Table structure for rol_permiso
-- ----------------------------
DROP TABLE IF EXISTS `rol_permiso`;
CREATE TABLE `rol_permiso` (
  `rol_id` bigint NOT NULL,
  `permiso_id` bigint NOT NULL,
  PRIMARY KEY (`rol_id`,`permiso_id`),
  KEY `fk_rol_permiso_permiso` (`permiso_id`),
  CONSTRAINT `fk_rol_permiso_permiso` FOREIGN KEY (`permiso_id`) REFERENCES `permiso` (`id`),
  CONSTRAINT `fk_rol_permiso_rol` FOREIGN KEY (`rol_id`) REFERENCES `rol` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Records of rol_permiso
-- ----------------------------
BEGIN;
INSERT INTO `rol_permiso` (`rol_id`, `permiso_id`) VALUES (1, 1);
INSERT INTO `rol_permiso` (`rol_id`, `permiso_id`) VALUES (1, 2);
INSERT INTO `rol_permiso` (`rol_id`, `permiso_id`) VALUES (1, 3);
INSERT INTO `rol_permiso` (`rol_id`, `permiso_id`) VALUES (1, 4);
INSERT INTO `rol_permiso` (`rol_id`, `permiso_id`) VALUES (2, 4);
INSERT INTO `rol_permiso` (`rol_id`, `permiso_id`) VALUES (1, 5);
INSERT INTO `rol_permiso` (`rol_id`, `permiso_id`) VALUES (2, 5);
INSERT INTO `rol_permiso` (`rol_id`, `permiso_id`) VALUES (1, 6);
INSERT INTO `rol_permiso` (`rol_id`, `permiso_id`) VALUES (2, 6);
INSERT INTO `rol_permiso` (`rol_id`, `permiso_id`) VALUES (1, 7);
INSERT INTO `rol_permiso` (`rol_id`, `permiso_id`) VALUES (1, 8);
INSERT INTO `rol_permiso` (`rol_id`, `permiso_id`) VALUES (2, 8);
INSERT INTO `rol_permiso` (`rol_id`, `permiso_id`) VALUES (1, 9);
INSERT INTO `rol_permiso` (`rol_id`, `permiso_id`) VALUES (2, 9);
INSERT INTO `rol_permiso` (`rol_id`, `permiso_id`) VALUES (3, 9);
INSERT INTO `rol_permiso` (`rol_id`, `permiso_id`) VALUES (1, 10);
INSERT INTO `rol_permiso` (`rol_id`, `permiso_id`) VALUES (2, 10);
INSERT INTO `rol_permiso` (`rol_id`, `permiso_id`) VALUES (3, 10);
INSERT INTO `rol_permiso` (`rol_id`, `permiso_id`) VALUES (1, 11);
INSERT INTO `rol_permiso` (`rol_id`, `permiso_id`) VALUES (2, 11);
INSERT INTO `rol_permiso` (`rol_id`, `permiso_id`) VALUES (1, 12);
INSERT INTO `rol_permiso` (`rol_id`, `permiso_id`) VALUES (3, 12);
INSERT INTO `rol_permiso` (`rol_id`, `permiso_id`) VALUES (1, 13);
INSERT INTO `rol_permiso` (`rol_id`, `permiso_id`) VALUES (2, 13);
INSERT INTO `rol_permiso` (`rol_id`, `permiso_id`) VALUES (3, 13);
INSERT INTO `rol_permiso` (`rol_id`, `permiso_id`) VALUES (1, 14);
INSERT INTO `rol_permiso` (`rol_id`, `permiso_id`) VALUES (2, 14);
INSERT INTO `rol_permiso` (`rol_id`, `permiso_id`) VALUES (1, 15);
INSERT INTO `rol_permiso` (`rol_id`, `permiso_id`) VALUES (2, 15);
INSERT INTO `rol_permiso` (`rol_id`, `permiso_id`) VALUES (1, 16);
INSERT INTO `rol_permiso` (`rol_id`, `permiso_id`) VALUES (4, 16);
INSERT INTO `rol_permiso` (`rol_id`, `permiso_id`) VALUES (1, 17);
INSERT INTO `rol_permiso` (`rol_id`, `permiso_id`) VALUES (3, 17);
INSERT INTO `rol_permiso` (`rol_id`, `permiso_id`) VALUES (1, 18);
INSERT INTO `rol_permiso` (`rol_id`, `permiso_id`) VALUES (2, 18);
INSERT INTO `rol_permiso` (`rol_id`, `permiso_id`) VALUES (1, 19);
INSERT INTO `rol_permiso` (`rol_id`, `permiso_id`) VALUES (2, 19);
INSERT INTO `rol_permiso` (`rol_id`, `permiso_id`) VALUES (3, 19);
INSERT INTO `rol_permiso` (`rol_id`, `permiso_id`) VALUES (4, 19);
INSERT INTO `rol_permiso` (`rol_id`, `permiso_id`) VALUES (1, 20);
INSERT INTO `rol_permiso` (`rol_id`, `permiso_id`) VALUES (2, 20);
INSERT INTO `rol_permiso` (`rol_id`, `permiso_id`) VALUES (1, 21);
INSERT INTO `rol_permiso` (`rol_id`, `permiso_id`) VALUES (3, 21);
INSERT INTO `rol_permiso` (`rol_id`, `permiso_id`) VALUES (1, 22);
INSERT INTO `rol_permiso` (`rol_id`, `permiso_id`) VALUES (2, 22);
INSERT INTO `rol_permiso` (`rol_id`, `permiso_id`) VALUES (1, 25);
INSERT INTO `rol_permiso` (`rol_id`, `permiso_id`) VALUES (2, 25);
INSERT INTO `rol_permiso` (`rol_id`, `permiso_id`) VALUES (1, 26);
INSERT INTO `rol_permiso` (`rol_id`, `permiso_id`) VALUES (2, 26);
INSERT INTO `rol_permiso` (`rol_id`, `permiso_id`) VALUES (1, 27);
INSERT INTO `rol_permiso` (`rol_id`, `permiso_id`) VALUES (2, 27);
INSERT INTO `rol_permiso` (`rol_id`, `permiso_id`) VALUES (1, 28);
COMMIT;

-- ----------------------------
-- Table structure for solicitud
-- ----------------------------
DROP TABLE IF EXISTS `solicitud`;
CREATE TABLE `solicitud` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `proyecto_id` bigint NOT NULL,
  `usuario_id` bigint NOT NULL,
  `tipo` varchar(15) NOT NULL,
  `titulo` varchar(150) NOT NULL,
  `descripcion` text,
  `estado` varchar(12) NOT NULL DEFAULT 'RECIBIDA',
  `respuesta` text,
  `fase_id` bigint DEFAULT NULL,
  `ticket_id` bigint DEFAULT NULL,
  `creado_en` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `fk_solicitud_proyecto` (`proyecto_id`),
  KEY `fk_solicitud_usuario` (`usuario_id`),
  KEY `fk_solicitud_fase` (`fase_id`),
  KEY `fk_solicitud_ticket` (`ticket_id`),
  CONSTRAINT `fk_solicitud_fase` FOREIGN KEY (`fase_id`) REFERENCES `fase` (`id`),
  CONSTRAINT `fk_solicitud_proyecto` FOREIGN KEY (`proyecto_id`) REFERENCES `proyecto` (`id`),
  CONSTRAINT `fk_solicitud_ticket` FOREIGN KEY (`ticket_id`) REFERENCES `ticket` (`id`),
  CONSTRAINT `fk_solicitud_usuario` FOREIGN KEY (`usuario_id`) REFERENCES `usuario` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Records of solicitud
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for ticket
-- ----------------------------
DROP TABLE IF EXISTS `ticket`;
CREATE TABLE `ticket` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `proyecto_id` bigint NOT NULL,
  `fase_id` bigint NOT NULL,
  `historia_id` bigint DEFAULT NULL,
  `tipo` varchar(4) NOT NULL,
  `numero` int NOT NULL,
  `titulo` varchar(200) NOT NULL,
  `descripcion` text,
  `estado` varchar(20) NOT NULL,
  `prioridad` varchar(10) NOT NULL DEFAULT 'MEDIA',
  `severidad` varchar(10) DEFAULT NULL,
  `modulo_id` bigint DEFAULT NULL,
  `responsable_id` bigint DEFAULT NULL,
  `tester_id` bigint DEFAULT NULL,
  `creado_por` bigint NOT NULL,
  `estimacion_horas` decimal(5,1) DEFAULT NULL,
  `horas_registradas` decimal(5,1) NOT NULL DEFAULT '0.0',
  `fecha_limite` date DEFAULT NULL,
  `rama` varchar(150) DEFAULT NULL,
  `capas` varchar(200) DEFAULT NULL,
  `historia_origen_id` bigint DEFAULT NULL,
  `version_reportada` varchar(20) DEFAULT NULL,
  `bloqueado_por_id` bigint DEFAULT NULL,
  `creado_en` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `actualizado_en` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_ticket_numero` (`proyecto_id`,`tipo`,`numero`),
  KEY `idx_ticket_fase_estado` (`fase_id`,`estado`),
  KEY `idx_ticket_responsable` (`responsable_id`,`estado`),
  KEY `fk_ticket_historia` (`historia_id`),
  KEY `fk_ticket_modulo` (`modulo_id`),
  KEY `fk_ticket_tester` (`tester_id`),
  KEY `fk_ticket_creado_por` (`creado_por`),
  KEY `fk_ticket_bloqueado_por` (`bloqueado_por_id`),
  KEY `fk_ticket_historia_origen` (`historia_origen_id`),
  CONSTRAINT `fk_ticket_bloqueado_por` FOREIGN KEY (`bloqueado_por_id`) REFERENCES `ticket` (`id`),
  CONSTRAINT `fk_ticket_creado_por` FOREIGN KEY (`creado_por`) REFERENCES `usuario` (`id`),
  CONSTRAINT `fk_ticket_fase` FOREIGN KEY (`fase_id`) REFERENCES `fase` (`id`),
  CONSTRAINT `fk_ticket_historia` FOREIGN KEY (`historia_id`) REFERENCES `historia` (`id`),
  CONSTRAINT `fk_ticket_historia_origen` FOREIGN KEY (`historia_origen_id`) REFERENCES `historia` (`id`),
  CONSTRAINT `fk_ticket_modulo` FOREIGN KEY (`modulo_id`) REFERENCES `modulo` (`id`),
  CONSTRAINT `fk_ticket_proyecto` FOREIGN KEY (`proyecto_id`) REFERENCES `proyecto` (`id`),
  CONSTRAINT `fk_ticket_responsable` FOREIGN KEY (`responsable_id`) REFERENCES `usuario` (`id`),
  CONSTRAINT `fk_ticket_tester` FOREIGN KEY (`tester_id`) REFERENCES `usuario` (`id`),
  CONSTRAINT `chk_ticket_tipo` CHECK ((`tipo` in (_utf8mb4'ACTV',_utf8mb4'BUGS',_utf8mb4'MEJR')))
) ENGINE=InnoDB AUTO_INCREMENT=1441 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Records of ticket
-- ----------------------------
BEGIN;
INSERT INTO `ticket` (`id`, `proyecto_id`, `fase_id`, `historia_id`, `tipo`, `numero`, `titulo`, `descripcion`, `estado`, `prioridad`, `severidad`, `modulo_id`, `responsable_id`, `tester_id`, `creado_por`, `estimacion_horas`, `horas_registradas`, `fecha_limite`, `rama`, `capas`, `historia_origen_id`, `version_reportada`, `bloqueado_por_id`, `creado_en`, `actualizado_en`) VALUES (1440, 1016, 1043, 1058, 'ACTV', 1, 'asdasd', 'asdas', 'EN_REVISION', 'MEDIA', NULL, 1023, 1001, NULL, 1016, NULL, 0.0, NULL, 'feature/actv-01-asdasd', NULL, NULL, NULL, NULL, '2026-10-29 21:20:49', '2026-10-29 21:32:31');
COMMIT;

-- ----------------------------
-- Table structure for ticket_criterio
-- ----------------------------
DROP TABLE IF EXISTS `ticket_criterio`;
CREATE TABLE `ticket_criterio` (
  `ticket_id` bigint NOT NULL,
  `criterio_id` bigint NOT NULL,
  PRIMARY KEY (`ticket_id`,`criterio_id`),
  KEY `fk_ticket_criterio_criterio` (`criterio_id`),
  CONSTRAINT `fk_ticket_criterio_criterio` FOREIGN KEY (`criterio_id`) REFERENCES `historia_criterio` (`id`),
  CONSTRAINT `fk_ticket_criterio_ticket` FOREIGN KEY (`ticket_id`) REFERENCES `ticket` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Records of ticket_criterio
-- ----------------------------
BEGIN;
INSERT INTO `ticket_criterio` (`ticket_id`, `criterio_id`) VALUES (1440, 1133);
INSERT INTO `ticket_criterio` (`ticket_id`, `criterio_id`) VALUES (1440, 1134);
INSERT INTO `ticket_criterio` (`ticket_id`, `criterio_id`) VALUES (1440, 1135);
INSERT INTO `ticket_criterio` (`ticket_id`, `criterio_id`) VALUES (1440, 1136);
COMMIT;

-- ----------------------------
-- Table structure for ticket_dependencia
-- ----------------------------
DROP TABLE IF EXISTS `ticket_dependencia`;
CREATE TABLE `ticket_dependencia` (
  `ticket_id` bigint NOT NULL,
  `depende_de_id` bigint NOT NULL,
  PRIMARY KEY (`ticket_id`,`depende_de_id`),
  KEY `fk_dependencia_depende_de` (`depende_de_id`),
  CONSTRAINT `fk_dependencia_depende_de` FOREIGN KEY (`depende_de_id`) REFERENCES `ticket` (`id`),
  CONSTRAINT `fk_dependencia_ticket` FOREIGN KEY (`ticket_id`) REFERENCES `ticket` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Records of ticket_dependencia
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for ticket_subtarea
-- ----------------------------
DROP TABLE IF EXISTS `ticket_subtarea`;
CREATE TABLE `ticket_subtarea` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `ticket_id` bigint NOT NULL,
  `descripcion` varchar(300) NOT NULL,
  `hecha` tinyint(1) NOT NULL DEFAULT '0',
  `orden` int NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `fk_subtarea_ticket` (`ticket_id`),
  CONSTRAINT `fk_subtarea_ticket` FOREIGN KEY (`ticket_id`) REFERENCES `ticket` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1027 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Records of ticket_subtarea
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for usuario
-- ----------------------------
DROP TABLE IF EXISTS `usuario`;
CREATE TABLE `usuario` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `nombre` varchar(80) NOT NULL,
  `apellidos` varchar(120) DEFAULT NULL,
  `correo` varchar(120) NOT NULL,
  `password_hash` varchar(100) NOT NULL,
  `tipo` varchar(10) NOT NULL,
  `cliente_id` bigint DEFAULT NULL,
  `activo` tinyint(1) NOT NULL DEFAULT '1',
  `cambiar_password` tinyint(1) NOT NULL DEFAULT '1',
  `ultimo_acceso` datetime DEFAULT NULL,
  `creado_en` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `actualizado_en` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `correo` (`correo`),
  KEY `fk_usuario_cliente` (`cliente_id`),
  CONSTRAINT `fk_usuario_cliente` FOREIGN KEY (`cliente_id`) REFERENCES `cliente` (`id`),
  CONSTRAINT `chk_usuario_cliente` CHECK (((`tipo` = _utf8mb4'EQUIPO') or (`cliente_id` is not null))),
  CONSTRAINT `chk_usuario_tipo` CHECK ((`tipo` in (_utf8mb4'EQUIPO',_utf8mb4'CLIENTE')))
) ENGINE=InnoDB AUTO_INCREMENT=1017 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Records of usuario
-- ----------------------------
BEGIN;
INSERT INTO `usuario` (`id`, `nombre`, `apellidos`, `correo`, `password_hash`, `tipo`, `cliente_id`, `activo`, `cambiar_password`, `ultimo_acceso`, `creado_en`, `actualizado_en`) VALUES (1001, 'Jorge', 'Sebastián', 'jorge@equipo.mx', '$2y$10$pnGhSVbhgOfoFJzOzILvX.Xwia6j5XItZeZwcUMUcp4FbYgVvcqTW', 'EQUIPO', NULL, 1, 0, '2026-10-29 21:28:38', '2026-10-18 22:10:25', '2026-10-29 21:28:38');
INSERT INTO `usuario` (`id`, `nombre`, `apellidos`, `correo`, `password_hash`, `tipo`, `cliente_id`, `activo`, `cambiar_password`, `ultimo_acceso`, `creado_en`, `actualizado_en`) VALUES (1013, 'Alma', NULL, 'alma@equipo.mx', '$2y$10$pnGhSVbhgOfoFJzOzILvX.Xwia6j5XItZeZwcUMUcp4FbYgVvcqTW', 'EQUIPO', NULL, 1, 0, '2026-10-25 22:34:24', '2026-10-24 19:50:08', '2026-10-25 22:34:24');
INSERT INTO `usuario` (`id`, `nombre`, `apellidos`, `correo`, `password_hash`, `tipo`, `cliente_id`, `activo`, `cambiar_password`, `ultimo_acceso`, `creado_en`, `actualizado_en`) VALUES (1014, 'Marco', NULL, 'marco@equipo.mx', '$2y$10$pnGhSVbhgOfoFJzOzILvX.Xwia6j5XItZeZwcUMUcp4FbYgVvcqTW', 'EQUIPO', NULL, 1, 0, '2026-10-26 22:17:40', '2026-10-24 19:50:08', '2026-10-26 22:17:39');
INSERT INTO `usuario` (`id`, `nombre`, `apellidos`, `correo`, `password_hash`, `tipo`, `cliente_id`, `activo`, `cambiar_password`, `ultimo_acceso`, `creado_en`, `actualizado_en`) VALUES (1015, 'Administrador', NULL, 'admin@equipo.mx', '$2y$10$pnGhSVbhgOfoFJzOzILvX.Xwia6j5XItZeZwcUMUcp4FbYgVvcqTW', 'EQUIPO', NULL, 1, 0, '2026-10-26 22:08:21', '2026-10-25 13:12:54', '2026-10-26 22:08:21');
INSERT INTO `usuario` (`id`, `nombre`, `apellidos`, `correo`, `password_hash`, `tipo`, `cliente_id`, `activo`, `cambiar_password`, `ultimo_acceso`, `creado_en`, `actualizado_en`) VALUES (1016, 'Lider', NULL, 'lider@equipo.mx', '$2y$10$pnGhSVbhgOfoFJzOzILvX.Xwia6j5XItZeZwcUMUcp4FbYgVvcqTW', 'EQUIPO', NULL, 1, 0, '2026-10-29 21:07:04', '2026-10-25 13:28:05', '2026-10-29 21:07:03');
COMMIT;

-- ----------------------------
-- Table structure for usuario_permiso
-- ----------------------------
DROP TABLE IF EXISTS `usuario_permiso`;
CREATE TABLE `usuario_permiso` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `usuario_id` bigint NOT NULL,
  `permiso_id` bigint NOT NULL,
  `proyecto_id` bigint DEFAULT NULL,
  `otorgado` tinyint(1) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_usuario_permiso_usuario` (`usuario_id`),
  KEY `fk_usuario_permiso_permiso` (`permiso_id`),
  KEY `fk_usuario_permiso_proyecto` (`proyecto_id`),
  CONSTRAINT `fk_usuario_permiso_permiso` FOREIGN KEY (`permiso_id`) REFERENCES `permiso` (`id`),
  CONSTRAINT `fk_usuario_permiso_proyecto` FOREIGN KEY (`proyecto_id`) REFERENCES `proyecto` (`id`),
  CONSTRAINT `fk_usuario_permiso_usuario` FOREIGN KEY (`usuario_id`) REFERENCES `usuario` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1013 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Records of usuario_permiso
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for usuario_rol
-- ----------------------------
DROP TABLE IF EXISTS `usuario_rol`;
CREATE TABLE `usuario_rol` (
  `usuario_id` bigint NOT NULL,
  `rol_id` bigint NOT NULL,
  PRIMARY KEY (`usuario_id`,`rol_id`),
  KEY `fk_usuario_rol_rol` (`rol_id`),
  CONSTRAINT `fk_usuario_rol_rol` FOREIGN KEY (`rol_id`) REFERENCES `rol` (`id`),
  CONSTRAINT `fk_usuario_rol_usuario` FOREIGN KEY (`usuario_id`) REFERENCES `usuario` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Records of usuario_rol
-- ----------------------------
BEGIN;
INSERT INTO `usuario_rol` (`usuario_id`, `rol_id`) VALUES (1015, 1);
INSERT INTO `usuario_rol` (`usuario_id`, `rol_id`) VALUES (1016, 2);
INSERT INTO `usuario_rol` (`usuario_id`, `rol_id`) VALUES (1013, 3);
INSERT INTO `usuario_rol` (`usuario_id`, `rol_id`) VALUES (1014, 3);
INSERT INTO `usuario_rol` (`usuario_id`, `rol_id`) VALUES (1001, 4);
INSERT INTO `usuario_rol` (`usuario_id`, `rol_id`) VALUES (1014, 4);
COMMIT;

-- ----------------------------
-- Table structure for version_release
-- ----------------------------
DROP TABLE IF EXISTS `version_release`;
CREATE TABLE `version_release` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `proyecto_id` bigint NOT NULL,
  `fase_id` bigint DEFAULT NULL,
  `version` varchar(20) NOT NULL,
  `tipo` varchar(10) NOT NULL DEFAULT 'FASE',
  `estado` varchar(12) NOT NULL DEFAULT 'PREPARANDO',
  `resumen` varchar(200) DEFAULT NULL,
  `notas` text,
  `enviar_notas` tinyint(1) NOT NULL DEFAULT '1',
  `publicado_por` bigint DEFAULT NULL,
  `publicado_en` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_release_version` (`proyecto_id`,`version`),
  KEY `fk_release_fase` (`fase_id`),
  KEY `fk_release_usuario` (`publicado_por`),
  CONSTRAINT `fk_release_fase` FOREIGN KEY (`fase_id`) REFERENCES `fase` (`id`),
  CONSTRAINT `fk_release_proyecto` FOREIGN KEY (`proyecto_id`) REFERENCES `proyecto` (`id`),
  CONSTRAINT `fk_release_usuario` FOREIGN KEY (`publicado_por`) REFERENCES `usuario` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1021 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Records of version_release
-- ----------------------------
BEGIN;
COMMIT;

SET FOREIGN_KEY_CHECKS = 1;
