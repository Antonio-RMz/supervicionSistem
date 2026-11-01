package com.team.gestorproyectos.shared.domain.port;

/** Donde se guardan los archivos que se suben (las imágenes de las historias). */
public interface ArchivosPort {

    /** @return la ruta con la que se vuelve a leer o borrar */
    String guardar(byte[] contenido, String extension);

    byte[] leer(String ruta);

    void borrar(String ruta);
}
