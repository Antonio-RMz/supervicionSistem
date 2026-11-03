package com.team.gestorproyectos.ticket.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.team.gestorproyectos.shared.domain.exception.ReglaNegocioException;

class ReporteFallaTest {

    private static ReporteFalla reporte(String severidad, String descripcion) {
        return new ReporteFalla(" Error 500 al descargar facturas ", 1L, severidad, descripcion, null);
    }

    @Test
    void criticaYAltaVanPorHotfixMediaYBajaConLaSiguienteFase() {
        assertThat(reporte("critica", "Sale 500").vaPorHotfix()).isTrue();
        assertThat(reporte("ALTA", "Sale 500").vaPorHotfix()).isTrue();
        assertThat(reporte("MEDIA", "Sale 500").vaPorHotfix()).isFalse();
        assertThat(reporte("BAJA", "Sale 500").vaPorHotfix()).isFalse();
    }

    @Test
    void pideSeveridad() {
        assertThatThrownBy(() -> reporte(null, "Sale 500")).isInstanceOf(ReglaNegocioException.class);
        assertThatThrownBy(() -> reporte("URGENTE", "Sale 500")).isInstanceOf(ReglaNegocioException.class);
        assertThat(reporte("ALTA", " Sale 500 ").titulo()).isEqualTo("Error 500 al descargar facturas");
        assertThat(reporte("ALTA", " Sale 500 ").descripcion()).isEqualTo("Sale 500");
    }

    @Test
    void laDescripcionEsOpcionalPorqueBastaLaImagen() {
        assertThat(reporte("ALTA", "  ").descripcion()).isNull();
        assertThat(reporte("ALTA", null).descripcion()).isNull();
    }
}
