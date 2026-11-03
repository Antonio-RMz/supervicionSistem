-- ─────────────────────────────────────────────────────────────────────────────
-- V35 · Notificaciones de historias
-- Como con los tickets: cuando a alguien le asignan una historia (de responsable o para
-- desarrollarla), le llega un aviso que lleva a la historia.
-- ─────────────────────────────────────────────────────────────────────────────
ALTER TABLE notificacion
    ADD COLUMN historia_id BIGINT NULL COMMENT 'A dónde lleva si no es de un ticket' AFTER ticket_id,
    ADD CONSTRAINT fk_notificacion_historia FOREIGN KEY (historia_id) REFERENCES historia (id) ON DELETE CASCADE;
