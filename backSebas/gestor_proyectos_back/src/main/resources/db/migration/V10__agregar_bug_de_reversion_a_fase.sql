-- =====================================================================
-- V10 · El BUGS que se crea al revertir una fase (PDF pág. 20 y 25)
-- =====================================================================
-- "Publicar de nuevo" solo se habilita cuando este bug se cierra.
ALTER TABLE fase
    ADD COLUMN bug_reversion_id BIGINT NULL AFTER reanudar_el,
    ADD CONSTRAINT fk_fase_bug_reversion FOREIGN KEY (bug_reversion_id) REFERENCES ticket (id);
