-- =====================================================================
-- V30 · Usuarios del sistema
-- =====================================================================
-- Quiénes usan el software que se construye (Administrador, Escuela, Alumno...), no el equipo que lo hace.
-- Son el "Como" de las historias y la IA los usa al proponerlas.

CREATE TABLE proyecto_usuario_sistema (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    proyecto_id BIGINT       NOT NULL,
    nombre      VARCHAR(80)  NOT NULL,
    descripcion VARCHAR(300),
    orden       INT          NOT NULL DEFAULT 0,
    UNIQUE KEY uk_usuario_sistema_nombre (proyecto_id, nombre),
    CONSTRAINT fk_usuario_sistema_proyecto FOREIGN KEY (proyecto_id) REFERENCES proyecto (id)
);
