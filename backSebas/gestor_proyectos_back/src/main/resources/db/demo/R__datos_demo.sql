-- =====================================================================
-- Datos de ejemplo (los del PDF). Solo se cargan con el perfil "dev".
-- Es una migración repetible: usa ids fijos (>= 1000) e INSERT IGNORE,
-- así se puede volver a correr sin duplicar nada.
-- Contraseña de todos los usuarios: Cambiar123
-- =====================================================================

SET @pwd = '$2y$10$pnGhSVbhgOfoFJzOzILvX.Xwia6j5XItZeZwcUMUcp4FbYgVvcqTW';

-- ---------- Clientes ----------
INSERT IGNORE INTO cliente (id, razon_social, nombre_corto, rfc, ciudad, contacto_nombre, contacto_correo, contacto_telefono) VALUES
    (1001, 'Grupo Ferretero León S.A. de C.V.', 'Grupo Ferretero León', 'GFL120315AB1', 'León, Gto.',    'Mariana Torres', 'mariana@gfl.mx',           '477 123 4567'),
    (1002, 'Distribuidora Bajío S.A. de C.V.',  'Distribuidora Bajío',  NULL,           'Irapuato, Gto.', 'Carlos Ruiz',    'carlos@dbajio.mx',         '462 555 0198'),
    (1003, 'Calzado Moderno S.A. de C.V.',      'Calzado Moderno',      NULL,           'León, Gto.',    'Laura Gómez',    'laura@calzadomoderno.mx',  '477 789 0011'),
    (1004, 'Interno',                           'Interno',              NULL,           'Equipo propio', 'Jorge Sebastián', 'jorge@equipo.mx',         NULL);

-- Giro (V9). UPDATE porque INSERT IGNORE no toca a los clientes que ya existían.
UPDATE cliente SET giro = 'COMERCIO'    WHERE id IN (1001, 1002) AND giro IS NULL;
UPDATE cliente SET giro = 'MANUFACTURA' WHERE id = 1003 AND giro IS NULL;
UPDATE cliente SET giro = 'TECNOLOGIA'  WHERE id = 1004 AND giro IS NULL;

-- ---------- Usuarios del equipo y del cliente ----------
INSERT IGNORE INTO usuario (id, nombre, apellidos, correo, password_hash, tipo, cliente_id, activo, cambiar_password) VALUES
    (1001, 'Jorge', 'Sebastián',      'jorge@equipo.mx',  @pwd, 'EQUIPO',  NULL, TRUE,  FALSE),
    (1002, 'Ana',   'López Hernández', 'ana@equipo.mx',    @pwd, 'EQUIPO',  NULL, TRUE,  FALSE),
    (1003, 'Pedro', 'Ramírez',         'pedro@equipo.mx',  @pwd, 'EQUIPO',  NULL, TRUE,  FALSE),
    (1004, 'Luis',  'Méndez',          'luis@equipo.mx',   @pwd, 'EQUIPO',  NULL, FALSE, FALSE),
    (1010, 'Mariana', 'Torres',        'mariana@gfl.mx',   @pwd, 'CLIENTE', 1001, TRUE,  FALSE);

INSERT IGNORE INTO usuario_rol (usuario_id, rol_id)
SELECT 1001, id FROM rol WHERE codigo IN ('ADMIN', 'LIDER')
UNION ALL SELECT 1002, id FROM rol WHERE codigo IN ('ANALISTA', 'TESTER')
UNION ALL SELECT 1003, id FROM rol WHERE codigo = 'DESARROLLADOR'
UNION ALL SELECT 1004, id FROM rol WHERE codigo = 'DESARROLLADOR';

-- ---------- Proyectos ----------
INSERT IGNORE INTO proyecto (id, codigo, nombre, descripcion, cliente_id, prioridad, estado, fecha_inicio, fecha_entrega, lider_id, permitir_autovalidacion) VALUES
    (1001, 'PC', 'Portal de Clientes', 'Portal donde los clientes consultan pedidos, facturas y abren solicitudes de soporte.',
           1001, 'ALTA',  'EN_CURSO',   '2026-02-03', '2026-10-30', 1001, TRUE),
    (1002, 'CV', 'CRM de Ventas',      'Seguimiento de prospectos y cotizaciones del equipo de ventas.',
           1001, 'MEDIA', 'PLANEACION', '2026-09-01', '2026-12-18', 1001, FALSE);

