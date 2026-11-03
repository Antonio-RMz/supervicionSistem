-- Cada proyecto puede tener su propio ícono (imagen); si no tiene, se siguen mostrando sus iniciales.
ALTER TABLE proyecto
    ADD COLUMN icono_ruta VARCHAR(300) NULL COMMENT 'Archivo en la carpeta de adjuntos; NULL = sin ícono',
    ADD COLUMN icono_tipo VARCHAR(30)  NULL COMMENT 'image/png, image/jpeg, image/gif o image/webp';
