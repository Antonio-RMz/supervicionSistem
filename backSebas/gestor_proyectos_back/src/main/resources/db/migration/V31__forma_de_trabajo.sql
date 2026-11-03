-- =====================================================================
-- V31 · Forma de trabajo del proyecto
-- =====================================================================
-- GITFLOW: develop + main; la historia aceptada se integra a develop y la fase sube a main.
-- NORMAL: solo main; la Actividad termina con su PR aprobado (o sin PR si requiere_pr = FALSE) y la historia
-- queda integrada al aceptarse. Se elige al crear el proyecto y ya no cambia. Los que ya existen siguen en Gitflow.

ALTER TABLE proyecto
    ADD COLUMN forma_trabajo VARCHAR(10) NOT NULL DEFAULT 'GITFLOW' AFTER notificar_cliente_fase,
    ADD COLUMN requiere_pr   BOOLEAN     NOT NULL DEFAULT TRUE     AFTER forma_trabajo;
