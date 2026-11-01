package com.team.gestorproyectos.ticket.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.team.gestorproyectos.shared.domain.exception.ReglaNegocioException;

class NuevoTicketTest {

    private static NuevoTicket ticket(TipoTicket tipo, Set<String> capas, BigDecimal horas) {
        return new NuevoTicket(4L, 16L, tipo, " Servicio de consulta de pedidos ", " ", 1L, capas, null,
                List.of(" Script Flyway ", ""), null, 3L, 2L, "alta", horas, null);
    }

    @Test
    void cadaTipoNaceEnSuPrimerEstado() {
        // La Actividad nace en Borrador; Lista (Por hacer) sigue sin empezar.
        assertThat(TipoTicket.ACTV.estadoInicial()).isEqualTo("BORRADOR");
        assertThat(TipoTicket.ACTV.sinEmpezar("POR_HACER")).isTrue();
        assertThat(TipoTicket.ACTV.sinEmpezar("EN_PROGRESO")).isFalse();
        assertThat(TipoTicket.BUGS.estadoInicial()).isEqualTo("REPORTADO");
        assertThat(TipoTicket.MEJR.estadoInicial()).isEqualTo("PROPUESTA");
    }

    @Test
    void limpiaLoCapturadoYGuardaLasCapasEnOrden() {
        NuevoTicket t = ticket(TipoTicket.ACTV, Set.of("Pruebas", "Base de datos", "Servicio"), new BigDecimal("6"));
        assertThat(t.titulo()).isEqualTo("Servicio de consulta de pedidos");
        assertThat(t.descripcion()).isNull();
        assertThat(t.subtareas()).containsExactly("Script Flyway");
        assertThat(t.capasComoTexto()).isEqualTo("Base de datos, Servicio, Pruebas");
        assertThat(ticket(TipoTicket.MEJR, null, null).capasComoTexto()).isNull();
    }

    @Test
    void rechazaCapasDesconocidasYHorasInvalidas() {
        assertThatThrownBy(() -> ticket(TipoTicket.ACTV, Set.of("Magia"), null)).isInstanceOf(ReglaNegocioException.class);
        assertThatThrownBy(() -> ticket(TipoTicket.ACTV, null, BigDecimal.ZERO)).isInstanceOf(ReglaNegocioException.class);
    }
}