-- Proyectos de los otros clientes (Clientes, PDF pág. 9). Aún sin fases ni tickets.
INSERT IGNORE INTO proyecto (id, codigo, nombre, descripcion, cliente_id, prioridad, estado, fecha_inicio, fecha_entrega, lider_id) VALUES
    (1003, 'AI', 'App de Inventario',   'App para contar inventario en almacén con el celular.',
           1002, 'MEDIA', 'EN_CURSO',   '2026-06-01', '2026-11-27', 1001),
    (1004, 'SC', 'Sitio Corporativo',   'Sitio web de la marca con catálogo de productos.',
           1003, 'BAJA',  'PLANEACION', '2026-10-01', '2027-01-29', 1001),
    (1005, 'GP', 'Gestor de Proyectos', 'Esta misma herramienta: fases, historias y tickets del equipo.',
           1004, 'ALTA',  'EN_CURSO',   '2026-08-03', NULL,         1001);

INSERT IGNORE INTO proyecto_miembro (proyecto_id, usuario_id) VALUES
    (1001, 1001), (1001, 1002), (1001, 1003),
    (1002, 1001), (1002, 1002),
    (1003, 1001), (1003, 1003),
    (1004, 1001), (1004, 1002),
    (1005, 1001), (1005, 1003);

-- Permisos extra de Ana (Usuarios y roles): puede crear tickets en todo, pero no aprobar QA en el CRM.
INSERT IGNORE INTO usuario_permiso (id, usuario_id, permiso_id, proyecto_id, otorgado)
SELECT 1001, 1002, id, NULL, TRUE  FROM permiso WHERE codigo = 'ticket.crear'
UNION ALL SELECT 1002, 1002, id, 1002, FALSE FROM permiso WHERE codigo = 'ticket.aprobar_qa'
-- Pestaña Equipo (PDF pág. 34): un permiso extra que solo aplica en el Portal de Clientes
UNION ALL SELECT 1003, 1002, id, 1001, TRUE  FROM permiso WHERE codigo = 'ticket.cancelar';

INSERT IGNORE INTO modulo (id, proyecto_id, tipo, nombre, tecnologia, repositorio) VALUES
    (1001, 1001, 'API',   'portal-api', 'Java 21 · Spring Boot', 'github.com/equipo/portal-api'),
    (1002, 1001, 'WEB',   'portal-web', 'Angular',               'github.com/equipo/portal-web'),
    (1003, 1001, 'MOVIL', 'portal-app', 'Flutter',               'github.com/equipo/portal-app'),
    (1004, 1002, 'WEB',   'crm-web',    'Angular',               'github.com/equipo/crm-web'),
    (1005, 1002, 'API',   'crm-api',    'Java 21 · Spring Boot', 'github.com/equipo/crm-api'),
    (1006, 1003, 'API',   'inventario-api', 'Java 21 · Spring Boot', 'github.com/equipo/inventario-api'),
    (1007, 1003, 'MOVIL', 'inventario-app', 'Flutter',           'github.com/equipo/inventario-app'),
    (1008, 1004, 'WEB',   'corporativo-web', 'Angular',          'github.com/equipo/corporativo-web'),
    (1009, 1005, 'API',   'gestor-api', 'Java 17 · Spring Boot', 'github.com/equipo/gestor-api'),
    (1010, 1005, 'WEB',   'gestor-web', 'Angular',               'github.com/equipo/gestor-web');

