-- Ya no hay Tareas técnicas (TECN), Investigaciones (INVS) ni Documentación (DOCT):
-- los tickets son Actividad, Bug y Mejora.

-- Lo que colgaba de tickets de esos tipos.
UPDATE ticket SET bloqueado_por_id = NULL
WHERE bloqueado_por_id IN (SELECT id FROM (SELECT id FROM ticket WHERE tipo IN ('TECN', 'INVS', 'DOCT')) AS t);
DELETE FROM ticket_dependencia
WHERE ticket_id IN (SELECT id FROM ticket WHERE tipo IN ('TECN', 'INVS', 'DOCT'))
   OR depende_de_id IN (SELECT id FROM ticket WHERE tipo IN ('TECN', 'INVS', 'DOCT'));
DELETE FROM ticket_criterio WHERE ticket_id IN (SELECT id FROM ticket WHERE tipo IN ('TECN', 'INVS', 'DOCT'));
DELETE FROM ticket_subtarea WHERE ticket_id IN (SELECT id FROM ticket WHERE tipo IN ('TECN', 'INVS', 'DOCT'));
DELETE FROM pull_request WHERE ticket_id IN (SELECT id FROM ticket WHERE tipo IN ('TECN', 'INVS', 'DOCT'));
UPDATE solicitud SET ticket_id = NULL WHERE ticket_id IN (SELECT id FROM ticket WHERE tipo IN ('TECN', 'INVS', 'DOCT'));
DELETE FROM comentario
WHERE entidad = 'TICKET' AND entidad_id IN (SELECT id FROM ticket WHERE tipo IN ('TECN', 'INVS', 'DOCT'));
DELETE FROM historial_estado
WHERE entidad = 'TICKET' AND entidad_id IN (SELECT id FROM ticket WHERE tipo IN ('TECN', 'INVS', 'DOCT'));
DELETE FROM adjunto
WHERE entidad = 'TICKET' AND entidad_id IN (SELECT id FROM ticket WHERE tipo IN ('TECN', 'INVS', 'DOCT'));
UPDATE ticket SET origen_ticket_id = NULL;
DELETE FROM ticket WHERE tipo IN ('TECN', 'INVS', 'DOCT');

ALTER TABLE ticket DROP CHECK chk_ticket_tipo;
ALTER TABLE ticket ADD CONSTRAINT chk_ticket_tipo CHECK (tipo IN ('ACTV', 'BUGS', 'MEJR'));

-- Solo los usaban las investigaciones y la documentación.
ALTER TABLE ticket DROP FOREIGN KEY fk_ticket_origen;
ALTER TABLE ticket
    DROP COLUMN origen_ticket_id,
    DROP COLUMN conclusiones,
    DROP COLUMN documento,
    DROP COLUMN visible_cliente;

-- Sus permisos ya no sirven.
DELETE rp FROM rol_permiso rp
JOIN permiso p ON p.id = rp.permiso_id
WHERE p.codigo IN ('revision.aprobar', 'documento.revisar');
DELETE up FROM usuario_permiso up
JOIN permiso p ON p.id = up.permiso_id
WHERE p.codigo IN ('revision.aprobar', 'documento.revisar');
DELETE FROM permiso WHERE codigo IN ('revision.aprobar', 'documento.revisar');
