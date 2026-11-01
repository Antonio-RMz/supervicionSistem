-- Ajustes a lo que hace cada rol:
--   · Solo el Administrador crea proyectos y arma su equipo.
--   · El Líder edita su proyecto y sus repositorios (antes "módulos").
--   · El Desarrollador ya no crea tickets: solo trabaja los que le asignan.

INSERT INTO permiso (codigo, nombre, grupo) VALUES
    ('proyecto.equipo', 'Armar el equipo del proyecto', 'PROYECTO');

INSERT INTO rol_permiso (rol_id, permiso_id)
SELECT r.id, p.id FROM rol r JOIN permiso p ON p.codigo = 'proyecto.equipo' WHERE r.codigo = 'ADMIN';

UPDATE permiso SET nombre = 'Editar proyecto y sus repositorios' WHERE codigo = 'proyecto.editar';

DELETE rp FROM rol_permiso rp
JOIN rol r ON r.id = rp.rol_id
JOIN permiso p ON p.id = rp.permiso_id
WHERE (p.codigo = 'proyecto.crear' AND r.codigo = 'LIDER')
   OR (p.codigo = 'ticket.crear' AND r.codigo = 'DESARROLLADOR');
