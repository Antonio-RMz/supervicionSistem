-- =====================================================================
-- V15 · Responsable técnico de cada módulo (PDF pág. 35)
-- =====================================================================
ALTER TABLE modulo
    ADD COLUMN responsable_id BIGINT AFTER rama_base,
    ADD CONSTRAINT fk_modulo_responsable FOREIGN KEY (responsable_id) REFERENCES usuario (id);
