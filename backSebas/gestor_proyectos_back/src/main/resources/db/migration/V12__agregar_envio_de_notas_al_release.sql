-- =====================================================================
-- V12 · Notas del release para el cliente (PDF pág. 27)
-- =====================================================================
-- Si las notas del release se envían al cliente al publicar.
ALTER TABLE version_release
    ADD COLUMN enviar_notas BOOLEAN NOT NULL DEFAULT TRUE AFTER notas;
