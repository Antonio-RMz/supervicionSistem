package com.team.gestorproyectos.fase.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.team.gestorproyectos.shared.domain.exception.ReglaNegocioException;

class NuevaFaseTest {

    private static final LocalDate JUN = LocalDate.of(2026, 6, 1);

    private static NuevaFase fase(LocalDate fin, String version, Set<Long> modulos) {
        return new NuevaFase(" Portal móvil ", " ", JUN, fin, 1L, version, "alta", modulos,
                List.of("Visto bueno del cliente en la demo", " "));
    }

    @Test
    void sugiereLaSiguienteVersion() {
        assertThat(VersionRelease.siguiente(List.of("v1.0.0", "v1.2.0", "v1.1.0"))).isEqualTo("v1.3.0");
        assertThat(VersionRelease.siguiente(java.util.Arrays.asList(null, "beta"))).isEqualTo("v1.0.0");
        assertThat(VersionRelease.siguiente(List.of("v2.0.0", "v1.9.0"))).isEqualTo("v2.1.0");
    }

    @Test
    void limpiaYValidaLoCapturado() {
        NuevaFase nueva = fase(JUN.plusWeeks(8), "v1.3.0", Set.of(1L));
        assertThat(nueva.nombre()).isEqualTo("Portal móvil");
        assertThat(nueva.objetivo()).isNull();
        assertThat(nueva.prioridad()).isEqualTo("ALTA");
        assertThat(nueva.criteriosManuales()).containsExactly("Visto bueno del cliente en la demo");

        assertThatThrownBy(() -> fase(JUN.minusDays(1), null, Set.of(1L))).isInstanceOf(ReglaNegocioException.class);
        assertThatThrownBy(() -> fase(JUN, "1.3", Set.of(1L))).isInstanceOf(ReglaNegocioException.class);
        assertThatThrownBy(() -> fase(JUN, null, Set.of())).isInstanceOf(ReglaNegocioException.class);
    }

    @Test
    void numeraYSugiereElInicioDespuesDeLaUltimaFase() {
        List<FaseExistente> fases = List.of(
                new FaseExistente(0, "Mantenimiento", "EN_CURSO", null, null, true),
                new FaseExistente(1, "Análisis", "PUBLICADA", LocalDate.of(2026, 2, 21), "v1.0.0", false),
                new FaseExistente(2, "Desarrollo", "EN_CURSO", LocalDate.of(2026, 4, 25), "v1.1.0", false));

        assertThat(FaseExistente.siguienteNumero(fases)).isEqualTo(3);
        assertThat(FaseExistente.inicioSugerido(fases, JUN)).isEqualTo(LocalDate.of(2026, 4, 26));
        assertThat(fases).filteredOn(FaseExistente::activa).extracting(FaseExistente::numero).containsExactly(2);
    }
}
