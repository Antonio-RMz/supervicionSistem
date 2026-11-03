package com.team.gestorproyectos.notificacion.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.team.gestorproyectos.notificacion.infrastructure.persistence.NotificacionesJpaRepository.TicketFila;
import com.team.gestorproyectos.shared.domain.port.AvisosPort.Papel;
import com.team.gestorproyectos.shared.domain.port.AvisosPort.Tipo;

class NotificacionesAdapterTest {

    private static TicketFila ticket(String tipo, int numero, String titulo, String severidad) {
        return new TicketFila() {
            public Long getProyectoId() { return 1L; }
            public String getTipo() { return tipo; }
            public Integer getNumero() { return numero; }
            public String getTitulo() { return titulo; }
            public String getSeveridad() { return severidad; }
        };
    }

    @Test
    void cadaTipoDiceQuienHizoQueYEnQueTicket() {
        TicketFila bug = ticket("BUGS", 12, "Error al guardar", "CRITICA");
        assertThat(NotificacionesAdapter.texto(Tipo.ASIGNACION, "Ana López", bug))
                .startsWith("Ana López te asignó ").endsWith(" · Error al guardar");
        assertThat(NotificacionesAdapter.texto(Tipo.BUG_REPORTADO, "Ana López", bug))
                .startsWith("Ana López reportó ").endsWith("Error al guardar (Crítica)");
        assertThat(NotificacionesAdapter.texto(Tipo.COMENTARIO, "Ana López", bug))
                .startsWith("Ana López comentó en ");
    }

    @Test
    void unTituloMuyLargoSeCorta() {
        String texto = NotificacionesAdapter.texto(Tipo.COMENTARIO, "Ana", ticket("MEJR", 3, "x".repeat(400), null));
        assertThat(texto).hasSize(300).endsWith("…");
    }

    @Test
    void laHistoriaDiceComoTeToca() {
        assertThat(NotificacionesAdapter.textoHistoria(Papel.RESPONSABLE, "Ana López", 4, "Dashboard"))
                .isEqualTo("Ana López te asignó HU-0004 como responsable · Dashboard");
        assertThat(NotificacionesAdapter.textoHistoria(Papel.DESARROLLADOR, "Ana López", 4, "Dashboard"))
                .isEqualTo("Ana López te asignó HU-0004 para desarrollar · Dashboard");
    }
}
