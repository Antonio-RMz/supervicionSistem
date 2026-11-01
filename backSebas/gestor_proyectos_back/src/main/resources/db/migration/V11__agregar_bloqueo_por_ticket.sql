-- =====================================================================
-- V11 · Ticket bloqueado por otro ticket (PDF pág. 26, Marcar bloqueada)
-- =====================================================================
-- Si lo bloquea otro ticket, se desbloquea solo cuando ese se termina. NULL = algo externo.
ALTER TABLE ticket
    ADD COLUMN bloqueado_por_id BIGINT NULL AFTER origen_ticket_id,
    ADD CONSTRAINT fk_ticket_bloqueado_por FOREIGN KEY (bloqueado_por_id) REFERENCES ticket (id);