-- ---------- Fases ----------
INSERT IGNORE INTO fase (id, proyecto_id, numero, nombre, objetivo, estado, prioridad, fecha_inicio, fecha_fin, responsable_id, version_release, es_mantenimiento) VALUES
    (1001, 1001, 1, 'Análisis',         'Levantar requerimientos y diseñar la arquitectura del portal.',
           'PUBLICADA', 'ALTA', '2026-02-03', '2026-02-21', 1001, NULL,     FALSE),
    (1002, 1001, 2, 'Desarrollo',       'Inicio de sesión, gestión de usuarios y catálogo de productos.',
           'PUBLICADA', 'ALTA', '2026-02-24', '2026-04-25', 1001, 'v1.1.0', FALSE),
    (1003, 1001, 3, 'Pruebas y salida', 'Probar con usuarios reales y salir a producción.',
           'PUBLICADA', 'ALTA', '2026-04-28', '2026-05-29', 1001, 'v1.2.0', FALSE),
    (1004, 1001, 4, 'Portal móvil',     'Que tus clientes consulten pedidos y facturas desde su celular y reciban avisos cuando cambie el estado de un pedido.',
           'EN_CURSO',  'ALTA', '2026-06-01', '2026-07-31', 1001, 'v1.3.0', FALSE),
    (1005, 1001, 5, 'Pagos en línea',   'Pagar pedidos con tarjeta desde el portal y la app.',
           'PLANEADA',  'MEDIA', '2026-09-01', '2026-10-30', 1001, 'v1.4.0', FALSE),
    (1006, 1001, 0, 'Mantenimiento',    'Fase permanente para corregir bugs de producción.',
           'EN_CURSO',  'ALTA', NULL, NULL, 1001, NULL, TRUE),
    (1010, 1002, 1, 'Análisis',         'Entender el proceso de ventas actual y definir el alcance.',
           'PLANEADA',  'MEDIA', '2026-09-01', '2026-09-30', 1001, NULL, FALSE);

INSERT IGNORE INTO fase_modulo (fase_id, modulo_id) VALUES
    (1001, 1001), (1002, 1001), (1002, 1002), (1003, 1002), (1004, 1001), (1004, 1003), (1005, 1001), (1005, 1002), (1010, 1004);

INSERT IGNORE INTO fase_criterio_cierre (id, fase_id, descripcion, tipo, codigo, cumplido) VALUES
    (1001, 1004, 'Todas las historias aceptadas por el Analista',  'AUTOMATICO', 'HISTORIAS_ACEPTADAS', FALSE),
    (1002, 1004, 'Sin bugs de prioridad alta abiertos',            'AUTOMATICO', 'SIN_BUGS_ALTA',       TRUE),
    (1003, 1004, 'Módulos involucrados desplegados en staging',    'AUTOMATICO', 'MODULOS_EN_STAGING',  FALSE),
    (1004, 1004, 'Visto bueno del cliente en la demo',             'MANUAL',     NULL,                  FALSE);

-- ---------- Historias ----------
INSERT IGNORE INTO historia (id, proyecto_id, fase_id, numero, titulo, como, quiero, para, estado, prioridad, puntos, analista_id, creado_por) VALUES
    (1012, 1001, 1002, 12, 'Login con JWT',                       'usuario del portal',          'iniciar sesión con mi correo y contraseña', 'entrar de forma segura',
           'CERRADA', 'ALTA', 5, 1002, 1002),
    (1014, 1001, 1003, 14, 'Gestión de clientes',                 'administrador',               'dar de alta y editar clientes',             'tener su información al día',
           'CERRADA', 'ALTA', 3, 1002, 1002),
    (1016, 1001, 1004, 16, 'Consultar mis pedidos desde la app',  'cliente con contrato activo', 'ver mis pedidos y en qué estado está cada uno', 'saber cuándo me llega sin tener que llamar a soporte',
           'EN_VALIDACION', 'ALTA', 5, 1002, 1002),
    (1017, 1001, 1004, 17, 'Descargar facturas en PDF',           'cliente',                     'descargar mis facturas desde el celular',   'tenerlas a la mano sin pedirlas por correo',
           'EN_DESARROLLO', 'ALTA', 8, 1002, 1002),
    (1018, 1001, 1004, 18, 'Avisos de cambio de estado',          'cliente',                     'recibir un aviso cuando mi pedido cambie de estado', 'no tener que estar revisando la app',
           'BORRADOR', 'MEDIA', NULL, 1002, 1002);

