-- QA se hace a la historia completa: cada ticket manda su PR y lo revisa el Líder.
-- Solo el hotfix (bug Crítico o Alto en Mantenimiento) sigue pasando por el Tester antes del parche.

UPDATE ticket t
JOIN fase f ON f.id = t.fase_id
SET t.estado = 'EN_REVISION'
WHERE t.estado = 'EN_QA'
  AND NOT (t.tipo = 'BUGS' AND f.es_mantenimiento = TRUE AND t.severidad IN ('CRITICA', 'ALTA'));

UPDATE permiso SET nombre = 'Probar en QA las historias y los hotfix' WHERE codigo = 'ticket.aprobar_qa';
UPDATE permiso SET nombre = 'Iniciar, bloquear y enviar PR' WHERE codigo = 'ticket.trabajar';
