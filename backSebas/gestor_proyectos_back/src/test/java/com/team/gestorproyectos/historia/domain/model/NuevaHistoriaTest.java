package com.team.gestorproyectos.historia.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.team.gestorproyectos.shared.domain.exception.ReglaNegocioException;

class NuevaHistoriaTest {

    private static NuevaHistoria historia(boolean borrador, String como, List<String> criterios, Set<Long> modulos,
                                          String figma, boolean aprobado, Integer puntos) {
        return new NuevaHistoria(borrador, 4L, " Consultar mis pedidos ", como, "ver mis pedidos",
                "saber cuándo llegan", criterios, figma, aprobado, 2L, "alta",
                puntos, modulos);
    }

    @Test
    void unBorradorSoloPideTituloFaseYAnalista() {
        NuevaHistoria borrador = historia(true, null, null, null, null, false, null);
        assertThat(borrador.titulo()).isEqualTo("Consultar mis pedidos");
        assertThat(borrador.criterios()).isEmpty();
        assertThat(borrador.prioridad()).isEqualTo("ALTA");
    }

    @Test
    void crearLaHistoriaPideLaHistoriaCompleta() {
        assertThatThrownBy(() -> historia(false, null, List.of("Veo mis pedidos"), Set.of(1L), null, false, 5))
                .isInstanceOf(ReglaNegocioException.class).hasMessageContaining("Como");
        assertThatThrownBy(() -> historia(false, "cliente", List.of(" "), Set.of(1L), null, false, 5))
                .isInstanceOf(ReglaNegocioException.class).hasMessageContaining("criterio");
        assertThatThrownBy(() -> historia(false, "cliente", List.of("Veo mis pedidos"), Set.of(), null, false, 5))
                .isInstanceOf(ReglaNegocioException.class).hasMessageContaining("repositorio");
        assertThat(historia(false, "cliente", List.of("Veo mis pedidos"), Set.of(1L), null, false, 5).puntos())
                .isEqualTo(5);
    }

    @Test
    void validaPuntosYDiseno() {
        assertThatThrownBy(() -> historia(true, null, null, null, null, false, 4))
                .isInstanceOf(ReglaNegocioException.class);
        assertThatThrownBy(() -> historia(true, null, null, null, null, true, null))
                .isInstanceOf(ReglaNegocioException.class).hasMessageContaining("Figma");
        assertThat(historia(true, null, null, null, "figma.com/design/Ab12", true, null).figmaUrl())
                .isEqualTo("https://figma.com/design/Ab12");
    }
}
