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

 Date: 26/10/2026 22:31:52
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
INSERT INTO `adjunto` (`id`, `entidad`, `entidad_id`, `nombre`, `tipo_mime`, `tamano_bytes`, `ruta`, `subido_por`, `creado_en`) VALUES (1, 'HISTORIA', 1041, '840600267_1755211138873146_1562616267050059485_n.jpeg', 'image/jpeg', 73612, '6aec78ec-ba52-40b8-9d16-b487e96a89ac.jpg', 1015, '2026-10-25 14:51:27');
INSERT INTO `adjunto` (`id`, `entidad`, `entidad_id`, `nombre`, `tipo_mime`, `tamano_bytes`, `ruta`, `subido_por`, `creado_en`) VALUES (2, 'HISTORIA', 1041, 'Gemini_Generated_Image_vznoi4vznoi4vzno.jpeg', 'image/jpeg', 2768753, '0932cd51-6bb1-4eae-9f5f-ac3e29c38e2d.jpg', 1015, '2026-10-25 14:51:30');
INSERT INTO `adjunto` (`id`, `entidad`, `entidad_id`, `nombre`, `tipo_mime`, `tamano_bytes`, `ruta`, `subido_por`, `creado_en`) VALUES (3, 'HISTORIA', 1042, 'Gemini_Generated_Image_vznoi4vznoi4vzno.jpeg', 'image/jpeg', 2768753, 'a7b8287e-dd00-465a-a908-b6e30011420c.jpg', 1013, '2026-10-25 14:53:03');
INSERT INTO `adjunto` (`id`, `entidad`, `entidad_id`, `nombre`, `tipo_mime`, `tamano_bytes`, `ruta`, `subido_por`, `creado_en`) VALUES (4, 'HISTORIA', 1042, 'Gemini_Generated_Image_vznoi4vznoi4vzno (1).jpeg', 'image/jpeg', 2768753, '25424686-c92d-4930-8afa-1382e96f776e.jpg', 1013, '2026-10-25 14:53:05');
INSERT INTO `adjunto` (`id`, `entidad`, `entidad_id`, `nombre`, `tipo_mime`, `tamano_bytes`, `ruta`, `subido_por`, `creado_en`) VALUES (5, 'HISTORIA', 1046, 'Gemini_Generated_Image_vznoi4vznoi4vzno.jpeg', 'image/jpeg', 2768753, 'df68063a-4e92-4f70-a30d-0603f61c3f66.jpg', 1013, '2026-10-25 16:20:52');
INSERT INTO `adjunto` (`id`, `entidad`, `entidad_id`, `nombre`, `tipo_mime`, `tamano_bytes`, `ruta`, `subido_por`, `creado_en`) VALUES (6, 'HISTORIA', 1046, 'Guia-paso-2-Paciente.png', 'image/png', 76014, '260f3f75-78be-444d-9639-ced8f92036b0.png', 1013, '2026-10-25 16:20:56');
INSERT INTO `adjunto` (`id`, `entidad`, `entidad_id`, `nombre`, `tipo_mime`, `tamano_bytes`, `ruta`, `subido_por`, `creado_en`) VALUES (7, 'HISTORIA', 1048, 'Gemini_Generated_Image_vznoi4vznoi4vzno.jpeg', 'image/jpeg', 2768753, '126d4101-e97e-4df5-afb1-8f464382241b.jpg', 1013, '2026-10-25 19:31:21');
INSERT INTO `adjunto` (`id`, `entidad`, `entidad_id`, `nombre`, `tipo_mime`, `tamano_bytes`, `ruta`, `subido_por`, `creado_en`) VALUES (8, 'HISTORIA', 1051, 'Gemini_Generated_Image_vznoi4vznoi4vzno.jpeg', 'image/jpeg', 2768753, '0263128a-60e2-4715-b963-751bc8e2cf91.jpg', 1014, '2026-10-25 22:22:32');
INSERT INTO `adjunto` (`id`, `entidad`, `entidad_id`, `nombre`, `tipo_mime`, `tamano_bytes`, `ruta`, `subido_por`, `creado_en`) VALUES (9, 'HISTORIA', 1053, 'Gemini_Generated_Image_vznoi4vznoi4vzno.jpeg', 'image/jpeg', 2768753, '8b3e1d0a-3eae-4240-b4dd-76de9d587c0e.jpg', 1014, '2026-10-26 22:11:13');
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
INSERT INTO `comentario` (`id`, `entidad`, `entidad_id`, `usuario_id`, `texto`, `creado_en`) VALUES (1016, 'HISTORIA', 1046, 1013, 'asdasd', '2026-10-25 16:21:00');
INSERT INTO `comentario` (`id`, `entidad`, `entidad_id`, `usuario_id`, `texto`, `creado_en`) VALUES (1017, 'HISTORIA', 1048, 1013, 'asdas', '2026-10-25 19:49:56');
INSERT INTO `comentario` (`id`, `entidad`, `entidad_id`, `usuario_id`, `texto`, `creado_en`) VALUES (1018, 'TICKET', 1432, 1001, 'asd', '2026-10-25 22:13:19');
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
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_fase_numero` (`proyecto_id`,`numero`),
  KEY `fk_fase_responsable` (`responsable_id`),
  KEY `fk_fase_bug_reversion` (`bug_reversion_id`),
  CONSTRAINT `fk_fase_bug_reversion` FOREIGN KEY (`bug_reversion_id`) REFERENCES `ticket` (`id`),
  CONSTRAINT `fk_fase_proyecto` FOREIGN KEY (`proyecto_id`) REFERENCES `proyecto` (`id`),
  CONSTRAINT `fk_fase_responsable` FOREIGN KEY (`responsable_id`) REFERENCES `usuario` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1042 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Records of fase
-- ----------------------------
BEGIN;
INSERT INTO `fase` (`id`, `proyecto_id`, `numero`, `nombre`, `objetivo`, `estado`, `prioridad`, `fecha_inicio`, `fecha_fin`, `responsable_id`, `version_release`, `es_mantenimiento`, `motivo_estado`, `reanudar_el`, `bug_reversion_id`, `creado_en`, `actualizado_en`) VALUES (1032, 1013, 0, 'Mantenimiento', 'Correcciones urgentes (hotfix) de lo que ya está en producción.', 'EN_CURSO', 'ALTA', NULL, NULL, 1016, NULL, 1, NULL, NULL, NULL, '2026-10-25 14:48:56', '2026-10-25 14:48:56');
INSERT INTO `fase` (`id`, `proyecto_id`, `numero`, `nombre`, `objetivo`, `estado`, `prioridad`, `fecha_inicio`, `fecha_fin`, `responsable_id`, `version_release`, `es_mantenimiento`, `motivo_estado`, `reanudar_el`, `bug_reversion_id`, `creado_en`, `actualizado_en`) VALUES (1033, 1013, 1, 'asd', 'asd', 'PUBLICADA', 'MEDIA', '2026-10-25', '2026-11-08', 1013, 'v1.0.0', 0, NULL, NULL, NULL, '2026-10-25 14:50:25', '2026-10-25 15:22:29');
INSERT INTO `fase` (`id`, `proyecto_id`, `numero`, `nombre`, `objetivo`, `estado`, `prioridad`, `fecha_inicio`, `fecha_fin`, `responsable_id`, `version_release`, `es_mantenimiento`, `motivo_estado`, `reanudar_el`, `bug_reversion_id`, `creado_en`, `actualizado_en`) VALUES (1034, 1013, 2, 'asda', 'sdasd', 'PUBLICADA', 'MEDIA', '2026-11-09', '2026-12-04', 1014, 'v1.1.0', 0, NULL, NULL, NULL, '2026-10-25 15:42:35', '2026-10-25 16:05:19');
INSERT INTO `fase` (`id`, `proyecto_id`, `numero`, `nombre`, `objetivo`, `estado`, `prioridad`, `fecha_inicio`, `fecha_fin`, `responsable_id`, `version_release`, `es_mantenimiento`, `motivo_estado`, `reanudar_el`, `bug_reversion_id`, `creado_en`, `actualizado_en`) VALUES (1035, 1013, 3, 'asdasd', 'asdasdsad', 'PUBLICADA', 'MEDIA', '2026-12-05', '2027-01-10', 1014, 'v1.2.0', 0, NULL, NULL, NULL, '2026-10-25 16:14:03', '2026-10-25 20:05:45');
INSERT INTO `fase` (`id`, `proyecto_id`, `numero`, `nombre`, `objetivo`, `estado`, `prioridad`, `fecha_inicio`, `fecha_fin`, `responsable_id`, `version_release`, `es_mantenimiento`, `motivo_estado`, `reanudar_el`, `bug_reversion_id`, `creado_en`, `actualizado_en`) VALUES (1036, 1013, 4, 'hasdhasd', 'dasdd', 'EN_CURSO', 'MEDIA', '2027-01-01', '2027-01-29', 1016, 'v1.3.0', 0, NULL, NULL, NULL, '2026-10-25 19:28:15', '2026-10-25 20:05:59');
INSERT INTO `fase` (`id`, `proyecto_id`, `numero`, `nombre`, `objetivo`, `estado`, `prioridad`, `fecha_inicio`, `fecha_fin`, `responsable_id`, `version_release`, `es_mantenimiento`, `motivo_estado`, `reanudar_el`, `bug_reversion_id`, `creado_en`, `actualizado_en`) VALUES (1037, 1014, 0, 'Mantenimiento', 'Correcciones urgentes (hotfix) de lo que ya está en producción.', 'EN_CURSO', 'ALTA', NULL, NULL, 1001, NULL, 1, NULL, NULL, NULL, '2026-10-25 21:35:44', '2026-10-25 21:35:44');
INSERT INTO `fase` (`id`, `proyecto_id`, `numero`, `nombre`, `objetivo`, `estado`, `prioridad`, `fecha_inicio`, `fecha_fin`, `responsable_id`, `version_release`, `es_mantenimiento`, `motivo_estado`, `reanudar_el`, `bug_reversion_id`, `creado_en`, `actualizado_en`) VALUES (1038, 1014, 1, 'sadsadasdas', 'sadasdasdasdasd', 'EN_CURSO', 'MEDIA', '2026-10-25', '2026-11-08', 1001, 'v1.0.0', 0, NULL, NULL, NULL, '2026-10-25 21:38:08', '2026-10-25 22:12:30');
INSERT INTO `fase` (`id`, `proyecto_id`, `numero`, `nombre`, `objetivo`, `estado`, `prioridad`, `fecha_inicio`, `fecha_fin`, `responsable_id`, `version_release`, `es_mantenimiento`, `motivo_estado`, `reanudar_el`, `bug_reversion_id`, `creado_en`, `actualizado_en`) VALUES (1039, 1015, 0, 'Mantenimiento', 'Correcciones urgentes (hotfix) de lo que ya está en producción.', 'EN_CURSO', 'ALTA', NULL, NULL, 1016, NULL, 1, NULL, NULL, NULL, '2026-10-25 22:20:21', '2026-10-25 22:20:21');
INSERT INTO `fase` (`id`, `proyecto_id`, `numero`, `nombre`, `objetivo`, `estado`, `prioridad`, `fecha_inicio`, `fecha_fin`, `responsable_id`, `version_release`, `es_mantenimiento`, `motivo_estado`, `reanudar_el`, `bug_reversion_id`, `creado_en`, `actualizado_en`) VALUES (1040, 1015, 1, 'asdas', 'asdasd', 'PUBLICADA', 'MEDIA', '2026-10-25', '2026-11-07', 1016, 'v1.0.0', 0, NULL, NULL, NULL, '2026-10-25 22:21:23', '2026-10-25 22:35:43');
INSERT INTO `fase` (`id`, `proyecto_id`, `numero`, `nombre`, `objetivo`, `estado`, `prioridad`, `fecha_inicio`, `fecha_fin`, `responsable_id`, `version_release`, `es_mantenimiento`, `motivo_estado`, `reanudar_el`, `bug_reversion_id`, `creado_en`, `actualizado_en`) VALUES (1041, 1015, 2, 'app', 'sadasdasd aaa bbb', 'EN_CURSO', 'MEDIA', '2026-11-08', '2026-12-06', 1016, 'v1.1.0', 0, NULL, NULL, NULL, '2026-10-26 22:10:08', '2026-10-26 22:16:14');
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
) ENGINE=InnoDB AUTO_INCREMENT=1081 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Records of fase_criterio_cierre
-- ----------------------------
BEGIN;
INSERT INTO `fase_criterio_cierre` (`id`, `fase_id`, `descripcion`, `tipo`, `codigo`, `cumplido`, `cumplido_por`, `cumplido_en`) VALUES (1058, 1033, 'Todas las historias aceptadas por el Analista', 'AUTOMATICO', 'HISTORIAS_ACEPTADAS', 0, NULL, NULL);
INSERT INTO `fase_criterio_cierre` (`id`, `fase_id`, `descripcion`, `tipo`, `codigo`, `cumplido`, `cumplido_por`, `cumplido_en`) VALUES (1059, 1033, 'Sin bugs de prioridad alta abiertos', 'AUTOMATICO', 'SIN_BUGS_ALTA', 0, NULL, NULL);
INSERT INTO `fase_criterio_cierre` (`id`, `fase_id`, `descripcion`, `tipo`, `codigo`, `cumplido`, `cumplido_por`, `cumplido_en`) VALUES (1060, 1033, 'Repositorios involucrados desplegados en staging', 'AUTOMATICO', 'MODULOS_EN_STAGING', 1, 1016, '2026-10-25 15:21:44');
INSERT INTO `fase_criterio_cierre` (`id`, `fase_id`, `descripcion`, `tipo`, `codigo`, `cumplido`, `cumplido_por`, `cumplido_en`) VALUES (1061, 1033, 'Visto bueno del cliente en la demo', 'MANUAL', NULL, 1, 1016, '2026-10-25 15:05:33');
INSERT INTO `fase_criterio_cierre` (`id`, `fase_id`, `descripcion`, `tipo`, `codigo`, `cumplido`, `cumplido_por`, `cumplido_en`) VALUES (1062, 1034, 'Todas las historias aceptadas por el Analista', 'AUTOMATICO', 'HISTORIAS_ACEPTADAS', 0, NULL, NULL);
INSERT INTO `fase_criterio_cierre` (`id`, `fase_id`, `descripcion`, `tipo`, `codigo`, `cumplido`, `cumplido_por`, `cumplido_en`) VALUES (1063, 1034, 'Sin bugs de prioridad alta abiertos', 'AUTOMATICO', 'SIN_BUGS_ALTA', 0, NULL, NULL);
INSERT INTO `fase_criterio_cierre` (`id`, `fase_id`, `descripcion`, `tipo`, `codigo`, `cumplido`, `cumplido_por`, `cumplido_en`) VALUES (1064, 1034, 'Repositorios involucrados desplegados en staging', 'AUTOMATICO', 'MODULOS_EN_STAGING', 1, 1016, '2026-10-25 16:04:58');
INSERT INTO `fase_criterio_cierre` (`id`, `fase_id`, `descripcion`, `tipo`, `codigo`, `cumplido`, `cumplido_por`, `cumplido_en`) VALUES (1065, 1034, 'Visto bueno del cliente en la demo', 'MANUAL', NULL, 1, 1016, '2026-10-25 16:04:59');
INSERT INTO `fase_criterio_cierre` (`id`, `fase_id`, `descripcion`, `tipo`, `codigo`, `cumplido`, `cumplido_por`, `cumplido_en`) VALUES (1066, 1035, 'Todas las historias aceptadas por el Analista', 'AUTOMATICO', 'HISTORIAS_ACEPTADAS', 0, NULL, NULL);
INSERT INTO `fase_criterio_cierre` (`id`, `fase_id`, `descripcion`, `tipo`, `codigo`, `cumplido`, `cumplido_por`, `cumplido_en`) VALUES (1067, 1035, 'Sin bugs de prioridad alta abiertos', 'AUTOMATICO', 'SIN_BUGS_ALTA', 0, NULL, NULL);
INSERT INTO `fase_criterio_cierre` (`id`, `fase_id`, `descripcion`, `tipo`, `codigo`, `cumplido`, `cumplido_por`, `cumplido_en`) VALUES (1068, 1035, 'Repositorios involucrados desplegados en staging', 'AUTOMATICO', 'MODULOS_EN_STAGING', 1, 1016, '2026-10-25 16:15:35');
INSERT INTO `fase_criterio_cierre` (`id`, `fase_id`, `descripcion`, `tipo`, `codigo`, `cumplido`, `cumplido_por`, `cumplido_en`) VALUES (1069, 1036, 'Todas las historias aceptadas por el Analista', 'AUTOMATICO', 'HISTORIAS_ACEPTADAS', 0, NULL, NULL);
INSERT INTO `fase_criterio_cierre` (`id`, `fase_id`, `descripcion`, `tipo`, `codigo`, `cumplido`, `cumplido_por`, `cumplido_en`) VALUES (1070, 1036, 'Sin bugs de prioridad alta abiertos', 'AUTOMATICO', 'SIN_BUGS_ALTA', 0, NULL, NULL);
INSERT INTO `fase_criterio_cierre` (`id`, `fase_id`, `descripcion`, `tipo`, `codigo`, `cumplido`, `cumplido_por`, `cumplido_en`) VALUES (1071, 1036, 'Repositorios involucrados desplegados en staging', 'AUTOMATICO', 'MODULOS_EN_STAGING', 1, 1016, '2026-10-25 19:48:27');
INSERT INTO `fase_criterio_cierre` (`id`, `fase_id`, `descripcion`, `tipo`, `codigo`, `cumplido`, `cumplido_por`, `cumplido_en`) VALUES (1072, 1038, 'Todas las historias aceptadas por el Analista', 'AUTOMATICO', 'HISTORIAS_ACEPTADAS', 0, NULL, NULL);
INSERT INTO `fase_criterio_cierre` (`id`, `fase_id`, `descripcion`, `tipo`, `codigo`, `cumplido`, `cumplido_por`, `cumplido_en`) VALUES (1073, 1038, 'Sin bugs de prioridad alta abiertos', 'AUTOMATICO', 'SIN_BUGS_ALTA', 0, NULL, NULL);
INSERT INTO `fase_criterio_cierre` (`id`, `fase_id`, `descripcion`, `tipo`, `codigo`, `cumplido`, `cumplido_por`, `cumplido_en`) VALUES (1074, 1038, 'Repositorios involucrados desplegados en staging', 'AUTOMATICO', 'MODULOS_EN_STAGING', 0, NULL, NULL);
INSERT INTO `fase_criterio_cierre` (`id`, `fase_id`, `descripcion`, `tipo`, `codigo`, `cumplido`, `cumplido_por`, `cumplido_en`) VALUES (1075, 1040, 'Todas las historias aceptadas por el Analista', 'AUTOMATICO', 'HISTORIAS_ACEPTADAS', 0, NULL, NULL);
INSERT INTO `fase_criterio_cierre` (`id`, `fase_id`, `descripcion`, `tipo`, `codigo`, `cumplido`, `cumplido_por`, `cumplido_en`) VALUES (1076, 1040, 'Sin bugs de prioridad alta abiertos', 'AUTOMATICO', 'SIN_BUGS_ALTA', 0, NULL, NULL);
INSERT INTO `fase_criterio_cierre` (`id`, `fase_id`, `descripcion`, `tipo`, `codigo`, `cumplido`, `cumplido_por`, `cumplido_en`) VALUES (1077, 1040, 'Repositorios involucrados desplegados en staging', 'AUTOMATICO', 'MODULOS_EN_STAGING', 1, 1016, '2026-10-25 22:35:27');
INSERT INTO `fase_criterio_cierre` (`id`, `fase_id`, `descripcion`, `tipo`, `codigo`, `cumplido`, `cumplido_por`, `cumplido_en`) VALUES (1078, 1041, 'Todas las historias aceptadas por el Analista', 'AUTOMATICO', 'HISTORIAS_ACEPTADAS', 0, NULL, NULL);
INSERT INTO `fase_criterio_cierre` (`id`, `fase_id`, `descripcion`, `tipo`, `codigo`, `cumplido`, `cumplido_por`, `cumplido_en`) VALUES (1079, 1041, 'Sin bugs de prioridad alta abiertos', 'AUTOMATICO', 'SIN_BUGS_ALTA', 0, NULL, NULL);
INSERT INTO `fase_criterio_cierre` (`id`, `fase_id`, `descripcion`, `tipo`, `codigo`, `cumplido`, `cumplido_por`, `cumplido_en`) VALUES (1080, 1041, 'Repositorios involucrados desplegados en staging', 'AUTOMATICO', 'MODULOS_EN_STAGING', 0, NULL, NULL);
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
INSERT INTO `fase_modulo` (`fase_id`, `modulo_id`) VALUES (1033, 1020);
INSERT INTO `fase_modulo` (`fase_id`, `modulo_id`) VALUES (1034, 1020);
INSERT INTO `fase_modulo` (`fase_id`, `modulo_id`) VALUES (1035, 1020);
INSERT INTO `fase_modulo` (`fase_id`, `modulo_id`) VALUES (1036, 1020);
INSERT INTO `fase_modulo` (`fase_id`, `modulo_id`) VALUES (1038, 1021);
INSERT INTO `fase_modulo` (`fase_id`, `modulo_id`) VALUES (1040, 1022);
INSERT INTO `fase_modulo` (`fase_id`, `modulo_id`) VALUES (1041, 1022);
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
) ENGINE=InnoDB AUTO_INCREMENT=1055 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Records of historia
-- ----------------------------
BEGIN;
INSERT INTO `historia` (`id`, `proyecto_id`, `fase_id`, `numero`, `titulo`, `como`, `quiero`, `para`, `estado`, `prioridad`, `puntos`, `analista_id`, `figma_url`, `diseno_aprobado`, `diseno_aprobado_por`, `diseno_aprobado_en`, `creado_por`, `creado_en`, `actualizado_en`) VALUES (1041, 1013, 1033, 1, 'asdasdasd', 'asd', 'asda', 'sd', 'CERRADA', 'MEDIA', NULL, 1013, NULL, 0, NULL, NULL, 1015, '2026-10-25 14:50:56', '2026-10-25 15:22:29');
INSERT INTO `historia` (`id`, `proyecto_id`, `fase_id`, `numero`, `titulo`, `como`, `quiero`, `para`, `estado`, `prioridad`, `puntos`, `analista_id`, `figma_url`, `diseno_aprobado`, `diseno_aprobado_por`, `diseno_aprobado_en`, `creado_por`, `creado_en`, `actualizado_en`) VALUES (1042, 1013, 1033, 2, 'asdasdsad', 'asd', 'asd', 'asd', 'CERRADA', 'MEDIA', NULL, 1013, NULL, 0, NULL, NULL, 1013, '2026-10-25 14:52:33', '2026-10-25 15:22:29');
INSERT INTO `historia` (`id`, `proyecto_id`, `fase_id`, `numero`, `titulo`, `como`, `quiero`, `para`, `estado`, `prioridad`, `puntos`, `analista_id`, `figma_url`, `diseno_aprobado`, `diseno_aprobado_por`, `diseno_aprobado_en`, `creado_por`, `creado_en`, `actualizado_en`) VALUES (1043, 1013, 1034, 3, 'asdd', 'asd', 'asd', 'asd', 'CERRADA', 'MEDIA', NULL, 1013, NULL, 0, NULL, NULL, 1016, '2026-10-25 15:43:03', '2026-10-25 16:05:19');
INSERT INTO `historia` (`id`, `proyecto_id`, `fase_id`, `numero`, `titulo`, `como`, `quiero`, `para`, `estado`, `prioridad`, `puntos`, `analista_id`, `figma_url`, `diseno_aprobado`, `diseno_aprobado_por`, `diseno_aprobado_en`, `creado_por`, `creado_en`, `actualizado_en`) VALUES (1044, 1013, 1035, 4, 'sadas', 'd', 'sdasda', 'asdasd', 'CERRADA', 'MEDIA', 5, 1013, NULL, 0, NULL, NULL, 1016, '2026-10-25 16:14:56', '2026-10-25 20:05:45');
INSERT INTO `historia` (`id`, `proyecto_id`, `fase_id`, `numero`, `titulo`, `como`, `quiero`, `para`, `estado`, `prioridad`, `puntos`, `analista_id`, `figma_url`, `diseno_aprobado`, `diseno_aprobado_por`, `diseno_aprobado_en`, `creado_por`, `creado_en`, `actualizado_en`) VALUES (1045, 1013, 1035, 5, 'asdasd', 'asd', 'asd', 'asd', 'CERRADA', 'MEDIA', 3, 1013, NULL, 0, NULL, NULL, 1016, '2026-10-25 16:19:40', '2026-10-25 20:05:45');
INSERT INTO `historia` (`id`, `proyecto_id`, `fase_id`, `numero`, `titulo`, `como`, `quiero`, `para`, `estado`, `prioridad`, `puntos`, `analista_id`, `figma_url`, `diseno_aprobado`, `diseno_aprobado_por`, `diseno_aprobado_en`, `creado_por`, `creado_en`, `actualizado_en`) VALUES (1046, 1013, 1035, 6, 'asdasd', 'asd', 'asdas', 'dasd', 'CERRADA', 'MEDIA', 5, 1013, NULL, 0, NULL, NULL, 1016, '2026-10-25 16:20:03', '2026-10-25 20:05:45');
INSERT INTO `historia` (`id`, `proyecto_id`, `fase_id`, `numero`, `titulo`, `como`, `quiero`, `para`, `estado`, `prioridad`, `puntos`, `analista_id`, `figma_url`, `diseno_aprobado`, `diseno_aprobado_por`, `diseno_aprobado_en`, `creado_por`, `creado_en`, `actualizado_en`) VALUES (1047, 1013, 1035, 7, 'asd', 'asd', 'asdsd', 'asd', 'CERRADA', 'MEDIA', 8, 1013, NULL, 0, NULL, NULL, 1016, '2026-10-25 16:30:01', '2026-10-25 20:05:45');
INSERT INTO `historia` (`id`, `proyecto_id`, `fase_id`, `numero`, `titulo`, `como`, `quiero`, `para`, `estado`, `prioridad`, `puntos`, `analista_id`, `figma_url`, `diseno_aprobado`, `diseno_aprobado_por`, `diseno_aprobado_en`, `creado_por`, `creado_en`, `actualizado_en`) VALUES (1048, 1013, 1036, 8, 'asdassad', 'asdas', 'dasd', 'asdasd', 'EN_VALIDACION', 'ALTA', 5, 1013, NULL, 0, NULL, NULL, 1013, '2026-10-25 19:29:03', '2026-10-25 21:36:28');
INSERT INTO `historia` (`id`, `proyecto_id`, `fase_id`, `numero`, `titulo`, `como`, `quiero`, `para`, `estado`, `prioridad`, `puntos`, `analista_id`, `figma_url`, `diseno_aprobado`, `diseno_aprobado_por`, `diseno_aprobado_en`, `creado_por`, `creado_en`, `actualizado_en`) VALUES (1049, 1014, 1038, 1, 'asdasd', 'sad', 'sad', 'sad', 'INTEGRADA', 'MEDIA', 5, 1016, NULL, 0, NULL, NULL, 1016, '2026-10-25 21:38:27', '2026-10-25 22:17:17');
INSERT INTO `historia` (`id`, `proyecto_id`, `fase_id`, `numero`, `titulo`, `como`, `quiero`, `para`, `estado`, `prioridad`, `puntos`, `analista_id`, `figma_url`, `diseno_aprobado`, `diseno_aprobado_por`, `diseno_aprobado_en`, `creado_por`, `creado_en`, `actualizado_en`) VALUES (1050, 1015, 1040, 1, 'aaaaaaaaasdasd', 'asdas', 'das', 'dasdasd', 'CERRADA', 'MEDIA', NULL, 1013, NULL, 0, NULL, NULL, 1015, '2026-10-25 22:21:44', '2026-10-25 22:35:43');
INSERT INTO `historia` (`id`, `proyecto_id`, `fase_id`, `numero`, `titulo`, `como`, `quiero`, `para`, `estado`, `prioridad`, `puntos`, `analista_id`, `figma_url`, `diseno_aprobado`, `diseno_aprobado_por`, `diseno_aprobado_en`, `creado_por`, `creado_en`, `actualizado_en`) VALUES (1051, 1015, 1040, 2, 'addddddddddd', 'asdasd', 'asdas', 'dasd', 'CERRADA', 'ALTA', 5, 1014, NULL, 0, NULL, NULL, 1015, '2026-10-25 22:22:02', '2026-10-25 22:35:43');
INSERT INTO `historia` (`id`, `proyecto_id`, `fase_id`, `numero`, `titulo`, `como`, `quiero`, `para`, `estado`, `prioridad`, `puntos`, `analista_id`, `figma_url`, `diseno_aprobado`, `diseno_aprobado_por`, `diseno_aprobado_en`, `creado_por`, `creado_en`, `actualizado_en`) VALUES (1052, 1015, 1040, 3, 'asdasdasd', 'asdas', 'das', 'dasd', 'CERRADA', 'MEDIA', 8, 1013, NULL, 0, NULL, NULL, 1013, '2026-10-25 22:23:18', '2026-10-25 22:35:43');
INSERT INTO `historia` (`id`, `proyecto_id`, `fase_id`, `numero`, `titulo`, `como`, `quiero`, `para`, `estado`, `prioridad`, `puntos`, `analista_id`, `figma_url`, `diseno_aprobado`, `diseno_aprobado_por`, `diseno_aprobado_en`, `creado_por`, `creado_en`, `actualizado_en`) VALUES (1053, 1015, 1041, 4, 'sjdihajksdasd', 'uadmin', 'poer logean', 'ver repoesa', 'INTEGRADA', 'MEDIA', 5, 1014, NULL, 0, NULL, NULL, 1016, '2026-10-26 22:10:54', '2026-10-26 22:18:37');
INSERT INTO `historia` (`id`, `proyecto_id`, `fase_id`, `numero`, `titulo`, `como`, `quiero`, `para`, `estado`, `prioridad`, `puntos`, `analista_id`, `figma_url`, `diseno_aprobado`, `diseno_aprobado_por`, `diseno_aprobado_en`, `creado_por`, `creado_en`, `actualizado_en`) VALUES (1054, 1015, 1041, 5, 'asdasd', NULL, NULL, NULL, 'BORRADOR', 'MEDIA', NULL, 1016, NULL, 0, NULL, NULL, 1016, '2026-10-26 22:15:36', '2026-10-26 22:15:36');
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
) ENGINE=InnoDB AUTO_INCREMENT=1059 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Records of historia_criterio
-- ----------------------------
BEGIN;
INSERT INTO `historia_criterio` (`id`, `historia_id`, `descripcion`, `verificado`, `orden`) VALUES (1042, 1041, 'aaaaaa', 0, 1);
INSERT INTO `historia_criterio` (`id`, `historia_id`, `descripcion`, `verificado`, `orden`) VALUES (1043, 1042, 'asd', 0, 1);
INSERT INTO `historia_criterio` (`id`, `historia_id`, `descripcion`, `verificado`, `orden`) VALUES (1044, 1043, 'asd', 0, 1);
INSERT INTO `historia_criterio` (`id`, `historia_id`, `descripcion`, `verificado`, `orden`) VALUES (1045, 1044, 'asd', 0, 1);
INSERT INTO `historia_criterio` (`id`, `historia_id`, `descripcion`, `verificado`, `orden`) VALUES (1046, 1045, 'asdasd', 0, 1);
INSERT INTO `historia_criterio` (`id`, `historia_id`, `descripcion`, `verificado`, `orden`) VALUES (1047, 1046, 'asdd', 0, 1);
INSERT INTO `historia_criterio` (`id`, `historia_id`, `descripcion`, `verificado`, `orden`) VALUES (1048, 1047, 'asd', 1, 1);
INSERT INTO `historia_criterio` (`id`, `historia_id`, `descripcion`, `verificado`, `orden`) VALUES (1050, 1048, 'sadasd', 0, 1);
INSERT INTO `historia_criterio` (`id`, `historia_id`, `descripcion`, `verificado`, `orden`) VALUES (1051, 1049, 'sadasd', 0, 1);
INSERT INTO `historia_criterio` (`id`, `historia_id`, `descripcion`, `verificado`, `orden`) VALUES (1052, 1051, 'asdasd', 0, 1);
INSERT INTO `historia_criterio` (`id`, `historia_id`, `descripcion`, `verificado`, `orden`) VALUES (1053, 1050, 'asd', 0, 1);
INSERT INTO `historia_criterio` (`id`, `historia_id`, `descripcion`, `verificado`, `orden`) VALUES (1054, 1050, 'asdasd', 0, 2);
INSERT INTO `historia_criterio` (`id`, `historia_id`, `descripcion`, `verificado`, `orden`) VALUES (1055, 1052, 'asdasd', 0, 1);
INSERT INTO `historia_criterio` (`id`, `historia_id`, `descripcion`, `verificado`, `orden`) VALUES (1056, 1053, 'askdnasd , dasjkld', 1, 1);
INSERT INTO `historia_criterio` (`id`, `historia_id`, `descripcion`, `verificado`, `orden`) VALUES (1057, 1053, 'asdasdasd', 1, 2);
INSERT INTO `historia_criterio` (`id`, `historia_id`, `descripcion`, `verificado`, `orden`) VALUES (1058, 1053, 'sadasd', 1, 3);
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
INSERT INTO `historia_modulo` (`historia_id`, `modulo_id`) VALUES (1041, 1020);
INSERT INTO `historia_modulo` (`historia_id`, `modulo_id`) VALUES (1042, 1020);
INSERT INTO `historia_modulo` (`historia_id`, `modulo_id`) VALUES (1043, 1020);
INSERT INTO `historia_modulo` (`historia_id`, `modulo_id`) VALUES (1044, 1020);
INSERT INTO `historia_modulo` (`historia_id`, `modulo_id`) VALUES (1045, 1020);
INSERT INTO `historia_modulo` (`historia_id`, `modulo_id`) VALUES (1046, 1020);
INSERT INTO `historia_modulo` (`historia_id`, `modulo_id`) VALUES (1047, 1020);
INSERT INTO `historia_modulo` (`historia_id`, `modulo_id`) VALUES (1048, 1020);
INSERT INTO `historia_modulo` (`historia_id`, `modulo_id`) VALUES (1049, 1021);
INSERT INTO `historia_modulo` (`historia_id`, `modulo_id`) VALUES (1050, 1022);
INSERT INTO `historia_modulo` (`historia_id`, `modulo_id`) VALUES (1051, 1022);
INSERT INTO `historia_modulo` (`historia_id`, `modulo_id`) VALUES (1052, 1022);
INSERT INTO `historia_modulo` (`historia_id`, `modulo_id`) VALUES (1053, 1022);
INSERT INTO `historia_modulo` (`historia_id`, `modulo_id`) VALUES (1054, 1022);
COMMIT;

-- ----------------------------
-- Table structure for historia_regla_negocio
-- ----------------------------
DROP TABLE IF EXISTS `historia_regla_negocio`;
CREATE TABLE `historia_regla_negocio` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `historia_id` bigint NOT NULL,
  `descripcion` varchar(300) NOT NULL,
  `orden` int NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `fk_historia_regla_negocio_historia` (`historia_id`),
  CONSTRAINT `fk_historia_regla_negocio_historia` FOREIGN KEY (`historia_id`) REFERENCES `historia` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=42 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Records of historia_regla_negocio
-- ----------------------------
BEGIN;
INSERT INTO `historia_regla_negocio` (`id`, `historia_id`, `descripcion`, `orden`) VALUES (30, 1041, 'asdasd', 1);
INSERT INTO `historia_regla_negocio` (`id`, `historia_id`, `descripcion`, `orden`) VALUES (31, 1042, 'asdasd', 1);
INSERT INTO `historia_regla_negocio` (`id`, `historia_id`, `descripcion`, `orden`) VALUES (32, 1043, 'sad', 1);
INSERT INTO `historia_regla_negocio` (`id`, `historia_id`, `descripcion`, `orden`) VALUES (33, 1046, 'asdasd', 1);
INSERT INTO `historia_regla_negocio` (`id`, `historia_id`, `descripcion`, `orden`) VALUES (34, 1047, 'sad', 1);
INSERT INTO `historia_regla_negocio` (`id`, `historia_id`, `descripcion`, `orden`) VALUES (36, 1048, 'asdasd', 1);
INSERT INTO `historia_regla_negocio` (`id`, `historia_id`, `descripcion`, `orden`) VALUES (37, 1049, 'asdas', 1);
INSERT INTO `historia_regla_negocio` (`id`, `historia_id`, `descripcion`, `orden`) VALUES (38, 1051, 'asdasd', 1);
INSERT INTO `historia_regla_negocio` (`id`, `historia_id`, `descripcion`, `orden`) VALUES (39, 1050, 'asdasd', 1);
INSERT INTO `historia_regla_negocio` (`id`, `historia_id`, `descripcion`, `orden`) VALUES (40, 1052, 'asdasd', 1);
INSERT INTO `historia_regla_negocio` (`id`, `historia_id`, `descripcion`, `orden`) VALUES (41, 1053, 'asdasdsad', 1);
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
) ENGINE=InnoDB AUTO_INCREMENT=1461 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Records of historial_estado
-- ----------------------------
BEGIN;
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1174, 'FASE', 1033, NULL, 'PLANEADA', NULL, 0, 1015, '2026-10-25 14:50:25');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1175, 'HISTORIA', 1041, NULL, 'BORRADOR', NULL, 0, 1015, '2026-10-25 14:50:57');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1176, 'HISTORIA', 1041, 'BORRADOR', 'LISTA', NULL, 0, 1015, '2026-10-25 14:51:38');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1177, 'HISTORIA', 1042, NULL, 'BORRADOR', NULL, 0, 1013, '2026-10-25 14:52:34');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1178, 'HISTORIA', 1042, 'BORRADOR', 'LISTA', NULL, 0, 1013, '2026-10-25 14:53:11');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1179, 'HISTORIA', 1042, 'LISTA', 'BORRADOR', NULL, 0, 1013, '2026-10-25 14:53:13');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1180, 'HISTORIA', 1042, 'BORRADOR', 'LISTA', NULL, 0, 1013, '2026-10-25 14:53:13');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1181, 'TICKET', 1418, NULL, 'POR_HACER', NULL, 0, 1016, '2026-10-25 14:54:39');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1182, 'FASE', 1033, 'PLANEADA', 'EN_CURSO', NULL, 0, 1016, '2026-10-25 14:55:36');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1183, 'HISTORIA', 1042, 'LISTA', 'EN_DESARROLLO', NULL, 0, 1016, '2026-10-25 14:57:36');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1184, 'TICKET', 1419, NULL, 'POR_HACER', NULL, 0, 1016, '2026-10-25 14:57:55');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1185, 'TICKET', 1418, 'POR_HACER', 'EN_PROGRESO', NULL, 0, 1001, '2026-10-25 14:58:19');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1186, 'TICKET', 1418, 'EN_PROGRESO', 'EN_QA', 'dasdasdasd', 0, 1001, '2026-10-25 14:58:34');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1187, 'TICKET', 1418, 'EN_QA', 'EN_PROGRESO', 'asd', 0, 1001, '2026-10-25 14:58:42');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1188, 'TICKET', 1418, 'EN_PROGRESO', 'EN_QA', 'asdad', 0, 1001, '2026-10-25 14:58:47');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1189, 'TICKET', 1418, 'EN_QA', 'APROBADA', NULL, 0, 1013, '2026-10-25 14:59:19');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1190, 'HISTORIA', 1042, 'EN_DESARROLLO', 'EN_VALIDACION', 'Se aprobó el último ticket', 1, NULL, '2026-10-25 14:59:19');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1191, 'HISTORIA', 1041, 'LISTA', 'EN_DESARROLLO', NULL, 0, 1016, '2026-10-25 14:59:54');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1192, 'HISTORIA', 1042, 'EN_VALIDACION', 'ACEPTADA', NULL, 0, 1013, '2026-10-25 15:00:17');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1193, 'HISTORIA', 1042, 'ACEPTADA', 'INTEGRADA', NULL, 0, 1016, '2026-10-25 15:01:03');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1194, 'TICKET', 1418, 'APROBADA', 'INTEGRADA', 'Se integró a develop', 1, 1016, '2026-10-25 15:01:03');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1195, 'TICKET', 1419, 'POR_HACER', 'EN_PROGRESO', NULL, 0, 1001, '2026-10-25 15:02:01');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1196, 'TICKET', 1419, 'EN_PROGRESO', 'EN_QA', 'fggf', 0, 1001, '2026-10-25 15:02:05');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1197, 'TICKET', 1419, 'EN_QA', 'APROBADA', NULL, 0, 1013, '2026-10-25 15:02:27');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1198, 'HISTORIA', 1041, 'EN_DESARROLLO', 'EN_VALIDACION', 'Se aprobó el último ticket', 1, NULL, '2026-10-25 15:02:27');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1199, 'HISTORIA', 1041, 'EN_VALIDACION', 'ACEPTADA', NULL, 0, 1013, '2026-10-25 15:03:36');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1200, 'HISTORIA', 1041, 'ACEPTADA', 'INTEGRADA', NULL, 0, 1016, '2026-10-25 15:03:56');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1201, 'TICKET', 1419, 'APROBADA', 'INTEGRADA', 'Se integró a develop', 1, 1016, '2026-10-25 15:03:56');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1202, 'FASE', 1033, 'EN_CURSO', 'LISTA_PARA_RELEASE', 'Se cumplieron todos los criterios de cierre', 1, NULL, '2026-10-25 15:21:44');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1203, 'FASE', 1033, 'LISTA_PARA_RELEASE', 'EN_PUBLICACION', NULL, 0, 1016, '2026-10-25 15:22:28');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1204, 'FASE', 1033, 'EN_PUBLICACION', 'PUBLICADA', NULL, 0, 1016, '2026-10-25 15:22:30');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1205, 'HISTORIA', 1041, 'INTEGRADA', 'CERRADA', 'Publicado en v1.0.0', 1, 1016, '2026-10-25 15:22:30');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1206, 'HISTORIA', 1042, 'INTEGRADA', 'CERRADA', 'Publicado en v1.0.0', 1, 1016, '2026-10-25 15:22:30');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1208, 'TICKET', 1418, 'INTEGRADA', 'CERRADA', 'Publicado en v1.0.0', 1, 1016, '2026-10-25 15:22:30');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1209, 'TICKET', 1419, 'INTEGRADA', 'CERRADA', 'Publicado en v1.0.0', 1, 1016, '2026-10-25 15:22:30');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1211, 'FASE', 1034, NULL, 'PLANEADA', NULL, 0, 1016, '2026-10-25 15:42:36');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1212, 'HISTORIA', 1043, NULL, 'BORRADOR', NULL, 0, 1016, '2026-10-25 15:43:04');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1213, 'HISTORIA', 1043, 'BORRADOR', 'LISTA', NULL, 0, 1013, '2026-10-25 15:44:09');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1214, 'TICKET', 1420, NULL, 'POR_HACER', NULL, 0, 1016, '2026-10-25 15:44:43');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1215, 'TICKET', 1421, NULL, 'POR_HACER', NULL, 0, 1016, '2026-10-25 15:46:39');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1216, 'FASE', 1034, 'PLANEADA', 'EN_CURSO', NULL, 0, 1016, '2026-10-25 15:47:56');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1217, 'HISTORIA', 1043, 'LISTA', 'EN_DESARROLLO', NULL, 0, 1016, '2026-10-25 15:48:04');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1218, 'TICKET', 1421, 'POR_HACER', 'EN_PROGRESO', NULL, 0, 1001, '2026-10-25 15:53:56');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1219, 'TICKET', 1421, 'EN_PROGRESO', 'EN_REVISION', 'asdasd', 0, 1001, '2026-10-25 15:54:08');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1220, 'TICKET', 1421, 'EN_REVISION', 'APROBADA', NULL, 0, 1016, '2026-10-25 15:54:26');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1221, 'TICKET', 1420, 'POR_HACER', 'EN_PROGRESO', NULL, 0, 1001, '2026-10-25 15:54:53');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1222, 'TICKET', 1420, 'EN_PROGRESO', 'EN_REVISION', 'asdasd', 0, 1001, '2026-10-25 15:54:56');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1223, 'TICKET', 1420, 'EN_REVISION', 'APROBADA', NULL, 0, 1016, '2026-10-25 15:55:14');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1224, 'HISTORIA', 1043, 'EN_DESARROLLO', 'EN_QA', 'Se aprobó el último PR: pasa a QA', 1, NULL, '2026-10-25 15:55:14');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1225, 'HISTORIA', 1043, 'EN_QA', 'EN_VALIDACION', NULL, 0, 1013, '2026-10-25 16:00:38');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1226, 'HISTORIA', 1043, 'EN_VALIDACION', 'ACEPTADA', NULL, 0, 1013, '2026-10-25 16:00:54');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1227, 'HISTORIA', 1043, 'ACEPTADA', 'INTEGRADA', NULL, 0, 1016, '2026-10-25 16:04:07');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1228, 'TICKET', 1420, 'APROBADA', 'INTEGRADA', 'Se integró a develop', 1, 1016, '2026-10-25 16:04:07');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1229, 'TICKET', 1421, 'APROBADA', 'INTEGRADA', 'Se integró a develop', 1, 1016, '2026-10-25 16:04:07');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1231, 'FASE', 1034, 'EN_CURSO', 'LISTA_PARA_RELEASE', 'Se cumplieron todos los criterios de cierre', 1, NULL, '2026-10-25 16:04:59');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1232, 'FASE', 1034, 'LISTA_PARA_RELEASE', 'EN_PUBLICACION', NULL, 0, 1016, '2026-10-25 16:05:17');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1233, 'FASE', 1034, 'EN_PUBLICACION', 'PUBLICADA', NULL, 0, 1016, '2026-10-25 16:05:20');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1234, 'HISTORIA', 1043, 'INTEGRADA', 'CERRADA', 'Publicado en v1.1.0', 1, 1016, '2026-10-25 16:05:20');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1235, 'TICKET', 1420, 'INTEGRADA', 'CERRADA', 'Publicado en v1.1.0', 1, 1016, '2026-10-25 16:05:20');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1236, 'TICKET', 1421, 'INTEGRADA', 'CERRADA', 'Publicado en v1.1.0', 1, 1016, '2026-10-25 16:05:20');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1238, 'TICKET', 1422, NULL, 'REPORTADO', NULL, 0, 1016, '2026-10-25 16:06:38');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1239, 'TICKET', 1422, 'REPORTADO', 'CONFIRMADO', NULL, 0, 1016, '2026-10-25 16:07:15');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1240, 'TICKET', 1422, 'CONFIRMADO', 'EN_CORRECCION', NULL, 0, 1001, '2026-10-25 16:07:34');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1241, 'TICKET', 1422, 'EN_CORRECCION', 'EN_QA', 'dfsdfsd', 0, 1001, '2026-10-25 16:07:44');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1242, 'TICKET', 1422, 'EN_QA', 'APROBADO', 'Aprobado por el Líder: no había Tester disponible', 0, 1016, '2026-10-25 16:09:33');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1243, 'TICKET', 1422, 'APROBADO', 'PUBLICADO', 'Publicado en el parche v1.1.1', 0, 1016, '2026-10-25 16:09:36');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1244, 'FASE', 1035, NULL, 'PLANEADA', NULL, 0, 1016, '2026-10-25 16:14:04');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1245, 'HISTORIA', 1044, NULL, 'BORRADOR', NULL, 0, 1016, '2026-10-25 16:14:57');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1246, 'HISTORIA', 1045, NULL, 'BORRADOR', NULL, 0, 1016, '2026-10-25 16:19:41');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1247, 'HISTORIA', 1046, NULL, 'BORRADOR', NULL, 0, 1016, '2026-10-25 16:20:03');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1248, 'HISTORIA', 1046, 'BORRADOR', 'LISTA', NULL, 0, 1013, '2026-10-25 16:21:03');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1249, 'HISTORIA', 1045, 'BORRADOR', 'LISTA', NULL, 0, 1013, '2026-10-25 16:25:20');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1250, 'HISTORIA', 1044, 'BORRADOR', 'LISTA', NULL, 0, 1013, '2026-10-25 16:25:30');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1251, 'HISTORIA', 1047, NULL, 'BORRADOR', NULL, 0, 1016, '2026-10-25 16:30:01');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1252, 'FASE', 1035, 'PLANEADA', 'EN_CURSO', NULL, 0, 1016, '2026-10-25 16:30:15');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1253, 'TICKET', 1423, NULL, 'POR_HACER', NULL, 0, 1016, '2026-10-25 16:31:56');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1254, 'TICKET', 1424, NULL, 'POR_HACER', NULL, 0, 1016, '2026-10-25 16:32:30');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1255, 'HISTORIA', 1044, 'LISTA', 'EN_DESARROLLO', NULL, 0, 1016, '2026-10-25 16:33:07');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1256, 'HISTORIA', 1045, 'LISTA', 'EN_DESARROLLO', NULL, 0, 1016, '2026-10-25 16:33:11');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1257, 'TICKET', 1425, NULL, 'POR_HACER', NULL, 0, 1016, '2026-10-25 16:33:23');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1258, 'HISTORIA', 1046, 'LISTA', 'EN_DESARROLLO', NULL, 0, 1016, '2026-10-25 16:33:32');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1259, 'TICKET', 1425, 'POR_HACER', 'EN_PROGRESO', NULL, 0, 1001, '2026-10-25 16:34:38');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1260, 'TICKET', 1425, 'EN_PROGRESO', 'EN_REVISION', 'sadasd', 0, 1001, '2026-10-25 16:34:44');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1261, 'FASE', 1036, NULL, 'PLANEADA', NULL, 0, 1015, '2026-10-25 19:28:15');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1262, 'HISTORIA', 1048, NULL, 'BORRADOR', NULL, 0, 1013, '2026-10-25 19:29:04');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1263, 'TICKET', 1426, NULL, 'POR_HACER', NULL, 0, 1016, '2026-10-25 19:36:19');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1264, 'HISTORIA', 1048, 'BORRADOR', 'LISTA', NULL, 0, 1013, '2026-10-25 19:49:02');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1265, 'TICKET', 1424, 'POR_HACER', 'EN_PROGRESO', NULL, 0, 1001, '2026-10-25 19:50:18');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1266, 'TICKET', 1424, 'EN_PROGRESO', 'EN_REVISION', 'sad', 0, 1001, '2026-10-25 19:50:21');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1267, 'TICKET', 1425, 'EN_REVISION', 'APROBADA', NULL, 0, 1016, '2026-10-25 19:51:24');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1268, 'HISTORIA', 1046, 'EN_DESARROLLO', 'EN_QA', 'Se aprobó el último PR: pasa a QA', 1, NULL, '2026-10-25 19:51:24');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1269, 'HISTORIA', 1046, 'EN_QA', 'EN_VALIDACION', NULL, 0, 1013, '2026-10-25 19:51:58');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1270, 'HISTORIA', 1046, 'EN_VALIDACION', 'ACEPTADA', NULL, 0, 1013, '2026-10-25 19:52:15');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1271, 'HISTORIA', 1046, 'ACEPTADA', 'INTEGRADA', NULL, 0, 1016, '2026-10-25 19:53:23');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1272, 'TICKET', 1425, 'APROBADA', 'INTEGRADA', 'Se integró a develop', 1, 1016, '2026-10-25 19:53:23');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1273, 'TICKET', 1427, NULL, 'POR_HACER', NULL, 0, 1016, '2026-10-25 19:54:35');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1274, 'TICKET', 1424, 'EN_REVISION', 'APROBADA', NULL, 0, 1016, '2026-10-25 19:54:48');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1275, 'TICKET', 1428, NULL, 'POR_HACER', NULL, 0, 1016, '2026-10-25 19:55:13');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1276, 'TICKET', 1429, NULL, 'POR_HACER', NULL, 0, 1016, '2026-10-25 19:55:30');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1277, 'TICKET', 1427, 'POR_HACER', 'EN_PROGRESO', NULL, 0, 1001, '2026-10-25 19:56:02');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1278, 'TICKET', 1427, 'EN_PROGRESO', 'EN_REVISION', 'asd', 0, 1001, '2026-10-25 19:56:04');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1279, 'TICKET', 1423, 'POR_HACER', 'EN_PROGRESO', NULL, 0, 1001, '2026-10-25 19:56:07');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1280, 'TICKET', 1423, 'EN_PROGRESO', 'EN_REVISION', 'asdasdasd', 0, 1001, '2026-10-25 19:56:11');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1281, 'HISTORIA', 1047, 'BORRADOR', 'LISTA', NULL, 0, 1013, '2026-10-25 19:56:22');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1282, 'HISTORIA', 1047, 'LISTA', 'EN_DESARROLLO', NULL, 0, 1016, '2026-10-25 19:56:59');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1283, 'TICKET', 1423, 'EN_REVISION', 'APROBADA', NULL, 0, 1016, '2026-10-25 19:57:03');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1284, 'HISTORIA', 1044, 'EN_DESARROLLO', 'EN_QA', 'Se aprobó el último PR: pasa a QA', 1, NULL, '2026-10-25 19:57:03');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1285, 'TICKET', 1427, 'EN_REVISION', 'APROBADA', NULL, 0, 1016, '2026-10-25 19:57:07');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1286, 'HISTORIA', 1045, 'EN_DESARROLLO', 'EN_QA', 'Se aprobó el último PR: pasa a QA', 1, NULL, '2026-10-25 19:57:07');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1287, 'HISTORIA', 1045, 'EN_QA', 'EN_VALIDACION', NULL, 0, 1013, '2026-10-25 19:57:27');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1288, 'HISTORIA', 1045, 'EN_VALIDACION', 'ACEPTADA', NULL, 0, 1013, '2026-10-25 19:57:28');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1289, 'HISTORIA', 1044, 'EN_QA', 'EN_VALIDACION', NULL, 0, 1013, '2026-10-25 19:57:32');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1290, 'HISTORIA', 1044, 'EN_VALIDACION', 'ACEPTADA', NULL, 0, 1013, '2026-10-25 19:57:33');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1291, 'TICKET', 1429, 'POR_HACER', 'EN_PROGRESO', NULL, 0, 1001, '2026-10-25 19:58:41');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1292, 'TICKET', 1429, 'EN_PROGRESO', 'EN_REVISION', 'asd', 0, 1001, '2026-10-25 19:58:42');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1293, 'TICKET', 1429, 'EN_REVISION', 'APROBADA', NULL, 0, 1016, '2026-10-25 19:59:16');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1294, 'HISTORIA', 1047, 'EN_DESARROLLO', 'EN_QA', 'Se aprobó el último PR: pasa a QA', 1, NULL, '2026-10-25 19:59:16');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1295, 'HISTORIA', 1044, 'ACEPTADA', 'INTEGRADA', NULL, 0, 1016, '2026-10-25 19:59:32');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1296, 'TICKET', 1423, 'APROBADA', 'INTEGRADA', 'Se integró a develop', 1, 1016, '2026-10-25 19:59:32');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1297, 'HISTORIA', 1045, 'ACEPTADA', 'INTEGRADA', NULL, 0, 1016, '2026-10-25 19:59:46');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1298, 'TICKET', 1424, 'APROBADA', 'INTEGRADA', 'Se integró a develop', 1, 1016, '2026-10-25 19:59:46');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1299, 'TICKET', 1427, 'APROBADA', 'INTEGRADA', 'Se integró a develop', 1, 1016, '2026-10-25 19:59:46');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1301, 'HISTORIA', 1047, 'EN_QA', 'EN_VALIDACION', NULL, 0, 1013, '2026-10-25 20:00:24');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1302, 'HISTORIA', 1047, 'EN_VALIDACION', 'ACEPTADA', NULL, 0, 1013, '2026-10-25 20:00:44');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1303, 'HISTORIA', 1047, 'ACEPTADA', 'INTEGRADA', NULL, 0, 1016, '2026-10-25 20:01:07');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1304, 'TICKET', 1429, 'APROBADA', 'INTEGRADA', 'Se integró a develop', 1, 1016, '2026-10-25 20:01:07');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1305, 'FASE', 1035, 'EN_CURSO', 'LISTA_PARA_RELEASE', NULL, 0, 1016, '2026-10-25 20:05:33');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1306, 'FASE', 1035, 'LISTA_PARA_RELEASE', 'EN_PUBLICACION', NULL, 0, 1016, '2026-10-25 20:05:44');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1307, 'FASE', 1035, 'EN_PUBLICACION', 'PUBLICADA', NULL, 0, 1016, '2026-10-25 20:05:46');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1308, 'HISTORIA', 1044, 'INTEGRADA', 'CERRADA', 'Publicado en v1.2.0', 1, 1016, '2026-10-25 20:05:46');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1309, 'HISTORIA', 1045, 'INTEGRADA', 'CERRADA', 'Publicado en v1.2.0', 1, 1016, '2026-10-25 20:05:46');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1310, 'HISTORIA', 1046, 'INTEGRADA', 'CERRADA', 'Publicado en v1.2.0', 1, 1016, '2026-10-25 20:05:46');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1311, 'HISTORIA', 1047, 'INTEGRADA', 'CERRADA', 'Publicado en v1.2.0', 1, 1016, '2026-10-25 20:05:46');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1315, 'TICKET', 1423, 'INTEGRADA', 'CERRADA', 'Publicado en v1.2.0', 1, 1016, '2026-10-25 20:05:46');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1316, 'TICKET', 1424, 'INTEGRADA', 'CERRADA', 'Publicado en v1.2.0', 1, 1016, '2026-10-25 20:05:46');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1317, 'TICKET', 1425, 'INTEGRADA', 'CERRADA', 'Publicado en v1.2.0', 1, 1016, '2026-10-25 20:05:46');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1318, 'TICKET', 1427, 'INTEGRADA', 'CERRADA', 'Publicado en v1.2.0', 1, 1016, '2026-10-25 20:05:46');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1319, 'TICKET', 1429, 'INTEGRADA', 'CERRADA', 'Publicado en v1.2.0', 1, 1016, '2026-10-25 20:05:46');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1322, 'FASE', 1036, 'PLANEADA', 'EN_CURSO', NULL, 0, 1016, '2026-10-25 20:06:00');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1323, 'HISTORIA', 1048, 'LISTA', 'EN_DESARROLLO', 'Se inició con la fase', 1, 1016, '2026-10-25 20:06:00');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1324, 'TICKET', 1428, 'POR_HACER', 'EN_PROGRESO', NULL, 0, 1001, '2026-10-25 20:43:21');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1325, 'TICKET', 1428, 'EN_PROGRESO', 'EN_REVISION', 'asdasd', 0, 1001, '2026-10-25 20:43:23');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1326, 'TICKET', 1426, 'POR_HACER', 'EN_PROGRESO', NULL, 0, 1001, '2026-10-25 20:43:30');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1327, 'TICKET', 1426, 'EN_PROGRESO', 'EN_REVISION', 'sadasd', 0, 1001, '2026-10-25 21:31:48');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1328, 'TICKET', 1426, 'EN_REVISION', 'APROBADA', NULL, 0, 1016, '2026-10-25 21:36:25');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1329, 'TICKET', 1428, 'EN_REVISION', 'APROBADA', NULL, 0, 1016, '2026-10-25 21:36:29');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1330, 'HISTORIA', 1048, 'EN_DESARROLLO', 'EN_VALIDACION', 'Se aprobó el último PR: pasa a validación', 1, NULL, '2026-10-25 21:36:29');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1331, 'FASE', 1038, NULL, 'PLANEADA', NULL, 0, 1016, '2026-10-25 21:38:09');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1332, 'HISTORIA', 1049, NULL, 'BORRADOR', NULL, 0, 1016, '2026-10-25 21:38:28');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1333, 'HISTORIA', 1049, 'BORRADOR', 'EN_PLANEACION', NULL, 0, 1013, '2026-10-25 21:39:05');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1334, 'HISTORIA', 1049, 'EN_PLANEACION', 'LISTA', NULL, 0, 1016, '2026-10-25 21:46:03');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1335, 'TICKET', 1430, NULL, 'POR_HACER', NULL, 0, 1016, '2026-10-25 21:47:37');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1336, 'HISTORIA', 1049, 'LISTA', 'EN_PLANEACION', NULL, 0, 1016, '2026-10-25 21:55:19');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1337, 'HISTORIA', 1049, 'EN_PLANEACION', 'LISTA', NULL, 0, 1016, '2026-10-25 21:55:20');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1338, 'TICKET', 1431, NULL, 'POR_HACER', NULL, 0, 1016, '2026-10-25 21:56:03');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1339, 'HISTORIA', 1049, 'LISTA', 'LISTA_DESARROLLO', 'Ya tiene su Actividad', 1, NULL, '2026-10-25 21:56:50');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1340, 'TICKET', 1432, NULL, 'POR_HACER', NULL, 0, 1016, '2026-10-25 22:10:04');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1341, 'FASE', 1038, 'PLANEADA', 'EN_CURSO', NULL, 0, 1015, '2026-10-25 22:12:31');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1342, 'HISTORIA', 1049, 'LISTA_DESARROLLO', 'EN_DESARROLLO', 'Se inició con la fase', 1, 1015, '2026-10-25 22:12:31');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1343, 'TICKET', 1432, 'POR_HACER', 'EN_PROGRESO', NULL, 0, 1001, '2026-10-25 22:13:43');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1344, 'TICKET', 1432, 'EN_PROGRESO', 'EN_REVISION', 'asd', 0, 1001, '2026-10-25 22:13:51');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1345, 'TICKET', 1432, 'EN_REVISION', 'APROBADA', NULL, 0, 1001, '2026-10-25 22:14:22');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1346, 'TICKET', 1430, 'POR_HACER', 'EN_PROGRESO', NULL, 0, 1001, '2026-10-25 22:14:30');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1347, 'TICKET', 1430, 'EN_PROGRESO', 'EN_REVISION', 'dasdasd', 0, 1001, '2026-10-25 22:14:34');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1348, 'TICKET', 1430, 'EN_REVISION', 'APROBADA', NULL, 0, 1001, '2026-10-25 22:15:09');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1349, 'TICKET', 1431, 'POR_HACER', 'EN_PROGRESO', NULL, 0, 1014, '2026-10-25 22:15:34');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1350, 'TICKET', 1431, 'EN_PROGRESO', 'EN_REVISION', 'asd', 0, 1014, '2026-10-25 22:15:36');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1351, 'TICKET', 1431, 'EN_REVISION', 'APROBADA', NULL, 0, 1001, '2026-10-25 22:15:59');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1352, 'HISTORIA', 1049, 'EN_DESARROLLO', 'EN_VALIDACION', 'Se aprobó el último PR: pasa a validación', 1, NULL, '2026-10-25 22:15:59');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1353, 'HISTORIA', 1049, 'EN_VALIDACION', 'ACEPTADA', NULL, 0, 1015, '2026-10-25 22:17:10');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1354, 'HISTORIA', 1049, 'ACEPTADA', 'INTEGRADA', NULL, 0, 1015, '2026-10-25 22:17:18');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1355, 'TICKET', 1430, 'APROBADA', 'INTEGRADA', 'Se integró a develop', 1, 1015, '2026-10-25 22:17:18');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1356, 'TICKET', 1431, 'APROBADA', 'INTEGRADA', 'Se integró a develop', 1, 1015, '2026-10-25 22:17:18');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1357, 'TICKET', 1432, 'APROBADA', 'INTEGRADA', 'Se integró a develop', 1, 1015, '2026-10-25 22:17:18');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1358, 'FASE', 1040, NULL, 'PLANEADA', NULL, 0, 1015, '2026-10-25 22:21:23');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1359, 'HISTORIA', 1050, NULL, 'BORRADOR', NULL, 0, 1015, '2026-10-25 22:21:45');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1360, 'HISTORIA', 1051, NULL, 'BORRADOR', NULL, 0, 1015, '2026-10-25 22:22:02');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1361, 'HISTORIA', 1051, 'BORRADOR', 'EN_PLANEACION', NULL, 0, 1014, '2026-10-25 22:22:14');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1362, 'HISTORIA', 1051, 'EN_PLANEACION', 'LISTA', NULL, 0, 1014, '2026-10-25 22:22:38');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1363, 'HISTORIA', 1050, 'BORRADOR', 'EN_PLANEACION', NULL, 0, 1013, '2026-10-25 22:22:53');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1364, 'HISTORIA', 1052, NULL, 'BORRADOR', NULL, 0, 1013, '2026-10-25 22:23:18');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1365, 'HISTORIA', 1052, 'BORRADOR', 'EN_PLANEACION', NULL, 0, 1013, '2026-10-25 22:23:31');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1366, 'HISTORIA', 1052, 'EN_PLANEACION', 'LISTA', NULL, 0, 1013, '2026-10-25 22:23:44');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1367, 'HISTORIA', 1050, 'EN_PLANEACION', 'LISTA', NULL, 0, 1013, '2026-10-25 22:23:47');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1368, 'TICKET', 1433, NULL, 'POR_HACER', NULL, 0, 1016, '2026-10-25 22:24:57');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1369, 'HISTORIA', 1051, 'LISTA', 'LISTA_DESARROLLO', 'Ya tiene su Actividad', 1, 1016, '2026-10-25 22:24:57');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1370, 'TICKET', 1434, NULL, 'POR_HACER', NULL, 0, 1016, '2026-10-25 22:25:16');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1371, 'HISTORIA', 1050, 'LISTA', 'LISTA_DESARROLLO', 'Ya tiene su Actividad', 1, 1016, '2026-10-25 22:25:16');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1372, 'TICKET', 1435, NULL, 'POR_HACER', NULL, 0, 1016, '2026-10-25 22:25:41');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1373, 'HISTORIA', 1052, 'LISTA', 'LISTA_DESARROLLO', 'Ya tiene su Actividad', 1, 1016, '2026-10-25 22:25:41');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1374, 'HISTORIA', 1050, 'LISTA_DESARROLLO', 'EN_PLANEACION', NULL, 0, 1016, '2026-10-25 22:25:58');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1375, 'HISTORIA', 1051, 'LISTA_DESARROLLO', 'EN_PLANEACION', NULL, 0, 1016, '2026-10-25 22:26:02');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1376, 'HISTORIA', 1052, 'LISTA_DESARROLLO', 'EN_PLANEACION', NULL, 0, 1016, '2026-10-25 22:26:06');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1377, 'HISTORIA', 1050, 'EN_PLANEACION', 'LISTA', NULL, 0, 1016, '2026-10-25 22:26:11');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1378, 'HISTORIA', 1050, 'LISTA', 'EN_PLANEACION', NULL, 0, 1016, '2026-10-25 22:26:15');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1379, 'HISTORIA', 1050, 'EN_PLANEACION', 'LISTA', NULL, 0, 1016, '2026-10-25 22:26:19');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1380, 'HISTORIA', 1051, 'EN_PLANEACION', 'LISTA', NULL, 0, 1016, '2026-10-25 22:26:38');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1381, 'HISTORIA', 1052, 'EN_PLANEACION', 'LISTA', NULL, 0, 1016, '2026-10-25 22:26:43');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1382, 'TICKET', 1436, NULL, 'POR_HACER', NULL, 0, 1016, '2026-10-25 22:27:08');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1383, 'HISTORIA', 1050, 'LISTA', 'LISTA_DESARROLLO', 'Ya tiene su Actividad', 1, 1016, '2026-10-25 22:27:08');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1384, 'FASE', 1040, 'PLANEADA', 'EN_CURSO', NULL, 0, 1016, '2026-10-25 22:27:15');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1385, 'HISTORIA', 1050, 'LISTA_DESARROLLO', 'EN_DESARROLLO', 'Se inició con la fase', 1, 1016, '2026-10-25 22:27:15');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1386, 'HISTORIA', 1051, 'LISTA', 'EN_PLANEACION', NULL, 0, 1016, '2026-10-25 22:29:04');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1387, 'HISTORIA', 1051, 'EN_PLANEACION', 'LISTA', NULL, 0, 1016, '2026-10-25 22:29:05');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1388, 'TICKET', 1437, NULL, 'POR_HACER', NULL, 0, 1016, '2026-10-25 22:29:14');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1389, 'HISTORIA', 1051, 'LISTA', 'EN_DESARROLLO', 'Ya tiene su Actividad', 1, 1016, '2026-10-25 22:29:14');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1390, 'HISTORIA', 1052, 'LISTA', 'EN_PLANEACION', NULL, 0, 1016, '2026-10-25 22:29:58');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1391, 'HISTORIA', 1052, 'EN_PLANEACION', 'LISTA', NULL, 0, 1016, '2026-10-25 22:29:59');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1392, 'TICKET', 1438, NULL, 'POR_HACER', NULL, 0, 1016, '2026-10-25 22:30:57');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1393, 'HISTORIA', 1052, 'LISTA', 'EN_DESARROLLO', 'Ya tiene su Actividad', 1, 1016, '2026-10-25 22:30:57');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1394, 'TICKET', 1435, 'POR_HACER', 'EN_PROGRESO', NULL, 0, 1014, '2026-10-25 22:31:18');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1395, 'TICKET', 1435, 'EN_PROGRESO', 'EN_REVISION', 'asdasd', 0, 1014, '2026-10-25 22:31:21');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1396, 'TICKET', 1438, 'POR_HACER', 'EN_PROGRESO', NULL, 0, 1001, '2026-10-25 22:31:31');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1397, 'TICKET', 1438, 'EN_PROGRESO', 'EN_REVISION', 'asdasd', 0, 1001, '2026-10-25 22:31:34');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1398, 'TICKET', 1437, 'POR_HACER', 'EN_PROGRESO', NULL, 0, 1001, '2026-10-25 22:31:37');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1399, 'TICKET', 1437, 'EN_PROGRESO', 'EN_REVISION', 'asdasd', 0, 1001, '2026-10-25 22:31:40');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1400, 'TICKET', 1436, 'POR_HACER', 'EN_PROGRESO', NULL, 0, 1001, '2026-10-25 22:31:44');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1401, 'TICKET', 1436, 'EN_PROGRESO', 'EN_REVISION', 'asdasdasd', 0, 1001, '2026-10-25 22:31:47');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1402, 'TICKET', 1434, 'POR_HACER', 'EN_PROGRESO', NULL, 0, 1001, '2026-10-25 22:31:51');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1403, 'TICKET', 1434, 'EN_PROGRESO', 'EN_REVISION', 'asdasdasd', 0, 1001, '2026-10-25 22:31:53');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1404, 'TICKET', 1433, 'POR_HACER', 'EN_PROGRESO', NULL, 0, 1001, '2026-10-25 22:31:57');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1405, 'TICKET', 1433, 'EN_PROGRESO', 'EN_REVISION', 'asdasdasd', 0, 1001, '2026-10-25 22:32:01');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1406, 'TICKET', 1433, 'EN_REVISION', 'APROBADA', NULL, 0, 1016, '2026-10-25 22:32:34');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1407, 'TICKET', 1434, 'EN_REVISION', 'APROBADA', NULL, 0, 1016, '2026-10-25 22:32:40');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1408, 'TICKET', 1436, 'EN_REVISION', 'APROBADA', NULL, 0, 1016, '2026-10-25 22:32:42');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1409, 'HISTORIA', 1050, 'EN_DESARROLLO', 'EN_VALIDACION', 'Se aprobó el último PR: pasa a validación', 1, NULL, '2026-10-25 22:32:42');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1410, 'TICKET', 1437, 'EN_REVISION', 'APROBADA', NULL, 0, 1016, '2026-10-25 22:32:45');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1411, 'HISTORIA', 1051, 'EN_DESARROLLO', 'EN_VALIDACION', 'Se aprobó el último PR: pasa a validación', 1, NULL, '2026-10-25 22:32:45');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1412, 'TICKET', 1438, 'EN_REVISION', 'APROBADA', NULL, 0, 1016, '2026-10-25 22:32:48');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1413, 'TICKET', 1435, 'EN_REVISION', 'APROBADA', NULL, 0, 1016, '2026-10-25 22:32:51');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1414, 'HISTORIA', 1052, 'EN_DESARROLLO', 'EN_VALIDACION', 'Se aprobó el último PR: pasa a validación', 1, NULL, '2026-10-25 22:32:51');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1415, 'HISTORIA', 1051, 'EN_VALIDACION', 'ACEPTADA', NULL, 0, 1014, '2026-10-25 22:34:18');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1416, 'HISTORIA', 1052, 'EN_VALIDACION', 'ACEPTADA', NULL, 0, 1013, '2026-10-25 22:34:28');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1417, 'HISTORIA', 1050, 'EN_VALIDACION', 'ACEPTADA', NULL, 0, 1013, '2026-10-25 22:34:31');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1418, 'HISTORIA', 1050, 'ACEPTADA', 'INTEGRADA', NULL, 0, 1016, '2026-10-25 22:34:49');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1419, 'TICKET', 1434, 'APROBADA', 'INTEGRADA', 'Se integró a develop', 1, 1016, '2026-10-25 22:34:49');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1420, 'TICKET', 1436, 'APROBADA', 'INTEGRADA', 'Se integró a develop', 1, 1016, '2026-10-25 22:34:49');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1422, 'HISTORIA', 1051, 'ACEPTADA', 'INTEGRADA', NULL, 0, 1016, '2026-10-25 22:34:57');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1423, 'TICKET', 1433, 'APROBADA', 'INTEGRADA', 'Se integró a develop', 1, 1016, '2026-10-25 22:34:57');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1424, 'TICKET', 1437, 'APROBADA', 'INTEGRADA', 'Se integró a develop', 1, 1016, '2026-10-25 22:34:57');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1426, 'HISTORIA', 1052, 'ACEPTADA', 'INTEGRADA', NULL, 0, 1016, '2026-10-25 22:35:08');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1427, 'TICKET', 1435, 'APROBADA', 'INTEGRADA', 'Se integró a develop', 1, 1016, '2026-10-25 22:35:08');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1428, 'TICKET', 1438, 'APROBADA', 'INTEGRADA', 'Se integró a develop', 1, 1016, '2026-10-25 22:35:08');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1430, 'FASE', 1040, 'EN_CURSO', 'LISTA_PARA_RELEASE', 'Se cumplieron todos los criterios de cierre', 1, NULL, '2026-10-25 22:35:27');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1431, 'FASE', 1040, 'LISTA_PARA_RELEASE', 'EN_PUBLICACION', NULL, 0, 1016, '2026-10-25 22:35:41');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1432, 'FASE', 1040, 'EN_PUBLICACION', 'PUBLICADA', NULL, 0, 1016, '2026-10-25 22:35:44');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1433, 'HISTORIA', 1050, 'INTEGRADA', 'CERRADA', 'Publicado en v1.0.0', 1, 1016, '2026-10-25 22:35:44');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1434, 'HISTORIA', 1051, 'INTEGRADA', 'CERRADA', 'Publicado en v1.0.0', 1, 1016, '2026-10-25 22:35:44');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1435, 'HISTORIA', 1052, 'INTEGRADA', 'CERRADA', 'Publicado en v1.0.0', 1, 1016, '2026-10-25 22:35:44');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1436, 'TICKET', 1433, 'INTEGRADA', 'CERRADA', 'Publicado en v1.0.0', 1, 1016, '2026-10-25 22:35:44');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1437, 'TICKET', 1434, 'INTEGRADA', 'CERRADA', 'Publicado en v1.0.0', 1, 1016, '2026-10-25 22:35:44');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1438, 'TICKET', 1435, 'INTEGRADA', 'CERRADA', 'Publicado en v1.0.0', 1, 1016, '2026-10-25 22:35:44');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1439, 'TICKET', 1436, 'INTEGRADA', 'CERRADA', 'Publicado en v1.0.0', 1, 1016, '2026-10-25 22:35:44');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1440, 'TICKET', 1437, 'INTEGRADA', 'CERRADA', 'Publicado en v1.0.0', 1, 1016, '2026-10-25 22:35:44');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1441, 'TICKET', 1438, 'INTEGRADA', 'CERRADA', 'Publicado en v1.0.0', 1, 1016, '2026-10-25 22:35:44');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1442, 'FASE', 1041, NULL, 'PLANEADA', NULL, 0, 1016, '2026-10-26 22:10:09');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1443, 'HISTORIA', 1053, NULL, 'BORRADOR', NULL, 0, 1016, '2026-10-26 22:10:55');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1444, 'HISTORIA', 1053, 'BORRADOR', 'EN_PLANEACION', NULL, 0, 1014, '2026-10-26 22:11:09');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1445, 'HISTORIA', 1053, 'EN_PLANEACION', 'LISTA', NULL, 0, 1014, '2026-10-26 22:12:24');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1446, 'HISTORIA', 1053, 'LISTA', 'EN_PLANEACION', NULL, 0, 1016, '2026-10-26 22:14:16');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1447, 'HISTORIA', 1053, 'EN_PLANEACION', 'LISTA', NULL, 0, 1014, '2026-10-26 22:14:28');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1448, 'TICKET', 1439, NULL, 'BORRADOR', NULL, 0, 1016, '2026-10-26 22:15:20');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1449, 'HISTORIA', 1054, NULL, 'BORRADOR', NULL, 0, 1016, '2026-10-26 22:15:37');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1450, 'TICKET', 1439, 'BORRADOR', 'POR_HACER', NULL, 0, 1016, '2026-10-26 22:15:54');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1451, 'HISTORIA', 1053, 'LISTA', 'LISTA_DESARROLLO', 'Ya tiene una Actividad Lista', 1, 1016, '2026-10-26 22:15:54');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1452, 'FASE', 1041, 'PLANEADA', 'EN_CURSO', NULL, 0, 1016, '2026-10-26 22:16:14');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1453, 'HISTORIA', 1053, 'LISTA_DESARROLLO', 'EN_DESARROLLO', 'Se inició con la fase', 1, 1016, '2026-10-26 22:16:14');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1454, 'TICKET', 1439, 'POR_HACER', 'EN_PROGRESO', NULL, 0, 1001, '2026-10-26 22:16:28');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1455, 'TICKET', 1439, 'EN_PROGRESO', 'EN_REVISION', 'njdskfnksdf', 0, 1001, '2026-10-26 22:17:07');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1456, 'TICKET', 1439, 'EN_REVISION', 'APROBADA', NULL, 0, 1016, '2026-10-26 22:17:21');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1457, 'HISTORIA', 1053, 'EN_DESARROLLO', 'EN_VALIDACION', 'Se aprobó el último PR: pasa a validación', 1, NULL, '2026-10-26 22:17:21');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1458, 'HISTORIA', 1053, 'EN_VALIDACION', 'ACEPTADA', NULL, 0, 1014, '2026-10-26 22:18:15');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1459, 'HISTORIA', 1053, 'ACEPTADA', 'INTEGRADA', NULL, 0, 1016, '2026-10-26 22:18:37');
INSERT INTO `historial_estado` (`id`, `entidad`, `entidad_id`, `estado_anterior`, `estado_nuevo`, `motivo`, `automatico`, `usuario_id`, `creado_en`) VALUES (1460, 'TICKET', 1439, 'APROBADA', 'INTEGRADA', 'Se integró a develop', 1, 1016, '2026-10-26 22:18:37');
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
) ENGINE=InnoDB AUTO_INCREMENT=1023 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Records of modulo
-- ----------------------------
BEGIN;
INSERT INTO `modulo` (`id`, `proyecto_id`, `tipo`, `nombre`, `tecnologia`, `repositorio`, `rama_base`, `responsable_id`) VALUES (1020, 1013, 'API', 'a', 'sadasd', 'asdasd', 'develop', 1001);
INSERT INTO `modulo` (`id`, `proyecto_id`, `tipo`, `nombre`, `tecnologia`, `repositorio`, `rama_base`, `responsable_id`) VALUES (1021, 1014, 'API', 'asd', 'asd', 'asdasd', 'develop', 1001);
INSERT INTO `modulo` (`id`, `proyecto_id`, `tipo`, `nombre`, `tecnologia`, `repositorio`, `rama_base`, `responsable_id`) VALUES (1022, 1015, 'API', 'asd', 'asdasd', 'asssd', 'develop', NULL);
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
) ENGINE=InnoDB AUTO_INCREMENT=1016 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Records of proyecto
-- ----------------------------
BEGIN;
INSERT INTO `proyecto` (`id`, `codigo`, `nombre`, `descripcion`, `cliente_id`, `prioridad`, `estado`, `fecha_inicio`, `fecha_entrega`, `lider_id`, `permitir_autovalidacion`, `comentario_al_rechazar`, `rama_al_iniciar`, `notificar_cliente_fase`, `creado_en`, `actualizado_en`) VALUES (1013, 'SA', 'sadasd', 'sadasd', NULL, 'MEDIA', 'PLANEACION', NULL, NULL, 1016, 1, 1, 1, 0, '2026-10-25 14:48:56', '2026-10-25 14:48:56');
INSERT INTO `proyecto` (`id`, `codigo`, `nombre`, `descripcion`, `cliente_id`, `prioridad`, `estado`, `fecha_inicio`, `fecha_entrega`, `lider_id`, `permitir_autovalidacion`, `comentario_al_rechazar`, `rama_al_iniciar`, `notificar_cliente_fase`, `creado_en`, `actualizado_en`) VALUES (1014, 'SD', 'sdsadasd', 'asdasd', 1001, 'MEDIA', 'PLANEACION', NULL, NULL, 1001, 1, 1, 1, 0, '2026-10-25 21:35:44', '2026-10-25 21:35:44');
INSERT INTO `proyecto` (`id`, `codigo`, `nombre`, `descripcion`, `cliente_id`, `prioridad`, `estado`, `fecha_inicio`, `fecha_entrega`, `lider_id`, `permitir_autovalidacion`, `comentario_al_rechazar`, `rama_al_iniciar`, `notificar_cliente_fase`, `creado_en`, `actualizado_en`) VALUES (1015, 'AA', 'aaaaaaaaaaaaaaaaaa', 'asd', 1001, 'BAJA', 'PLANEACION', NULL, NULL, 1016, 1, 1, 1, 0, '2026-10-25 22:20:21', '2026-10-25 22:20:21');
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
INSERT INTO `proyecto_miembro` (`proyecto_id`, `usuario_id`, `agregado_en`) VALUES (1013, 1001, '2026-10-25 14:48:56');
INSERT INTO `proyecto_miembro` (`proyecto_id`, `usuario_id`, `agregado_en`) VALUES (1013, 1013, '2026-10-25 14:48:56');
INSERT INTO `proyecto_miembro` (`proyecto_id`, `usuario_id`, `agregado_en`) VALUES (1013, 1014, '2026-10-25 14:48:56');
INSERT INTO `proyecto_miembro` (`proyecto_id`, `usuario_id`, `agregado_en`) VALUES (1013, 1015, '2026-10-25 14:48:56');
INSERT INTO `proyecto_miembro` (`proyecto_id`, `usuario_id`, `agregado_en`) VALUES (1013, 1016, '2026-10-25 14:48:56');
INSERT INTO `proyecto_miembro` (`proyecto_id`, `usuario_id`, `agregado_en`) VALUES (1014, 1001, '2026-10-25 21:35:44');
INSERT INTO `proyecto_miembro` (`proyecto_id`, `usuario_id`, `agregado_en`) VALUES (1014, 1013, '2026-10-25 21:36:56');
INSERT INTO `proyecto_miembro` (`proyecto_id`, `usuario_id`, `agregado_en`) VALUES (1014, 1014, '2026-10-25 21:36:59');
INSERT INTO `proyecto_miembro` (`proyecto_id`, `usuario_id`, `agregado_en`) VALUES (1014, 1015, '2026-10-25 21:36:53');
INSERT INTO `proyecto_miembro` (`proyecto_id`, `usuario_id`, `agregado_en`) VALUES (1014, 1016, '2026-10-25 21:37:02');
INSERT INTO `proyecto_miembro` (`proyecto_id`, `usuario_id`, `agregado_en`) VALUES (1015, 1001, '2026-10-25 22:20:21');
INSERT INTO `proyecto_miembro` (`proyecto_id`, `usuario_id`, `agregado_en`) VALUES (1015, 1013, '2026-10-25 22:20:21');
INSERT INTO `proyecto_miembro` (`proyecto_id`, `usuario_id`, `agregado_en`) VALUES (1015, 1014, '2026-10-25 22:20:21');
INSERT INTO `proyecto_miembro` (`proyecto_id`, `usuario_id`, `agregado_en`) VALUES (1015, 1016, '2026-10-25 22:20:21');
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
) ENGINE=InnoDB AUTO_INCREMENT=1440 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Records of ticket
-- ----------------------------
BEGIN;
INSERT INTO `ticket` (`id`, `proyecto_id`, `fase_id`, `historia_id`, `tipo`, `numero`, `titulo`, `descripcion`, `estado`, `prioridad`, `severidad`, `modulo_id`, `responsable_id`, `tester_id`, `creado_por`, `estimacion_horas`, `horas_registradas`, `fecha_limite`, `rama`, `capas`, `historia_origen_id`, `version_reportada`, `bloqueado_por_id`, `creado_en`, `actualizado_en`) VALUES (1418, 1013, 1033, 1042, 'ACTV', 1, 'asdas', 'dasdasd', 'CERRADA', 'MEDIA', NULL, 1020, 1001, 1013, 1016, NULL, 0.0, '2026-10-30', 'feature/actv-01-asdas', NULL, NULL, NULL, NULL, '2026-10-25 14:54:39', '2026-10-25 15:22:29');
INSERT INTO `ticket` (`id`, `proyecto_id`, `fase_id`, `historia_id`, `tipo`, `numero`, `titulo`, `descripcion`, `estado`, `prioridad`, `severidad`, `modulo_id`, `responsable_id`, `tester_id`, `creado_por`, `estimacion_horas`, `horas_registradas`, `fecha_limite`, `rama`, `capas`, `historia_origen_id`, `version_reportada`, `bloqueado_por_id`, `creado_en`, `actualizado_en`) VALUES (1419, 1013, 1033, 1041, 'ACTV', 2, 'sdad', 'asd', 'CERRADA', 'MEDIA', NULL, 1020, 1001, 1013, 1016, NULL, 0.0, NULL, 'feature/actv-02-sdad', NULL, NULL, NULL, NULL, '2026-10-25 14:57:55', '2026-10-25 15:22:29');
INSERT INTO `ticket` (`id`, `proyecto_id`, `fase_id`, `historia_id`, `tipo`, `numero`, `titulo`, `descripcion`, `estado`, `prioridad`, `severidad`, `modulo_id`, `responsable_id`, `tester_id`, `creado_por`, `estimacion_horas`, `horas_registradas`, `fecha_limite`, `rama`, `capas`, `historia_origen_id`, `version_reportada`, `bloqueado_por_id`, `creado_en`, `actualizado_en`) VALUES (1420, 1013, 1034, 1043, 'ACTV', 3, 'asd', 'asd', 'CERRADA', 'MEDIA', NULL, 1020, 1001, NULL, 1016, NULL, 0.0, NULL, 'feature/actv-03-asd', NULL, NULL, NULL, NULL, '2026-10-25 15:44:42', '2026-10-25 16:05:19');
INSERT INTO `ticket` (`id`, `proyecto_id`, `fase_id`, `historia_id`, `tipo`, `numero`, `titulo`, `descripcion`, `estado`, `prioridad`, `severidad`, `modulo_id`, `responsable_id`, `tester_id`, `creado_por`, `estimacion_horas`, `horas_registradas`, `fecha_limite`, `rama`, `capas`, `historia_origen_id`, `version_reportada`, `bloqueado_por_id`, `creado_en`, `actualizado_en`) VALUES (1421, 1013, 1034, 1043, 'ACTV', 4, 'asdas', 'dasdasd', 'CERRADA', 'ALTA', NULL, 1020, 1001, NULL, 1016, NULL, 0.0, '2026-10-23', 'feature/actv-04-asdas', NULL, NULL, NULL, NULL, '2026-10-25 15:46:39', '2026-10-25 16:05:19');
INSERT INTO `ticket` (`id`, `proyecto_id`, `fase_id`, `historia_id`, `tipo`, `numero`, `titulo`, `descripcion`, `estado`, `prioridad`, `severidad`, `modulo_id`, `responsable_id`, `tester_id`, `creado_por`, `estimacion_horas`, `horas_registradas`, `fecha_limite`, `rama`, `capas`, `historia_origen_id`, `version_reportada`, `bloqueado_por_id`, `creado_en`, `actualizado_en`) VALUES (1422, 1013, 1032, NULL, 'BUGS', 1, 'asdasd', 'asdasd', 'PUBLICADO', 'MEDIA', 'CRITICA', 1020, 1001, NULL, 1016, NULL, 0.0, NULL, 'hotfix/bugs-01-asdasd', NULL, NULL, NULL, NULL, '2026-10-25 16:06:38', '2026-10-25 16:09:35');
INSERT INTO `ticket` (`id`, `proyecto_id`, `fase_id`, `historia_id`, `tipo`, `numero`, `titulo`, `descripcion`, `estado`, `prioridad`, `severidad`, `modulo_id`, `responsable_id`, `tester_id`, `creado_por`, `estimacion_horas`, `horas_registradas`, `fecha_limite`, `rama`, `capas`, `historia_origen_id`, `version_reportada`, `bloqueado_por_id`, `creado_en`, `actualizado_en`) VALUES (1423, 1013, 1035, 1044, 'ACTV', 5, 'asdas', 'dad', 'CERRADA', 'MEDIA', NULL, 1020, 1001, NULL, 1016, NULL, 0.0, NULL, 'feature/actv-05-asdas', NULL, NULL, NULL, NULL, '2026-10-25 16:31:56', '2026-10-25 20:05:45');
INSERT INTO `ticket` (`id`, `proyecto_id`, `fase_id`, `historia_id`, `tipo`, `numero`, `titulo`, `descripcion`, `estado`, `prioridad`, `severidad`, `modulo_id`, `responsable_id`, `tester_id`, `creado_por`, `estimacion_horas`, `horas_registradas`, `fecha_limite`, `rama`, `capas`, `historia_origen_id`, `version_reportada`, `bloqueado_por_id`, `creado_en`, `actualizado_en`) VALUES (1424, 1013, 1035, 1045, 'ACTV', 6, 'asdasd', 'adasd', 'CERRADA', 'MEDIA', NULL, 1020, 1001, NULL, 1016, NULL, 0.0, NULL, 'feature/actv-06-asdasd', NULL, NULL, NULL, NULL, '2026-10-25 16:32:30', '2026-10-25 20:05:45');
INSERT INTO `ticket` (`id`, `proyecto_id`, `fase_id`, `historia_id`, `tipo`, `numero`, `titulo`, `descripcion`, `estado`, `prioridad`, `severidad`, `modulo_id`, `responsable_id`, `tester_id`, `creado_por`, `estimacion_horas`, `horas_registradas`, `fecha_limite`, `rama`, `capas`, `historia_origen_id`, `version_reportada`, `bloqueado_por_id`, `creado_en`, `actualizado_en`) VALUES (1425, 1013, 1035, 1046, 'ACTV', 7, 'asdsadasd', 'asdasd', 'CERRADA', 'MEDIA', NULL, 1020, 1001, NULL, 1016, NULL, 0.0, NULL, 'feature/actv-07-asdsadasd', NULL, NULL, NULL, NULL, '2026-10-25 16:33:23', '2026-10-25 20:05:45');
INSERT INTO `ticket` (`id`, `proyecto_id`, `fase_id`, `historia_id`, `tipo`, `numero`, `titulo`, `descripcion`, `estado`, `prioridad`, `severidad`, `modulo_id`, `responsable_id`, `tester_id`, `creado_por`, `estimacion_horas`, `horas_registradas`, `fecha_limite`, `rama`, `capas`, `historia_origen_id`, `version_reportada`, `bloqueado_por_id`, `creado_en`, `actualizado_en`) VALUES (1426, 1013, 1036, 1048, 'ACTV', 8, 'dasdasdad', 'asdasdsad', 'APROBADA', 'MEDIA', NULL, 1020, 1001, NULL, 1016, NULL, 0.0, NULL, 'feature/actv-08-dasdasdad', NULL, NULL, NULL, NULL, '2026-10-25 19:36:18', '2026-10-25 21:36:24');
INSERT INTO `ticket` (`id`, `proyecto_id`, `fase_id`, `historia_id`, `tipo`, `numero`, `titulo`, `descripcion`, `estado`, `prioridad`, `severidad`, `modulo_id`, `responsable_id`, `tester_id`, `creado_por`, `estimacion_horas`, `horas_registradas`, `fecha_limite`, `rama`, `capas`, `historia_origen_id`, `version_reportada`, `bloqueado_por_id`, `creado_en`, `actualizado_en`) VALUES (1427, 1013, 1035, 1045, 'ACTV', 9, 'asdasd', 'asdasd', 'CERRADA', 'MEDIA', NULL, 1020, 1001, NULL, 1016, NULL, 0.0, NULL, 'feature/actv-09-asdasd', NULL, NULL, NULL, NULL, '2026-10-25 19:54:35', '2026-10-25 20:05:45');
INSERT INTO `ticket` (`id`, `proyecto_id`, `fase_id`, `historia_id`, `tipo`, `numero`, `titulo`, `descripcion`, `estado`, `prioridad`, `severidad`, `modulo_id`, `responsable_id`, `tester_id`, `creado_por`, `estimacion_horas`, `horas_registradas`, `fecha_limite`, `rama`, `capas`, `historia_origen_id`, `version_reportada`, `bloqueado_por_id`, `creado_en`, `actualizado_en`) VALUES (1428, 1013, 1036, 1048, 'ACTV', 10, 'asdasd', 'asdasd', 'APROBADA', 'MEDIA', NULL, 1020, 1001, NULL, 1016, NULL, 0.0, NULL, 'feature/actv-10-asdasd', NULL, NULL, NULL, NULL, '2026-10-25 19:55:13', '2026-10-25 21:36:28');
INSERT INTO `ticket` (`id`, `proyecto_id`, `fase_id`, `historia_id`, `tipo`, `numero`, `titulo`, `descripcion`, `estado`, `prioridad`, `severidad`, `modulo_id`, `responsable_id`, `tester_id`, `creado_por`, `estimacion_horas`, `horas_registradas`, `fecha_limite`, `rama`, `capas`, `historia_origen_id`, `version_reportada`, `bloqueado_por_id`, `creado_en`, `actualizado_en`) VALUES (1429, 1013, 1035, 1047, 'ACTV', 11, 'asda', 'sdasd', 'CERRADA', 'MEDIA', NULL, 1020, 1001, NULL, 1016, NULL, 0.0, NULL, 'feature/actv-11-asda', NULL, NULL, NULL, NULL, '2026-10-25 19:55:29', '2026-10-25 20:05:45');
INSERT INTO `ticket` (`id`, `proyecto_id`, `fase_id`, `historia_id`, `tipo`, `numero`, `titulo`, `descripcion`, `estado`, `prioridad`, `severidad`, `modulo_id`, `responsable_id`, `tester_id`, `creado_por`, `estimacion_horas`, `horas_registradas`, `fecha_limite`, `rama`, `capas`, `historia_origen_id`, `version_reportada`, `bloqueado_por_id`, `creado_en`, `actualizado_en`) VALUES (1430, 1014, 1038, 1049, 'ACTV', 1, 'asd', 'asd', 'INTEGRADA', 'MEDIA', NULL, 1021, 1001, NULL, 1016, NULL, 0.0, '2026-11-04', 'feature/actv-01-asd', NULL, NULL, NULL, NULL, '2026-10-25 21:47:37', '2026-10-25 22:17:17');
INSERT INTO `ticket` (`id`, `proyecto_id`, `fase_id`, `historia_id`, `tipo`, `numero`, `titulo`, `descripcion`, `estado`, `prioridad`, `severidad`, `modulo_id`, `responsable_id`, `tester_id`, `creado_por`, `estimacion_horas`, `horas_registradas`, `fecha_limite`, `rama`, `capas`, `historia_origen_id`, `version_reportada`, `bloqueado_por_id`, `creado_en`, `actualizado_en`) VALUES (1431, 1014, 1038, 1049, 'ACTV', 2, 'asdas', 'asdasd', 'INTEGRADA', 'MEDIA', NULL, 1021, 1014, NULL, 1016, 3.0, 0.0, NULL, 'feature/actv-02-asdas', NULL, NULL, NULL, NULL, '2026-10-25 21:56:03', '2026-10-25 22:17:17');
INSERT INTO `ticket` (`id`, `proyecto_id`, `fase_id`, `historia_id`, `tipo`, `numero`, `titulo`, `descripcion`, `estado`, `prioridad`, `severidad`, `modulo_id`, `responsable_id`, `tester_id`, `creado_por`, `estimacion_horas`, `horas_registradas`, `fecha_limite`, `rama`, `capas`, `historia_origen_id`, `version_reportada`, `bloqueado_por_id`, `creado_en`, `actualizado_en`) VALUES (1432, 1014, 1038, 1049, 'ACTV', 3, 'asdas', 'dasd', 'INTEGRADA', 'MEDIA', NULL, 1021, 1001, NULL, 1016, NULL, 0.0, NULL, 'feature/actv-03-asdas', NULL, NULL, NULL, NULL, '2026-10-25 22:10:03', '2026-10-25 22:17:17');
INSERT INTO `ticket` (`id`, `proyecto_id`, `fase_id`, `historia_id`, `tipo`, `numero`, `titulo`, `descripcion`, `estado`, `prioridad`, `severidad`, `modulo_id`, `responsable_id`, `tester_id`, `creado_por`, `estimacion_horas`, `horas_registradas`, `fecha_limite`, `rama`, `capas`, `historia_origen_id`, `version_reportada`, `bloqueado_por_id`, `creado_en`, `actualizado_en`) VALUES (1433, 1015, 1040, 1051, 'ACTV', 1, 'asdasd', 'sadasd', 'CERRADA', 'MEDIA', NULL, 1022, 1001, NULL, 1016, NULL, 0.0, NULL, 'feature/actv-01-asdasd', NULL, NULL, NULL, NULL, '2026-10-25 22:24:57', '2026-10-25 22:35:43');
INSERT INTO `ticket` (`id`, `proyecto_id`, `fase_id`, `historia_id`, `tipo`, `numero`, `titulo`, `descripcion`, `estado`, `prioridad`, `severidad`, `modulo_id`, `responsable_id`, `tester_id`, `creado_por`, `estimacion_horas`, `horas_registradas`, `fecha_limite`, `rama`, `capas`, `historia_origen_id`, `version_reportada`, `bloqueado_por_id`, `creado_en`, `actualizado_en`) VALUES (1434, 1015, 1040, 1050, 'ACTV', 2, 'asdasd', 'asdasd', 'CERRADA', 'MEDIA', NULL, 1022, 1001, NULL, 1016, NULL, 0.0, NULL, 'feature/actv-02-asdasd', NULL, NULL, NULL, NULL, '2026-10-25 22:25:16', '2026-10-25 22:35:43');
INSERT INTO `ticket` (`id`, `proyecto_id`, `fase_id`, `historia_id`, `tipo`, `numero`, `titulo`, `descripcion`, `estado`, `prioridad`, `severidad`, `modulo_id`, `responsable_id`, `tester_id`, `creado_por`, `estimacion_horas`, `horas_registradas`, `fecha_limite`, `rama`, `capas`, `historia_origen_id`, `version_reportada`, `bloqueado_por_id`, `creado_en`, `actualizado_en`) VALUES (1435, 1015, 1040, 1052, 'ACTV', 3, 'asdasd', 'asdasd', 'CERRADA', 'MEDIA', NULL, 1022, 1014, NULL, 1016, NULL, 0.0, NULL, 'feature/actv-03-asdasd', NULL, NULL, NULL, NULL, '2026-10-25 22:25:41', '2026-10-25 22:35:43');
INSERT INTO `ticket` (`id`, `proyecto_id`, `fase_id`, `historia_id`, `tipo`, `numero`, `titulo`, `descripcion`, `estado`, `prioridad`, `severidad`, `modulo_id`, `responsable_id`, `tester_id`, `creado_por`, `estimacion_horas`, `horas_registradas`, `fecha_limite`, `rama`, `capas`, `historia_origen_id`, `version_reportada`, `bloqueado_por_id`, `creado_en`, `actualizado_en`) VALUES (1436, 1015, 1040, 1050, 'ACTV', 4, 'asdas', 'dasd', 'CERRADA', 'MEDIA', NULL, 1022, 1001, NULL, 1016, NULL, 0.0, NULL, 'feature/actv-04-asdas', NULL, NULL, NULL, NULL, '2026-10-25 22:27:08', '2026-10-25 22:35:43');
INSERT INTO `ticket` (`id`, `proyecto_id`, `fase_id`, `historia_id`, `tipo`, `numero`, `titulo`, `descripcion`, `estado`, `prioridad`, `severidad`, `modulo_id`, `responsable_id`, `tester_id`, `creado_por`, `estimacion_horas`, `horas_registradas`, `fecha_limite`, `rama`, `capas`, `historia_origen_id`, `version_reportada`, `bloqueado_por_id`, `creado_en`, `actualizado_en`) VALUES (1437, 1015, 1040, 1051, 'ACTV', 5, 'asd', 'asd', 'CERRADA', 'MEDIA', NULL, 1022, 1001, NULL, 1016, NULL, 0.0, NULL, 'feature/actv-05-asd', NULL, NULL, NULL, NULL, '2026-10-25 22:29:14', '2026-10-25 22:35:43');
INSERT INTO `ticket` (`id`, `proyecto_id`, `fase_id`, `historia_id`, `tipo`, `numero`, `titulo`, `descripcion`, `estado`, `prioridad`, `severidad`, `modulo_id`, `responsable_id`, `tester_id`, `creado_por`, `estimacion_horas`, `horas_registradas`, `fecha_limite`, `rama`, `capas`, `historia_origen_id`, `version_reportada`, `bloqueado_por_id`, `creado_en`, `actualizado_en`) VALUES (1438, 1015, 1040, 1052, 'ACTV', 6, 'asdasd', 'dasd', 'CERRADA', 'MEDIA', NULL, 1022, 1001, NULL, 1016, NULL, 0.0, NULL, 'feature/actv-06-asdasd', NULL, NULL, NULL, NULL, '2026-10-25 22:30:56', '2026-10-25 22:35:43');
INSERT INTO `ticket` (`id`, `proyecto_id`, `fase_id`, `historia_id`, `tipo`, `numero`, `titulo`, `descripcion`, `estado`, `prioridad`, `severidad`, `modulo_id`, `responsable_id`, `tester_id`, `creado_por`, `estimacion_horas`, `horas_registradas`, `fecha_limite`, `rama`, `capas`, `historia_origen_id`, `version_reportada`, `bloqueado_por_id`, `creado_en`, `actualizado_en`) VALUES (1439, 1015, 1041, 1053, 'ACTV', 7, 'sdfasdasd', 'asdad', 'INTEGRADA', 'MEDIA', NULL, 1022, 1001, NULL, 1016, NULL, 0.0, NULL, 'feature/actv-07-sdfasdasd', NULL, NULL, NULL, NULL, '2026-10-26 22:15:20', '2026-10-26 22:18:37');
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
INSERT INTO `ticket_criterio` (`ticket_id`, `criterio_id`) VALUES (1419, 1042);
INSERT INTO `ticket_criterio` (`ticket_id`, `criterio_id`) VALUES (1418, 1043);
INSERT INTO `ticket_criterio` (`ticket_id`, `criterio_id`) VALUES (1420, 1044);
INSERT INTO `ticket_criterio` (`ticket_id`, `criterio_id`) VALUES (1423, 1045);
INSERT INTO `ticket_criterio` (`ticket_id`, `criterio_id`) VALUES (1424, 1046);
INSERT INTO `ticket_criterio` (`ticket_id`, `criterio_id`) VALUES (1425, 1047);
INSERT INTO `ticket_criterio` (`ticket_id`, `criterio_id`) VALUES (1429, 1048);
INSERT INTO `ticket_criterio` (`ticket_id`, `criterio_id`) VALUES (1426, 1050);
INSERT INTO `ticket_criterio` (`ticket_id`, `criterio_id`) VALUES (1430, 1051);
INSERT INTO `ticket_criterio` (`ticket_id`, `criterio_id`) VALUES (1431, 1051);
INSERT INTO `ticket_criterio` (`ticket_id`, `criterio_id`) VALUES (1433, 1052);
INSERT INTO `ticket_criterio` (`ticket_id`, `criterio_id`) VALUES (1434, 1053);
INSERT INTO `ticket_criterio` (`ticket_id`, `criterio_id`) VALUES (1436, 1053);
INSERT INTO `ticket_criterio` (`ticket_id`, `criterio_id`) VALUES (1434, 1054);
INSERT INTO `ticket_criterio` (`ticket_id`, `criterio_id`) VALUES (1436, 1054);
INSERT INTO `ticket_criterio` (`ticket_id`, `criterio_id`) VALUES (1435, 1055);
INSERT INTO `ticket_criterio` (`ticket_id`, `criterio_id`) VALUES (1439, 1056);
INSERT INTO `ticket_criterio` (`ticket_id`, `criterio_id`) VALUES (1439, 1057);
INSERT INTO `ticket_criterio` (`ticket_id`, `criterio_id`) VALUES (1439, 1058);
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
INSERT INTO `ticket_subtarea` (`id`, `ticket_id`, `descripcion`, `hecha`, `orden`) VALUES (1012, 1420, 'asdad', 1, 1);
INSERT INTO `ticket_subtarea` (`id`, `ticket_id`, `descripcion`, `hecha`, `orden`) VALUES (1013, 1420, 'asdad', 1, 2);
INSERT INTO `ticket_subtarea` (`id`, `ticket_id`, `descripcion`, `hecha`, `orden`) VALUES (1014, 1420, 'asdasdasd', 1, 3);
INSERT INTO `ticket_subtarea` (`id`, `ticket_id`, `descripcion`, `hecha`, `orden`) VALUES (1015, 1426, 'asdasd', 1, 1);
INSERT INTO `ticket_subtarea` (`id`, `ticket_id`, `descripcion`, `hecha`, `orden`) VALUES (1016, 1426, 'asdasdasd', 1, 2);
INSERT INTO `ticket_subtarea` (`id`, `ticket_id`, `descripcion`, `hecha`, `orden`) VALUES (1017, 1426, 'asdasd', 1, 3);
INSERT INTO `ticket_subtarea` (`id`, `ticket_id`, `descripcion`, `hecha`, `orden`) VALUES (1019, 1426, 'sadasd', 1, 4);
INSERT INTO `ticket_subtarea` (`id`, `ticket_id`, `descripcion`, `hecha`, `orden`) VALUES (1020, 1426, 'asdsad', 1, 5);
INSERT INTO `ticket_subtarea` (`id`, `ticket_id`, `descripcion`, `hecha`, `orden`) VALUES (1021, 1432, 'sadsadasd', 1, 1);
INSERT INTO `ticket_subtarea` (`id`, `ticket_id`, `descripcion`, `hecha`, `orden`) VALUES (1022, 1432, 'asdasd', 1, 2);
INSERT INTO `ticket_subtarea` (`id`, `ticket_id`, `descripcion`, `hecha`, `orden`) VALUES (1023, 1432, 'asdas', 1, 3);
INSERT INTO `ticket_subtarea` (`id`, `ticket_id`, `descripcion`, `hecha`, `orden`) VALUES (1024, 1432, 'asdas', 1, 4);
INSERT INTO `ticket_subtarea` (`id`, `ticket_id`, `descripcion`, `hecha`, `orden`) VALUES (1025, 1432, 'asdasd', 1, 5);
INSERT INTO `ticket_subtarea` (`id`, `ticket_id`, `descripcion`, `hecha`, `orden`) VALUES (1026, 1439, 'asdasd', 0, 1);
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
INSERT INTO `usuario` (`id`, `nombre`, `apellidos`, `correo`, `password_hash`, `tipo`, `cliente_id`, `activo`, `cambiar_password`, `ultimo_acceso`, `creado_en`, `actualizado_en`) VALUES (1001, 'Jorge', 'Sebastián', 'jorge@equipo.mx', '$2y$10$pnGhSVbhgOfoFJzOzILvX.Xwia6j5XItZeZwcUMUcp4FbYgVvcqTW', 'EQUIPO', NULL, 1, 0, '2026-10-26 22:16:22', '2026-10-18 22:10:25', '2026-10-26 22:16:21');
INSERT INTO `usuario` (`id`, `nombre`, `apellidos`, `correo`, `password_hash`, `tipo`, `cliente_id`, `activo`, `cambiar_password`, `ultimo_acceso`, `creado_en`, `actualizado_en`) VALUES (1013, 'Alma', NULL, 'alma@equipo.mx', '$2y$10$pnGhSVbhgOfoFJzOzILvX.Xwia6j5XItZeZwcUMUcp4FbYgVvcqTW', 'EQUIPO', NULL, 1, 0, '2026-10-25 22:34:24', '2026-10-24 19:50:08', '2026-10-25 22:34:24');
INSERT INTO `usuario` (`id`, `nombre`, `apellidos`, `correo`, `password_hash`, `tipo`, `cliente_id`, `activo`, `cambiar_password`, `ultimo_acceso`, `creado_en`, `actualizado_en`) VALUES (1014, 'Marco', NULL, 'marco@equipo.mx', '$2y$10$pnGhSVbhgOfoFJzOzILvX.Xwia6j5XItZeZwcUMUcp4FbYgVvcqTW', 'EQUIPO', NULL, 1, 0, '2026-10-26 22:17:40', '2026-10-24 19:50:08', '2026-10-26 22:17:39');
INSERT INTO `usuario` (`id`, `nombre`, `apellidos`, `correo`, `password_hash`, `tipo`, `cliente_id`, `activo`, `cambiar_password`, `ultimo_acceso`, `creado_en`, `actualizado_en`) VALUES (1015, 'Administrador', NULL, 'admin@equipo.mx', '$2y$10$pnGhSVbhgOfoFJzOzILvX.Xwia6j5XItZeZwcUMUcp4FbYgVvcqTW', 'EQUIPO', NULL, 1, 0, '2026-10-26 22:08:21', '2026-10-25 13:12:54', '2026-10-26 22:08:21');
INSERT INTO `usuario` (`id`, `nombre`, `apellidos`, `correo`, `password_hash`, `tipo`, `cliente_id`, `activo`, `cambiar_password`, `ultimo_acceso`, `creado_en`, `actualizado_en`) VALUES (1016, 'Lider', NULL, 'lider@equipo.mx', '$2y$10$pnGhSVbhgOfoFJzOzILvX.Xwia6j5XItZeZwcUMUcp4FbYgVvcqTW', 'EQUIPO', NULL, 1, 0, '2026-10-26 22:21:12', '2026-10-25 13:28:05', '2026-10-26 22:21:11');
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
INSERT INTO `version_release` (`id`, `proyecto_id`, `fase_id`, `version`, `tipo`, `estado`, `resumen`, `notas`, `enviar_notas`, `publicado_por`, `publicado_en`) VALUES (1012, 1013, 1033, 'v1.0.0', 'FASE', 'PUBLICADA', 'asd', 'Nuevo\n- asdas\n- sdad', 0, 1016, '2026-10-25 15:22:30');
INSERT INTO `version_release` (`id`, `proyecto_id`, `fase_id`, `version`, `tipo`, `estado`, `resumen`, `notas`, `enviar_notas`, `publicado_por`, `publicado_en`) VALUES (1014, 1013, 1034, 'v1.1.0', 'FASE', 'PUBLICADA', 'asda', 'Nuevo\n- asd\n- asdas', 0, 1016, '2026-10-25 16:05:20');
INSERT INTO `version_release` (`id`, `proyecto_id`, `fase_id`, `version`, `tipo`, `estado`, `resumen`, `notas`, `enviar_notas`, `publicado_por`, `publicado_en`) VALUES (1016, 1013, NULL, 'v1.1.1', 'PARCHE', 'PUBLICADA', 'Parche: BUGS-01', NULL, 1, 1016, '2026-10-25 16:09:36');
INSERT INTO `version_release` (`id`, `proyecto_id`, `fase_id`, `version`, `tipo`, `estado`, `resumen`, `notas`, `enviar_notas`, `publicado_por`, `publicado_en`) VALUES (1017, 1013, 1035, 'v1.2.0', 'FASE', 'PUBLICADA', 'asdasd', 'Nuevo\n- asdas\n- asdasd\n- asdsadasd\n- asdasd\n- asda', 0, 1016, '2026-10-25 20:05:46');
INSERT INTO `version_release` (`id`, `proyecto_id`, `fase_id`, `version`, `tipo`, `estado`, `resumen`, `notas`, `enviar_notas`, `publicado_por`, `publicado_en`) VALUES (1019, 1015, 1040, 'v1.0.0', 'FASE', 'PUBLICADA', 'asdas', 'Nuevo\n- asdasd\n- asdasd\n- asdasd\n- asdas\n- asd\n- asdasd', 0, 1016, '2026-10-25 22:35:44');
COMMIT;

SET FOREIGN_KEY_CHECKS = 1;
