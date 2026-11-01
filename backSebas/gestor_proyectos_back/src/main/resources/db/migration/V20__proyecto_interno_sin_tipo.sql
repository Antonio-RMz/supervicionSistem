-- Un proyecto interno no tiene cliente.
ALTER TABLE proyecto MODIFY cliente_id BIGINT NULL;

-- El tipo de proyecto ya no se captura: lo dicen sus módulos (API, Web, Móvil...).
ALTER TABLE proyecto DROP COLUMN tipo;