INSERT IGNORE INTO historia_criterio (id, historia_id, descripcion, verificado, orden) VALUES
    (1001, 1016, 'Veo mis pedidos de los últimos 12 meses, del más reciente al más antiguo', TRUE,  1),
    (1002, 1016, 'Cada pedido muestra folio, fecha, total y estado',                         TRUE,  2),
    (1003, 1016, 'Puedo filtrar por estado: en proceso, enviado, entregado, cancelado',      TRUE,  3),
    (1004, 1016, 'Si no tengo pedidos veo un mensaje que lo explica',                        FALSE, 4);

INSERT IGNORE INTO historia_modulo (historia_id, modulo_id) VALUES
    (1016, 1001), (1016, 1003), (1017, 1001), (1017, 1003), (1018, 1001), (1018, 1003);

-- ---------- Tickets ----------
INSERT IGNORE INTO ticket (id, proyecto_id, fase_id, historia_id, tipo, numero, titulo, estado, prioridad, modulo_id, responsable_id, tester_id, creado_por, estimacion_horas) VALUES
    -- Fase 2 (publicada)
    (1028, 1001, 1002, 1012, 'ACTV', 28, 'Estructura base del proyecto',            'CERRADA', 'ALTA', 1001, 1003, 1002, 1001, 4),
    (1029, 1001, 1002, 1012, 'ACTV', 29, 'Modelo de usuarios y sesiones',           'CERRADA', 'ALTA', 1001, 1003, 1002, 1001, 6),
    (1030, 1001, 1002, 1012, 'ACTV', 30, 'Pantalla de login',                       'CERRADA', 'ALTA', 1002, 1003, 1002, 1001, 6),
    (1104, 1001, 1002, 1012, 'TECN',  4, 'Configurar base de datos',                'CERRADA', 'MEDIA', 1001, 1001, NULL, 1001, 3),
    -- Fase 3 (publicada)
    (1033, 1001, 1003, 1014, 'ACTV', 33, 'CRUD de clientes',                        'CERRADA', 'ALTA', 1001, 1003, 1002, 1001, 8),
    (1034, 1001, 1003, 1014, 'ACTV', 34, 'Pantalla de clientes',                    'CERRADA', 'ALTA', 1002, 1003, 1002, 1001, 6),
    -- Fase 4 (en curso) · HU-0016
    (1107, 1001, 1004, 1016, 'TECN',  7, 'Modelo de datos de pedidos',              'HECHA',       'ALTA', 1001, 1003, NULL, 1001, 3),
    (1041, 1001, 1004, 1016, 'ACTV', 41, 'Servicio de consulta de pedidos en Spring Boot', 'APROBADA', 'ALTA', 1001, 1003, 1002, 1001, 6),
    (1042, 1001, 1004, 1016, 'ACTV', 42, 'Pantalla Mis pedidos en Flutter',         'APROBADA',    'ALTA', 1003, 1003, 1002, 1001, 8),
    -- Fase 4 · HU-0017
    (1043, 1001, 1004, 1017, 'ACTV', 43, 'Servicio de facturas del cliente',        'APROBADA',    'ALTA', 1001, 1003, 1002, 1001, 5),
    (1044, 1001, 1004, 1017, 'ACTV', 44, 'Generar factura en PDF',                  'EN_QA',       'ALTA', 1001, 1003, 1002, 1001, 6),
    (1045, 1001, 1004, 1017, 'ACTV', 45, 'Pantalla Mis facturas en Flutter',        'EN_PROGRESO', 'ALTA', 1003, 1003, 1002, 1001, 8),
    (1046, 1001, 1004, 1017, 'ACTV', 46, 'Descargar y abrir el PDF en el celular',  'POR_HACER',   'MEDIA', 1003, 1003, 1002, 1001, 4);

