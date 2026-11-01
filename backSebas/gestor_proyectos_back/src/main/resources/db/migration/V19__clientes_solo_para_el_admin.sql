-- Clientes es solo del Administrador: el Líder ya no tiene "cliente.gestionar".
DELETE rp FROM rol_permiso rp
JOIN rol r ON r.id = rp.rol_id
JOIN permiso p ON p.id = rp.permiso_id
WHERE p.codigo = 'cliente.gestionar' AND r.nombre <> 'Administrador';

-- Ni como permiso extra a una persona.
DELETE up FROM usuario_permiso up
JOIN permiso p ON p.id = up.permiso_id
WHERE p.codigo = 'cliente.gestionar';
