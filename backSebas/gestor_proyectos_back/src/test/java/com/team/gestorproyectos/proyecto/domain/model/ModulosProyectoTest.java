package com.team.gestorproyectos.proyecto.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.team.gestorproyectos.proyecto.domain.model.ModulosProyecto.Ambiente;
import com.team.gestorproyectos.proyecto.domain.model.ModulosProyecto.DatosModulo;
import com.team.gestorproyectos.proyecto.domain.model.ModulosProyecto.VistaModulo;
import com.team.gestorproyectos.shared.domain.model.ConteoTickets;

class ModulosProyectoTest {

    private static final LocalDateTime HOY = LocalDateTime.of(2026, 7, 10, 12, 0);
    private static final Ambiente PRODUCCION = new Ambiente("producción", "v1.2.0", HOY.minusDays(70));

    private static ConteoTickets conteo(String estado, long cantidad) {
        return new ConteoTickets(4L, 1L, estado, cantidad);
    }

    private static DatosModulo api(int integradas) {
        var modulo = new ModuloProyecto(1L, TipoModulo.API, "portal-api", "Java 21 · Spring Boot 3, MySQL 8",
                "github.com/equipo/portal-api", null);
        return new DatosModulo(new ModuloConCi(modulo, "PASO", HOY.minusHours(2)), null,
                List.of(conteo("POR_HACER", 2), conteo("EN_PROGRESO", 2), conteo("BLOQUEADA", 1), conteo("EN_QA", 2),
                        conteo("APROBADA", 3), conteo("CERRADA", 5)),
                List.of(conteo("EN_CORRECCION", 1), conteo("REPORTADO", 1), conteo("CERRADO", 4)),
                integradas, integradas == 0 ? null : HOY.minusHours(2), true);
    }

    @Test
    void laTarjetaCuentaLosTicketsAbiertosPorEtapaYLosBugsDeProduccion() {
        VistaModulo v = ModulosProyecto.armar(List.of(api(3)), "v1.3.0", PRODUCCION, true).modulos().get(0);

        assertThat(v.tecnologias()).containsExactly("Java 21", "Spring Boot 3", "MySQL 8");
        assertThat(v.modulo().ramaBase()).isEqualTo("develop");
        assertThat(v.porHacer()).isEqualTo(2);
        assertThat(v.enProgreso()).isEqualTo(3); // la bloqueada sigue en trabajo
        assertThat(v.enQa()).isEqualTo(2);
        assertThat(v.bugsProduccion()).isEqualTo(2);
        assertThat(v.ultimoBuild().estado()).isEqualTo("PASO");
    }

    @Test
    void developYStagingLlevanLaCandidataDeLaFaseYProduccionLoPublicado() {
        List<Ambiente> conIntegraciones = ModulosProyecto.armar(List.of(api(3)), "v1.3.0", PRODUCCION, true)
                .modulos().get(0).ambientes();
        assertThat(conIntegraciones).extracting(Ambiente::version).containsExactly("1.3.0-rc3", "1.3.0-rc3", "v1.2.0");

        List<Ambiente> sinIntegraciones = ModulosProyecto.armar(List.of(api(0)), "v1.3.0", null, true)
                .modulos().get(0).ambientes();
        assertThat(sinIntegraciones).extracting(Ambiente::version).containsOnlyNulls();
    }

    @Test
    void sugiereLasPartesDelSistemaQueFaltan() {
        assertThat(ModulosProyecto.armar(List.of(api(0)), null, null, false).faltan())
                .containsExactly(TipoModulo.WEB, TipoModulo.MOVIL, TipoModulo.ESCRITORIO, TipoModulo.BASE_DATOS,
                        TipoModulo.INFRAESTRUCTURA);
    }
}
