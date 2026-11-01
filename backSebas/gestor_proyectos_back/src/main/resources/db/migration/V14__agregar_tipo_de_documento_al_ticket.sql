-- =====================================================================
-- V14 · Otros tipos de ticket (PDF pág. 30)
-- =====================================================================
-- De una documentación (DOCT): FUNCIONAL la revisa el Analista de su historia; TECNICA, el Líder.
-- Se indica al enviarla a revisión.
ALTER TABLE ticket
    ADD COLUMN documento VARCHAR(10) AFTER conclusiones;
