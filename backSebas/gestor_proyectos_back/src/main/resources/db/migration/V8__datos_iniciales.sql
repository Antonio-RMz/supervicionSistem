-- =====================================================================
-- V8 · Datos iniciales: roles, permisos y el primer administrador
-- =====================================================================

INSERT INTO rol (codigo, nombre, descripcion) VALUES
    ('ADMIN',         'Administrador',      'Acceso total al sistema'),
    ('LIDER',         'Líder de Proyecto',  'Administra proyectos y equipos'),
    ('ANALISTA',      'Analista Comercial', 'Gestiona requerimientos e historias'),
    ('DESARROLLADOR', 'Desarrollador',      'Desarrolla funcionalidades'),
    ('TESTER',        'Tester',             'Realiza pruebas y validaciones');

INSERT INTO permiso (codigo, nombre, grupo) VALUES
    ('usuario.gestionar',      'Dar de alta usuarios y asignar roles',     'ADMINISTRACION'),
    ('cliente.gestionar',      'Crear y editar clientes',                  'ADMINISTRACION'),
    ('proyecto.crear',         'Crear proyectos',                          'PROYECTO'),
    ('proyecto.editar',        'Editar proyecto, equipo y módulos',        'PROYECTO'),
    ('fase.crear',             'Crear fases',                              'FASE'),
    ('fase.gestionar',         'Iniciar, pausar y cancelar fases',         'FASE'),
    ('fase.reabrir',           'Reabrir una fase cancelada',               'FASE'),
    ('fase.marcar_criterio',   'Marcar criterios manuales de cierre',      'FASE'),
    ('historia.crear',         'Crear historias de usuario',               'HISTORIA'),
    ('historia.marcar_lista',  'Marcar historia como Lista',               'HISTORIA'),
    ('historia.iniciar',       'Iniciar historia',                         'HISTORIA'),
    ('historia.aceptar',       'Aceptar o devolver historia',              'HISTORIA'),
    ('historia.descartar',     'Descartar historia',                       'HISTORIA'),
    ('historia.integrar',      'Integrar historia a develop',              'HISTORIA'),
    ('ticket.crear',           'Crear tickets de trabajo',                 'TICKET'),
    ('ticket.trabajar',        'Iniciar, bloquear y enviar a QA',          'TICKET'),
    ('ticket.aprobar_qa',      'Aprobar o rechazar QA',                    'TICKET'),
    ('ticket.cancelar',        'Cancelar tickets',                         'TICKET'),
    ('bug.reportar',           'Reportar bugs',                            'TICKET'),
    ('bug.confirmar',          'Confirmar bugs y su prioridad',            'TICKET'),
    ('mejora.proponer',        'Proponer mejoras',                         'TICKET'),
    ('mejora.aprobar',         'Aprobar o rechazar mejoras',               'TICKET'),
    ('revision.aprobar',       'Aprobar revisión de código (TECN)',        'TICKET'),
    ('documento.revisar',      'Revisar y publicar documentación',         'TICKET'),
    ('release.publicar',       'Subir a main, crear tag y desplegar',      'RELEASE'),
    ('release.revertir',       'Revertir a la versión anterior',           'RELEASE'),
    ('parche.publicar',        'Publicar parches (hotfix)',                'RELEASE');

-- El Administrador tiene todos los permisos.
INSERT INTO rol_permiso (rol_id, permiso_id)
SELECT r.id, p.id FROM rol r CROSS JOIN permiso p WHERE r.codigo = 'ADMIN';

INSERT INTO rol_permiso (rol_id, permiso_id)
SELECT r.id, p.id FROM rol r JOIN permiso p ON p.codigo IN (
    'cliente.gestionar', 'proyecto.crear', 'proyecto.editar',
    'fase.crear', 'fase.gestionar', 'fase.marcar_criterio',
    'historia.crear', 'historia.iniciar', 'historia.descartar', 'historia.integrar',
    'ticket.crear', 'ticket.cancelar', 'bug.reportar', 'bug.confirmar', 'mejora.aprobar',
    'revision.aprobar', 'documento.revisar',
    'release.publicar', 'release.revertir', 'parche.publicar')
WHERE r.codigo = 'LIDER';

INSERT INTO rol_permiso (rol_id, permiso_id)
SELECT r.id, p.id FROM rol r JOIN permiso p ON p.codigo IN (
    'historia.crear', 'historia.marcar_lista', 'historia.aceptar', 'historia.descartar',
    'bug.reportar', 'mejora.proponer', 'documento.revisar')
WHERE r.codigo = 'ANALISTA';

INSERT INTO rol_permiso (rol_id, permiso_id)
SELECT r.id, p.id FROM rol r JOIN permiso p ON p.codigo IN (
    'ticket.crear', 'ticket.trabajar', 'bug.reportar', 'revision.aprobar')
WHERE r.codigo = 'DESARROLLADOR';

INSERT INTO rol_permiso (rol_id, permiso_id)
SELECT r.id, p.id FROM rol r JOIN permiso p ON p.codigo IN (
    'ticket.aprobar_qa', 'bug.reportar')
WHERE r.codigo = 'TESTER';

-- Primer administrador. Contraseña temporal: Cambiar123 (se pide cambiarla al entrar).
INSERT INTO usuario (nombre, apellidos, correo, password_hash, tipo, cambiar_password) VALUES
    ('Administrador', NULL, 'admin@teamdev.mx',
     '$2y$10$pnGhSVbhgOfoFJzOzILvX.Xwia6j5XItZeZwcUMUcp4FbYgVvcqTW', 'EQUIPO', TRUE);

INSERT INTO usuario_rol (usuario_id, rol_id)
SELECT u.id, r.id FROM usuario u JOIN rol r ON r.codigo = 'ADMIN' WHERE u.correo = 'admin@teamdev.mx';
