-- ─────────────────────────────────────────────────────────────────────────────
-- V34 · Notificaciones y usuario de git
-- ─────────────────────────────────────────────────────────────────────────────

-- Avisos para una persona: le asignaron un ticket, reportaron un bug en su proyecto
-- o comentaron un ticket suyo. Nunca se avisa a quien hizo la acción.
CREATE TABLE notificacion (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id  BIGINT       NOT NULL COMMENT 'A quién le llega',
    tipo        VARCHAR(20)  NOT NULL,
    texto       VARCHAR(300) NOT NULL COMMENT '"Ana te asignó BUGS-12 · Error al guardar"',
    proyecto_id BIGINT       NOT NULL,
    ticket_id   BIGINT       NULL COMMENT 'A dónde lleva al abrirla',
    leida       BOOLEAN      NOT NULL DEFAULT FALSE,
    creado_en   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_notificacion_usuario (usuario_id, leida, id),
    CONSTRAINT fk_notificacion_usuario  FOREIGN KEY (usuario_id)  REFERENCES usuario (id),
    CONSTRAINT fk_notificacion_proyecto FOREIGN KEY (proyecto_id) REFERENCES proyecto (id) ON DELETE CASCADE,
    CONSTRAINT fk_notificacion_ticket   FOREIGN KEY (ticket_id)   REFERENCES ticket (id) ON DELETE CASCADE,
    CONSTRAINT chk_notificacion_tipo    CHECK (tipo IN ('ASIGNACION', 'BUG_REPORTADO', 'COMENTARIO'))
);

-- El usuario de git de cada persona (GitHub, GitLab, Bitbucket...). Lo captura el Admin.
ALTER TABLE usuario
    ADD COLUMN usuario_git VARCHAR(100) NULL COMMENT 'Usuario en la plataforma de git; NULL = sin capturar' AFTER correo;
