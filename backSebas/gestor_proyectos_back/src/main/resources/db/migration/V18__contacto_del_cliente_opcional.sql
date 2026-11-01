-- Al dar de alta un cliente solo el nombre es obligatorio; el contacto puede llenarse después.
ALTER TABLE cliente MODIFY contacto_nombre VARCHAR(120) NULL;
