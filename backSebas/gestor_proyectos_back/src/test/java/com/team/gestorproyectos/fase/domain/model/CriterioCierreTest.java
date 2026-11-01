package com.team.gestorproyectos.fase.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import com.team.gestorproyectos.fase.domain.model.CriterioCierre.Conteos;

class CriterioCierreTest {

    private static final Conteos CONTEOS = new Conteos(2, 2, 0);

    @Test
    void elDespliegueEnStagingLoMarcaElLider() {
        CriterioCierre pendiente = CriterioCierre.evaluar(3L, "Repositorios involucrados desplegados en staging",
                "MODULOS_EN_STAGING", false, null, null, CONTEOS);
        assertThat(pendiente.manual()).isTrue();
        assertThat(pendiente.cumplido()).isFalse();

        LocalDateTime cuando = LocalDateTime.of(2026, 10, 25, 12, 0);
        CriterioCierre marcado = CriterioCierre.evaluar(3L, "Repositorios involucrados desplegados en staging",
                "MODULOS_EN_STAGING", true, "Lider", cuando, CONTEOS);
        assertThat(marcado.cumplido()).isTrue();
        assertThat(marcado.cumplidoPor()).isEqualTo("Lider");
        assertThat(marcado.cumplidoEn()).isEqualTo(cuando);
    }

    @Test
    void lasHistoriasAceptadasSiguenSiendoAutomaticas() {
        assertThat(CriterioCierre.evaluar(1L, "Todas las historias aceptadas", "HISTORIAS_ACEPTADAS", false, null,
                null, CONTEOS).manual()).isFalse();
    }
}
