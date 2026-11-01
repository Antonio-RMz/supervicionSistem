package com.team.gestorproyectos.shared;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

/** El dominio y la aplicación no conocen frameworks: solo la infraestructura los importa. */
class ArquitecturaHexagonalTest {

    private static final Path FUENTES = Path.of("src/main/java/com/team/gestorproyectos");

    @Test
    void dominioYAplicacionNoImportanFrameworks() throws IOException {
        List<String> infractores;
        try (Stream<Path> archivos = Files.walk(FUENTES)) {
            infractores = archivos
                    .filter(archivo -> archivo.toString().endsWith(".java"))
                    .filter(archivo -> archivo.toString().matches(".*/(domain|application)/.*"))
                    .filter(ArquitecturaHexagonalTest::importaFramework)
                    .map(archivo -> FUENTES.relativize(archivo).toString())
                    .toList();
        }
        assertThat(infractores).isEmpty();
    }

    private static boolean importaFramework(Path archivo) {
        try (Stream<String> lineas = Files.lines(archivo)) {
            return lineas.anyMatch(linea -> linea.startsWith("import org.springframework.")
                    || linea.startsWith("import jakarta.persistence."));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
