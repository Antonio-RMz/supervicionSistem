-- El Líder también marca una historia como Lista (además de su Analista): él crea su Actividad.
INSERT INTO rol_permiso (rol_id, permiso_id)
SELECT r.id, p.id FROM rol r JOIN permiso p ON p.codigo = 'historia.marcar_lista' WHERE r.codigo = 'LIDER';
