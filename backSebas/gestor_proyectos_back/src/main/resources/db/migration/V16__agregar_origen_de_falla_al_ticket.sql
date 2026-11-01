-- =====================================================================
-- V16 · Reportar falla en producción (PDF pág. 39)
-- =====================================================================
-- historia_origen_id: "Viene de", la historia donde nació lo que falla. No es historia_id: el bug vive en
-- Mantenimiento y no debe mover el avance de esa historia.
-- version_reportada: la versión que estaba en producción cuando se reportó.
ALTER TABLE ticket
    ADD COLUMN historia_origen_id BIGINT AFTER origen_ticket_id,
    ADD COLUMN version_reportada VARCHAR(20) AFTER historia_origen_id,
    ADD CONSTRAINT fk_ticket_historia_origen FOREIGN KEY (historia_origen_id) REFERENCES historia (id);
