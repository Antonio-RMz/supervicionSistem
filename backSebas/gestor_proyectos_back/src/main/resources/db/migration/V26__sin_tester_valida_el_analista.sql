-- Ya no hay Tester:
--   · La historia, con todos sus PR aprobados, pasa directo a En validación: la prueba completa su Analista.
--   · Los hotfix los prueba y verifica en producción un Analista del equipo.

-- Las historias que esperaban QA pasan a validación.
INSERT INTO historial_estado (entidad, entidad_id, estado_anterior, estado_nuevo, motivo, automatico)
SELECT 'HISTORIA', h.id, 'EN_QA', 'EN_VALIDACION', 'Ya no hay Tester: la valida su Analista', TRUE
FROM historia h WHERE h.estado = 'EN_QA';

UPDATE historia SET estado = 'EN_VALIDACION' WHERE estado = 'EN_QA';

-- Probar y verificar los hotfix pasa al Analista.
UPDATE permiso SET nombre = 'Probar y verificar los hotfix' WHERE codigo = 'ticket.aprobar_qa';

INSERT INTO rol_permiso (rol_id, permiso_id)
SELECT r.id, p.id FROM rol r JOIN permiso p ON p.codigo = 'ticket.aprobar_qa'
WHERE r.codigo = 'ANALISTA'
  AND NOT EXISTS (SELECT 1 FROM rol_permiso rp WHERE rp.rol_id = r.id AND rp.permiso_id = p.id);

-- Fuera el rol: quien solo era Tester queda como Analista.
INSERT INTO usuario_rol (usuario_id, rol_id)
SELECT ur.usuario_id, ra.id
FROM usuario_rol ur
JOIN rol rt ON rt.id = ur.rol_id AND rt.codigo = 'TESTER'
JOIN rol ra ON ra.codigo = 'ANALISTA'
WHERE NOT EXISTS (SELECT 1 FROM usuario_rol otro WHERE otro.usuario_id = ur.usuario_id AND otro.rol_id <> rt.id);

DELETE ur FROM usuario_rol ur JOIN rol r ON r.id = ur.rol_id WHERE r.codigo = 'TESTER';
DELETE rp FROM rol_permiso rp JOIN rol r ON r.id = rp.rol_id WHERE r.codigo = 'TESTER';
DELETE FROM rol WHERE codigo = 'TESTER';
