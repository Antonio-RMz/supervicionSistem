-- =====================================================================
-- V29 · Diseño de la fase
-- =====================================================================
-- Un solo enlace de Figma para toda la fase. Las historias sin diseño propio lo heredan
-- mientras figma_para_todas esté encendido; la que necesite otro lo pone en su sección Diseño.

ALTER TABLE fase
    ADD COLUMN figma_url        VARCHAR(300) NULL,
    ADD COLUMN figma_para_todas BOOLEAN      NOT NULL DEFAULT TRUE;
