package com.team.gestorproyectos.usuario.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PermisoEfectivoTest {

    @Test
    void sinAjusteMandanLosRoles() {
        assertThat(new PermisoEfectivo(true, null).concedido()).isTrue();
        assertThat(new PermisoEfectivo(false, null).concedido()).isFalse();
    }

    @Test
    void elAjustePuntualGanaSobreLosRoles() {
        assertThat(new PermisoEfectivo(true, false).concedido()).isFalse();
        assertThat(new PermisoEfectivo(false, true).concedido()).isTrue();
    }
}
