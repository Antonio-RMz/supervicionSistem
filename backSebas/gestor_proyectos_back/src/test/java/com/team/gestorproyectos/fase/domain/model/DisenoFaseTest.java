package com.team.gestorproyectos.fase.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.team.gestorproyectos.shared.domain.exception.ReglaNegocioException;

class DisenoFaseTest {

    @Test
    void completaElProtocoloYQuitaEspacios() {
        assertThat(new DisenoFase("  figma.com/design/Xy12/Sistema  ", true).figmaUrl())
                .isEqualTo("https://figma.com/design/Xy12/Sistema");
    }

    @Test
    void vacioEsSinEnlace() {
        assertThat(new DisenoFase(" ", false).figmaUrl()).isNull();
    }

    @Test
    void rechazaLoQueNoEsUnaDireccion() {
        assertThatThrownBy(() -> new DisenoFase("no es un enlace", true))
                .isInstanceOf(ReglaNegocioException.class);
    }
}
