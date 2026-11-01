-- =====================================================================
-- V9 · Giro del cliente y nombre corto único (PDF pág. 10, Nuevo cliente)
-- =====================================================================
-- giro: COMERCIO, MANUFACTURA, SERVICIOS, TECNOLOGIA, SALUD, EDUCACION, GOBIERNO, OTRO

ALTER TABLE cliente
    ADD COLUMN giro VARCHAR(20) AFTER rfc,
    ADD UNIQUE KEY uk_cliente_nombre_corto (nombre_corto);
