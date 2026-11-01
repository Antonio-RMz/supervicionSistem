package com.team.gestorproyectos.historia.infrastructure.archivos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.team.gestorproyectos.shared.domain.exception.NoEncontradoException;

class ArchivosEnDiscoAdapterTest {

    @TempDir
    Path carpeta;

    @Test
    void guardaLeeYBorraDentroDeSuCarpeta() {
        var archivos = new ArchivosEnDiscoAdapter(carpeta.toString());
        String ruta = archivos.guardar(new byte[] {1, 2, 3}, "png");

        assertThat(ruta).endsWith(".png").doesNotContain("/");
        assertThat(archivos.leer(ruta)).containsExactly(1, 2, 3);
        archivos.borrar(ruta);
        assertThat(Files.exists(carpeta.resolve(ruta))).isFalse();
    }

    @Test
    void noSaleDeLaCarpeta() {
        var archivos = new ArchivosEnDiscoAdapter(carpeta.resolve("adjuntos").toString());
        assertThatThrownBy(() -> archivos.leer("../secreto.txt")).isInstanceOf(NoEncontradoException.class);
        assertThatThrownBy(() -> archivos.leer("/etc/passwd")).isInstanceOf(NoEncontradoException.class);
    }
}
