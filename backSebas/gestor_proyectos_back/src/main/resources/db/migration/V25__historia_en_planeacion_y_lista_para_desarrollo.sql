-- La historia ahora se planea antes de quedar Lista, y sus Actividades son un paso aparte:
--   Borrador → En planeación → Lista → Lista para desarrollo → En desarrollo (al iniciar la fase).
-- Antes, Lista ya pedía la Actividad con Desarrollador: esas pasan a Lista para desarrollo
-- y, si su fase ya corre, arrancan (ya no hay botón "Iniciar historia").

CREATE TEMPORARY TABLE historia_con_actividad AS
SELECT h.id, f.estado AS fase_estado
FROM historia h
JOIN fase f ON f.id = h.fase_id
WHERE h.estado = 'LISTA'
  AND EXISTS (SELECT 1 FROM ticket t
              WHERE t.historia_id = h.id AND t.tipo = 'ACTV' AND t.responsable_id IS NOT NULL
                AND t.estado NOT IN ('CANCELADA', 'NO_REPRODUCIBLE', 'RECHAZADA'));

INSERT INTO historial_estado (entidad, entidad_id, estado_anterior, estado_nuevo, motivo, automatico)
SELECT 'HISTORIA', id, 'LISTA', IF(fase_estado = 'EN_CURSO', 'EN_DESARROLLO', 'LISTA_DESARROLLO'),
       'Ya tenía sus Actividades', TRUE
FROM historia_con_actividad;

UPDATE historia h
JOIN historia_con_actividad c ON c.id = h.id
SET h.estado = IF(c.fase_estado = 'EN_CURSO', 'EN_DESARROLLO', 'LISTA_DESARROLLO');

DROP TEMPORARY TABLE historia_con_actividad;
