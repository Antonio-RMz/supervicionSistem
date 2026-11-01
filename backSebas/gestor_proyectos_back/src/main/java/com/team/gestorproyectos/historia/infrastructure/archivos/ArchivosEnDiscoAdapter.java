package com.team.gestorproyectos.historia.infrastructure.archivos;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.team.gestorproyectos.shared.domain.exception.NoEncontradoException;
import com.team.gestorproyectos.shared.domain.port.ArchivosPort;

/** Guarda cada archivo en la carpeta app.adjuntos.carpeta con un nombre único; la ruta es solo ese nombre. */
@Component
public class ArchivosEnDiscoAdapter implements ArchivosPort {

    private final Path carpeta;

    public ArchivosEnDiscoAdapter(@Value("${app.adjuntos.carpeta}") String carpeta) {
        this.carpeta = Path.of(carpeta).toAbsolutePath().normalize();
    }

    @Override
    public String guardar(byte[] contenido, String extension) {
        String nombre = UUID.randomUUID() + "." + extension;
        try {
            Files.createDirectories(carpeta);
            Files.write(carpeta.resolve(nombre), contenido);
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo guardar el archivo", e);
        }
        return nombre;
    }

    @Override
    public byte[] leer(String ruta) {
        try {
            return Files.readAllBytes(resolver(ruta));
        } catch (IOException e) {
            throw new NoEncontradoException("No encontramos el archivo de esa imagen");
        }
    }

    @Override
    public void borrar(String ruta) {
        try {
            Files.deleteIfExists(resolver(ruta));
        } catch (IOException e) {
            // Ya no está ligado a nada: si no se pudo borrar solo queda ocupando espacio.
        }
    }

    /** Solo archivos dentro de la carpeta: una ruta con ".." o absoluta no sale de ahí. */
    private Path resolver(String ruta) {
        Path archivo = carpeta.resolve(ruta).normalize();
        if (!archivo.getParent().equals(carpeta)) {
            throw new NoEncontradoException("No encontramos el archivo de esa imagen");
        }
        return archivo;
    }
}
