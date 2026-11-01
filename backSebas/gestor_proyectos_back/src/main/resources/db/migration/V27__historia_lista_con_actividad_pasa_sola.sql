-- Ya no hay botón "Actividades listas": una historia Lista que tiene su Actividad con Desarrollador
-- queda Lista para desarrollo (o En desarrollo, si su fase ya corre). Pasa a las que ya la tenían.

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
       'Ya tiene su Actividad', TRUE
FROM historia_con_actividad;

UPDATE historia h
JOIN historia_con_actividad c ON c.id = h.id
SET h.estado = IF(c.fase_estado = 'EN_CURSO', 'EN_DESARROLLO', 'LISTA_DESARROLLO');

DROP TEMPORARY TABLE historia_con_actividad;
