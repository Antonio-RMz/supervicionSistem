package com.team.gestorproyectos.cliente.domain.model;

import java.util.regex.Pattern;

import com.team.gestorproyectos.shared.domain.exception.ReglaNegocioException;

/**
 * Lo que se captura en "Nuevo cliente" o al editarlo (PDF pág. 10). Se limpia al construirse.
 *
 * @param nombreCorto si viene vacío se toma la razón social sin "S.A. de C.V."
 */
public record DatosCliente(
        String razonSocial,
        String nombreCorto,
        String rfc,
        Giro giro,
        String ciudad,
        String contactoNombre,
        String contactoCorreo,
        String contactoTelefono,
        String notas,
        boolean activo) {

    /** Persona moral (12) o física (13): letras, fecha AAMMDD y homoclave. */
    private static final Pattern RFC = Pattern.compile("^[A-ZÑ&]{3,4}\\d{6}[A-Z0-9]{3}$");

    /** ", S.A. de C.V.", " S. de R.L. de C.V.", " SAPI de CV"... al final de la razón social. */
    private static final Pattern TIPO_SOCIEDAD = Pattern.compile(
            "[,\\s]+(S\\.?\\s?A\\.?\\s?P\\.?\\s?I\\.?|S\\.?\\s?A\\.?|S\\.?\\s?de\\s?R\\.?\\s?L\\.?|S\\.?\\s?C\\.?|A\\.?\\s?C\\.?)"
                    + "(\\s?de\\s?C\\.?\\s?V\\.?)?\\.?$",
            Pattern.CASE_INSENSITIVE);

    public DatosCliente {
        razonSocial = limpio(razonSocial);
        if (razonSocial == null) {
            throw new ReglaNegocioException("Escribe el nombre o razón social");
        }
        nombreCorto = limpio(nombreCorto);
        if (nombreCorto == null) {
            nombreCorto = TIPO_SOCIEDAD.matcher(razonSocial).replaceFirst("").trim();
        }
        rfc = limpio(rfc) == null ? null : rfc.trim().toUpperCase();
        if (rfc != null && !RFC.matcher(rfc).matches()) {
            throw new ReglaNegocioException("El RFC " + rfc + " no tiene un formato válido");
        }
        ciudad = limpio(ciudad);
        // Solo el nombre es obligatorio; el contacto puede llenarse después.
        contactoNombre = limpio(contactoNombre);
        contactoCorreo = limpio(contactoCorreo) == null ? null : contactoCorreo.trim().toLowerCase();
        contactoTelefono = limpio(contactoTelefono);
        notas = limpio(notas);
    }

    private static String limpio(String texto) {
        return texto == null || texto.isBlank() ? null : texto.trim();
    }
}
