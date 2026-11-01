-- =====================================================================
-- V17 · La fase es la decisión del release; las reglas de negocio son de la historia
-- =====================================================================
-- Antes la fase guardaba requerimientos y reglas, y cada historia elegía de ahí. Ahora la fase solo dice qué
-- sale y cuándo; cada historia escribe sus reglas y todos sus tickets las respetan (y van en el prompt de IA).

CREATE TABLE historia_regla_negocio (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    historia_id BIGINT       NOT NULL,
    descripcion VARCHAR(300) NOT NULL,
    orden       INT          NOT NULL DEFAULT 0,
    CONSTRAINT fk_historia_regla_negocio_historia FOREIGN KEY (historia_id) REFERENCES historia (id)
);

-- Cada historia se queda con el texto de las reglas que tenía ligadas.
INSERT INTO historia_regla_negocio (historia_id, descripcion, orden)
SELECT hr.historia_id, r.descripcion, r.orden
FROM historia_regla hr
JOIN fase_regla_negocio r ON r.id = hr.regla_id
ORDER BY hr.historia_id, r.orden, r.id;

DROP TABLE historia_regla;
DROP TABLE fase_regla_negocio;

-- El requerimiento de la fase ya lo dice el título de la historia.
ALTER TABLE historia
    DROP FOREIGN KEY fk_historia_requerimiento,
    DROP COLUMN requerimiento_id;
DROP TABLE fase_requerimiento;
