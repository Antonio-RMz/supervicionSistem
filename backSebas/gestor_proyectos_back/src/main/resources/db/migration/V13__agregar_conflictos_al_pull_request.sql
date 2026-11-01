-- =====================================================================
-- V13 · Revisión previa al integrar una historia (PDF pág. 28)
-- =====================================================================
-- Si la rama del PR choca con develop. Un PR con conflictos impide integrar la historia.
ALTER TABLE pull_request
    ADD COLUMN con_conflictos BOOLEAN NOT NULL DEFAULT FALSE AFTER ci_estado;