-- Fase 4 · tickets sueltos y de otros tipos (para el Dashboard)
INSERT IGNORE INTO ticket (id, proyecto_id, fase_id, historia_id, tipo, numero, titulo, estado, prioridad, modulo_id, responsable_id, tester_id, creado_por, creado_en) VALUES
    (1131, 1001, 1004, 1017, 'BUGS', 31, 'Error al refrescar el token JWT',    'REPORTADO',   'ALTA',  1001, 1003, 1002, 1002, NOW() - INTERVAL 2 HOUR),
    (1208, 1001, 1004, NULL, 'MEJR',  8, 'Exportar reporte de tickets a PDF', 'PROPUESTA',   'MEDIA', 1001, 1003, 1002, 1002, NOW() - INTERVAL 1 DAY),
    (1108, 1001, 1004, NULL, 'TECN',  8, 'Configurar despliegue en VPS',      'EN_REVISION', 'MEDIA', 1001, 1003, NULL, 1001, NOW() - INTERVAL 3 DAY),
    (1302, 1001, 1004, NULL, 'INVS',  2, 'Evaluar librería de gráficas',      'REVISADA',    'BAJA',  1002, 1003, NULL, 1001, NOW() - INTERVAL 6 DAY),
    (1404, 1001, 1004, NULL, 'DOCT',  4, 'Manual de flujos por rol',          'PUBLICADA',   'BAJA',  NULL, 1002, NULL, 1001, NOW() - INTERVAL 6 DAY);

-- Mantenimiento: bugs de producción (Hotfix, PDF pág. 23). Crítica y Alta salen en parche; Media sigue el flujo normal.
INSERT IGNORE INTO ticket (id, proyecto_id, fase_id, historia_id, tipo, numero, titulo, descripcion, estado, prioridad, severidad, modulo_id, responsable_id, tester_id, creado_por, rama, creado_en) VALUES
    (1150, 1001, 1006, NULL, 'BUGS', 50, 'Error 500 al descargar facturas en PDF',
           'Desde v1.2.0, GET /api/clientes/{id}/facturas/{folio}/pdf responde 500 cuando la factura tiene descuentos.',
           'EN_CORRECCION', 'ALTA', 'CRITICA', 1001, 1003, 1002, 1002, 'hotfix/bugs-50-error-500-al-descargar-facturas-en-pdf',
           NOW() - INTERVAL 5 HOUR),
    (1151, 1001, 1006, NULL, 'BUGS', 51, 'Texto cortado en el pie de la factura',
           'En facturas con más de 20 partidas el pie se corta en la segunda hoja.',
           'REPORTADO', 'MEDIA', NULL, 1001, NULL, NULL, 1002, NULL, NOW() - INTERVAL 1 DAY),
    (1152, 1001, 1006, NULL, 'BUGS', 52, 'La app se cierra al abrir un pedido cancelado',
           'En Android, abrir un pedido cancelado desde notificaciones cierra la app.',
           'APROBADO', 'ALTA', 'ALTA', 1003, 1003, 1002, 1001, 'hotfix/bugs-52-la-app-se-cierra-al-abrir-un-pedido',
           NOW() - INTERVAL 2 DAY);

-- Fechas de creación realistas: lo de fases pasadas hace meses, lo de la Fase 4 hace semanas
UPDATE ticket SET creado_en = NOW() - INTERVAL 120 DAY WHERE fase_id IN (1001, 1002, 1003);
UPDATE ticket SET creado_en = NOW() - INTERVAL 20 DAY  WHERE id IN (1107, 1041, 1042, 1043, 1044, 1045, 1046);
UPDATE historia SET creado_en = NOW() - INTERVAL 25 DAY WHERE fase_id = 1004;

-- ACTV-44 lleva más de 3 días en QA
UPDATE ticket SET actualizado_en = NOW() - INTERVAL 4 DAY WHERE id = 1044;

INSERT IGNORE INTO ticket_dependencia (ticket_id, depende_de_id) VALUES (1041, 1107);

-- Detalle del ticket (PDF pág. 29): ACTV-41 con su descripción técnica, subtareas y PR; ACTV-45 en progreso
UPDATE ticket SET
    descripcion = 'Crear PedidoController (GET /api/clientes/{id}/pedidos), PedidoService y PedidoRepository con Spring Data JPA. Paginación con Pageable (20 por página), filtro por estado con Specification y orden por fecha descendente. Validar contrato activo antes de responder.',
    capas = 'Base de datos, Entidad / Repository, Servicio, Controller REST, Pruebas',
    fecha_limite = '2026-06-12', horas_registradas = 4,
    rama = 'feature/actv-41-servicio-de-consulta-de-pedidos-en-spring'
WHERE id = 1041 AND descripcion IS NULL;

