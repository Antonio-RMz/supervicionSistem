package com.team.gestorproyectos.fase.domain.model;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.team.gestorproyectos.shared.domain.exception.ReglaNegocioException;

class FaseGuardadaTest {

    private static final LocalDate JUN = LocalDate.of(2026, 6, 1);
    private static final Map<Long, String> MODULOS = Map.of(1L, "portal-api", 2L, "portal-web");
    private static final List<FaseExistente> OTRAS = List.of(
            new FaseExistente(3, "Pruebas", "PUBLICADA", JUN, "v1.2.0", false),
            new FaseExistente(4, "Portal móvil", "PLANEADA", JUN.plusDays(30), "v1.3.0", false));

    private static final FaseGuardada FASE = new FaseGuardada(1004L, 4, "PLANEADA", "Portal móvil", null, JUN,
            JUN.plusDays(30), 1L, "v1.3.0", "MEDIA", Set.of(1L, 2L), List.of(), Set.of(1L));

    private static NuevaFase cambios(String version, Set<Long> modulos) {
        return new NuevaFase("Portal móvil y pagos", null, JUN, JUN.plusDays(40), 1L, version, "alta", modulos,
                List.of());
    }

    @Test
    void noSeQuitaUnModuloQueYaSeTrabaja() {
        assertThatThrownBy(() -> FASE.validarCambios(cambios("v1.3.0", Set.of(2L)), OTRAS, MODULOS))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("portal-api");
        assertThatCode(() -> FASE.validarCambios(cambios("v1.3.0", Set.of(1L)), OTRAS, MODULOS))
                .doesNotThrowAnyException();
    }

    @Test
    void laVersionNoSeRepiteConOtraFase() {
        assertThatThrownBy(() -> FASE.validarCambios(cambios("v1.2.0", Set.of(1L, 2L)), OTRAS, MODULOS))
                .hasMessageContaining("ya es de otra fase");
    }
}
