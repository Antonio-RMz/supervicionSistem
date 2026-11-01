package com.team.gestorproyectos.proyecto.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.team.gestorproyectos.proyecto.domain.model.ReleasesProyecto.Publicado;

class ReleasesProyectoTest {

    private static Publicado release(long id, String version, String tipo, String estado, LocalDateTime fecha) {
        return new Publicado(id, version, tipo, estado, null, null, null, fecha, 0, 0, List.of(), "Jorge", false);
    }

    @Test
    void laMasRecienteNoRevertidaEsLaQueEstaEnProduccionYVanDeLaMasNuevaALaMasVieja() {
        LocalDateTime feb = LocalDateTime.of(2026, 2, 21, 18, 0);
        List<Publicado> publicados = ReleasesProyecto.marcarProduccion(List.of(
                release(1, "v1.1.0", "FASE", "PUBLICADA", feb),
                release(3, "v1.2.0", "FASE", "REVERTIDA", feb.plusMonths(2)),
                release(2, "v1.1.1", "PARCHE", "PUBLICADA", feb.plusDays(10))));

        assertThat(publicados).extracting(Publicado::version).containsExactly("v1.2.0", "v1.1.1", "v1.1.0");
        assertThat(publicados).extracting(Publicado::enProduccion).containsExactly(false, true, false);
    }

    @Test
    void sinPublicacionesNadaEstaEnProduccion() {
        assertThat(ReleasesProyecto.marcarProduccion(List.of())).isEmpty();
    }
}