UPDATE ticket SET
    descripcion = 'Pantalla Mis facturas en Flutter: lista paginada con folio, fecha y total, y botón para descargar el PDF. Usa el servicio de ACTV-43.',
    capas = 'Pantalla, Pruebas',
    rama = 'feature/actv-45-pantalla-mis-facturas-en-flutter'
WHERE id = 1045 AND descripcion IS NULL;

INSERT IGNORE INTO ticket_subtarea (id, ticket_id, descripcion, hecha, orden) VALUES
    (1001, 1041, 'Script Flyway V12: tabla pedido y pedido_estado con índices', TRUE,  1),
    (1002, 1041, 'Entidad Pedido y PedidoRepository (Spring Data JPA)',          TRUE,  2),
    (1003, 1041, 'PedidoService: filtro por estado, paginación y orden por fecha', TRUE,  3),
    (1004, 1041, 'PedidoController + PedidoResponseDTO',                          TRUE,  4),
    (1005, 1041, 'Validar contrato activo (regla de negocio de la historia)',         TRUE,  5),
    (1006, 1041, 'Pruebas con JUnit 5 y MockMvc',                                 TRUE,  6),
    (1007, 1045, 'Lista de facturas con paginación',                              TRUE,  1),
    (1008, 1045, 'Botón para descargar el PDF',                                   FALSE, 2),
    (1009, 1045, 'Mensaje cuando no hay facturas',                                FALSE, 3);

INSERT IGNORE INTO pull_request (id, ticket_id, modulo_id, numero, rama_origen, rama_destino, estado, ci_estado, url) VALUES
    (1001, 1041, 1001, 45, 'feature/actv-41-servicio-de-consulta-de-pedidos-en-spring', 'develop', 'APROBADO', 'PASO',
           'https://github.com/equipo/portal-api/pull/45'),
    (1002, 1045, 1003, 18, 'feature/actv-45-pantalla-mis-facturas-en-flutter',          'develop', 'ABIERTO',  'PASO',
           'https://github.com/equipo/portal-app/pull/18');

-- Integrar HU-0016 (PDF pág. 28): TECN-07 ya está en develop; ACTV-41 y ACTV-42 esperan su merge
UPDATE ticket SET rama = 'feature/tecn-07-modelo-de-datos-de-pedidos' WHERE id = 1107 AND rama IS NULL;
UPDATE ticket SET rama = 'feature/actv-42-pantalla-mis-pedidos-en-flutter' WHERE id = 1042 AND rama IS NULL;

INSERT IGNORE INTO pull_request (id, ticket_id, modulo_id, numero, rama_origen, rama_destino, estado, ci_estado, url) VALUES
    (1003, 1107, 1001, 41, 'feature/tecn-07-modelo-de-datos-de-pedidos',      'develop', 'INTEGRADO', 'PASO',
           'https://github.com/equipo/portal-api/pull/41'),
    (1004, 1042, 1003, 12, 'feature/actv-42-pantalla-mis-pedidos-en-flutter', 'develop', 'APROBADO',  'PASO',
           'https://github.com/equipo/portal-app/pull/12');

INSERT IGNORE INTO comentario (id, entidad, entidad_id, usuario_id, texto, creado_en) VALUES
    (1003, 'TICKET', 1041, 1001, 'Ojo con el índice por cliente_id y fecha en la tabla pedido.', NOW() - INTERVAL 1 DAY),
    (1004, 'TICKET', 1041, 1003, 'Listo, agregado en V12. Me falta la validación de contrato y las pruebas.', NOW() - INTERVAL 2 HOUR);

