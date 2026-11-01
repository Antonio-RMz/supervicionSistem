package com.team.gestorproyectos.historia.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import com.team.gestorproyectos.shared.domain.exception.ReglaNegocioException;

class NuevaImagenTest {

    private static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 0, 0};
    private static final byte[] JPG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0, 0};

    @Test
    void elTipoSaleDelContenidoNoDelNombre() {
        NuevaImagen png = NuevaImagen.de("boceto.jpg", PNG);
        assertThat(png.tipoMime()).isEqualTo("image/png");
        assertThat(png.extension()).isEqualTo("png");
        assertThat(NuevaImagen.de("foto.jpeg", JPG).tipoMime()).isEqualTo("image/jpeg");
    }

    @Test
    void soloImagenesDeHastaCincoMegas() {
        assertThatThrownBy(() -> NuevaImagen.de("pagina.html", "<html>".getBytes(StandardCharsets.UTF_8)))
                .isInstanceOf(ReglaNegocioException.class).hasMessageContaining("PNG, JPG");
        assertThatThrownBy(() -> NuevaImagen.de("vacia.png", new byte[0])).hasMessageContaining("Elige");
        byte[] grande = new byte[(int) NuevaImagen.TAMANO_MAXIMO + 1];
        System.arraycopy(PNG, 0, grande, 0, PNG.length);
        assertThatThrownBy(() -> NuevaImagen.de("grande.png", grande)).hasMessageContaining("5 MB");
    }

    @Test
    void elNombreNoTraeCarpetas() {
        assertThat(NuevaImagen.de("C:\\Users\\ana\\captura.png", PNG).nombre()).isEqualTo("captura.png");
        assertThat(NuevaImagen.de("../../etc/passwd", PNG).nombre()).isEqualTo("passwd");
        assertThat(NuevaImagen.de(null, PNG).nombre()).isEqualTo("imagen.png");
    }
}
