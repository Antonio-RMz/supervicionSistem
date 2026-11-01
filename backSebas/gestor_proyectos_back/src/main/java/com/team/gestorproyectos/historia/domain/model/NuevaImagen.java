package com.team.gestorproyectos.historia.domain.model;

import com.team.gestorproyectos.shared.domain.exception.ReglaNegocioException;

/**
 * Una imagen que se sube a la historia: una captura, un boceto o el diseño, junto con el enlace de Figma.
 * El tipo se toma de los primeros bytes del archivo, no de lo que diga el navegador.
 *
 * @param tipoMime image/png, image/jpeg, image/gif o image/webp
 */
public record NuevaImagen(String nombre, String tipoMime, byte[] contenido) {

    public static final long TAMANO_MAXIMO = 5L * 1024 * 1024;
    public static final int MAXIMO_POR_HISTORIA = 10;

    private static final int LARGO_NOMBRE = 200;

    /** Arma la imagen con lo que subió el navegador; rechaza lo que no sea PNG, JPG, GIF o WebP. */
    public static NuevaImagen de(String nombreOriginal, byte[] contenido) {
        if (contenido == null || contenido.length == 0) {
            throw new ReglaNegocioException("Elige una imagen");
        }
        if (contenido.length > TAMANO_MAXIMO) {
            throw new ReglaNegocioException("La imagen pesa más de 5 MB");
        }
        String tipo = tipoDe(contenido);
        if (tipo == null) {
            throw new ReglaNegocioException("Solo se suben imágenes PNG, JPG, GIF o WebP");
        }
        return new NuevaImagen(limpiarNombre(nombreOriginal, tipo), tipo, contenido);
    }

    public long tamanoBytes() {
        return contenido.length;
    }

    /** "png", "jpg"... para el archivo que se guarda. */
    public String extension() {
        return extensionDe(tipoMime);
    }

    private static String extensionDe(String tipo) {
        return switch (tipo) {
            case "image/png" -> "png";
            case "image/jpeg" -> "jpg";
            case "image/gif" -> "gif";
            default -> "webp";
        };
    }

    private static String tipoDe(byte[] b) {
        if (empieza(b, 0, 0x89, 'P', 'N', 'G')) {
            return "image/png";
        }
        if (empieza(b, 0, 0xFF, 0xD8, 0xFF)) {
            return "image/jpeg";
        }
        if (empieza(b, 0, 'G', 'I', 'F', '8')) {
            return "image/gif";
        }
        if (empieza(b, 0, 'R', 'I', 'F', 'F') && empieza(b, 8, 'W', 'E', 'B', 'P')) {
            return "image/webp";
        }
        return null;
    }

    private static boolean empieza(byte[] b, int desde, int... firma) {
        if (b.length < desde + firma.length) {
            return false;
        }
        for (int i = 0; i < firma.length; i++) {
            if ((b[desde + i] & 0xFF) != firma[i]) {
                return false;
            }
        }
        return true;
    }

    /** Sin carpetas ni caracteres de control; si no trae nombre, "imagen.png". */
    private static String limpiarNombre(String original, String tipo) {
        String nombre = original == null ? "" : original.replace('\\', '/');
        nombre = nombre.substring(nombre.lastIndexOf('/') + 1).replaceAll("\\p{Cntrl}", "").trim();
        if (nombre.isEmpty()) {
            return "imagen." + extensionDe(tipo);
        }
        return nombre.length() <= LARGO_NOMBRE ? nombre : nombre.substring(0, LARGO_NOMBRE);
    }
}