-- ---------- Historial (alimenta "Actividad reciente" y "Cerrados esta semana") ----------
INSERT IGNORE INTO historial_estado (id, entidad, entidad_id, estado_anterior, estado_nuevo, usuario_id, creado_en) VALUES
    (1001, 'FASE',     1005, NULL,           'PLANEADA',    1001, NOW() - INTERVAL 1 DAY),
    (1002, 'TICKET',   1041, 'EN_QA',        'APROBADA',    1002, NOW() - INTERVAL 1 DAY),
    (1003, 'TICKET',   1302, 'CONCLUSIONES', 'REVISADA',    1001, NOW() - INTERVAL 2 DAY),
    (1004, 'TICKET',   1404, 'EN_REVISION',  'PUBLICADA',   1002, NOW() - INTERVAL 3 DAY),
    (1005, 'HISTORIA', 1018, NULL,           'BORRADOR',    1002, NOW() - INTERVAL 3 HOUR),
    (1006, 'TICKET',   1131, NULL,           'REPORTADO',   1002, NOW() - INTERVAL 2 HOUR),
    (1007, 'TICKET',   1045, 'EN_PROGRESO',  'EN_QA',       1003, NOW() - INTERVAL 1 HOUR),
    (1008, 'TICKET',   1045, 'EN_QA',        'EN_PROGRESO', 1002, NOW() - INTERVAL 10 MINUTE),
    (1009, 'TICKET',   1044, 'EN_PROGRESO',  'EN_QA',       1003, NOW() - INTERVAL 4 DAY);

-- HU-0016 (Detalle de la historia, PDF pág. 24): sus pasos y qué ticket cubre cada criterio
INSERT IGNORE INTO historial_estado (id, entidad, entidad_id, estado_anterior, estado_nuevo, motivo, automatico, usuario_id, creado_en) VALUES
    (1010, 'HISTORIA', 1016, NULL,            'BORRADOR',      NULL,                         FALSE, 1002, NOW() - INTERVAL 25 DAY),
    (1011, 'HISTORIA', 1016, 'BORRADOR',      'LISTA',         NULL,                         FALSE, 1002, NOW() - INTERVAL 24 DAY),
    (1012, 'HISTORIA', 1016, 'LISTA',         'EN_DESARROLLO', NULL,                         FALSE, 1001, NOW() - INTERVAL 23 DAY),
    (1013, 'HISTORIA', 1016, 'EN_DESARROLLO', 'EN_VALIDACION', 'Se aprobó el último ticket', TRUE,  NULL, NOW() - INTERVAL 1 DAY);

INSERT IGNORE INTO ticket_criterio (ticket_id, criterio_id) VALUES
    (1041, 1001), (1041, 1002), (1042, 1002), (1041, 1003), (1042, 1004);

-- ---------- Releases, demo y comentarios ----------
INSERT IGNORE INTO version_release (id, proyecto_id, fase_id, version, tipo, estado, resumen, publicado_por, publicado_en) VALUES
    (1001, 1001, 1002, 'v1.1.0', 'FASE', 'PUBLICADA',  'Catálogo de productos',                  1001, '2026-03-15 18:00:00'),
    (1002, 1001, 1003, 'v1.2.0', 'FASE', 'PUBLICADA',  'Inicio de sesión y gestión de clientes', 1001, '2026-04-30 18:00:00'),
    (1003, 1001, 1004, 'v1.3.0', 'FASE', 'PREPARANDO', 'Portal móvil',                           NULL, NULL);

INSERT IGNORE INTO demo (id, fase_id, titulo, url, estado) VALUES
    (1001, 1004, 'Fase 4 · Portal móvil', 'https://staging.portal.gfl.mx', 'PENDIENTE');

INSERT IGNORE INTO comentario (id, entidad, entidad_id, usuario_id, texto) VALUES
    (1001, 'HISTORIA', 1016, 1002, 'Probé en staging con el cliente demo. Solo me falta revisar el mensaje cuando no hay pedidos.'),
    (1002, 'HISTORIA', 1016, 1003, 'El mensaje vacío ya está en ACTV-42, build 1.3.0-rc1 en staging.');

-- Otros tipos de ticket (PDF pág. 30): la investigación ya revisada con su recomendación y la documentación funcional
UPDATE ticket SET conclusiones = 'Comparé Chart.js, ECharts y ngx-charts. Chart.js cubre barras, líneas y dona, pesa unos 60 KB y tiene buena documentación. Recomiendo usar Chart.js con ng2-charts para el Dashboard.'
WHERE id = 1302 AND conclusiones IS NULL;
UPDATE ticket SET documento = 'FUNCIONAL', visible_cliente = TRUE WHERE id = 1404 AND documento IS NULL;

-- Pestaña Módulos (PDF pág. 35): Pedro es el responsable técnico de los módulos del Portal
UPDATE modulo SET responsable_id = 1003 WHERE proyecto_id = 1001 AND responsable_id IS NULL;
