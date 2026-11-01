package com.team.gestorproyectos.cliente.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.team.gestorproyectos.shared.domain.exception.ReglaNegocioException;

class DatosClienteTest {

    private static DatosCliente datos(String razonSocial, String nombreCorto, String rfc) {
        return new DatosCliente(razonSocial, nombreCorto, rfc, Giro.COMERCIO, " León, Gto. ",
                "Mariana Torres", " Mariana@GFL.mx ", "", "  ", true);
    }

    @Test
    void sinNombreCortoSeUsaLaRazonSocialSinElTipoDeSociedad() {
        assertThat(datos("Grupo Ferretero León S.A. de C.V.", null, null).nombreCorto()).isEqualTo("Grupo Ferretero León");
        assertThat(datos("Textiles del Centro, S. de R.L. de C.V.", " ", null).nombreCorto()).isEqualTo("Textiles del Centro");
        assertThat(datos("Interno", null, null).nombreCorto()).isEqualTo("Interno");
        assertThat(datos("Grupo Ferretero León S.A. de C.V.", "Ferretero León", null).nombreCorto()).isEqualTo("Ferretero León");
    }

    @Test
    void limpiaLoCapturado() {
        DatosCliente d = datos("Grupo Ferretero León S.A. de C.V.", null, "gfl120315ab1");
        assertThat(d.rfc()).isEqualTo("GFL120315AB1");
        assertThat(d.contactoCorreo()).isEqualTo("mariana@gfl.mx");
        assertThat(d.ciudad()).isEqualTo("León, Gto.");
        assertThat(d.contactoTelefono()).isNull();
        assertThat(d.notas()).isNull();
    }

    @Test
    void rechazaUnRfcMalFormado() {
        assertThatThrownBy(() -> datos("Grupo Ferretero León", null, "GFL-12"))
                .isInstanceOf(ReglaNegocioException.class);
    }

    @Test
    void inicialesDeLaTablaDeClientes() {
        assertThat(cliente("Grupo Ferretero León").iniciales()).isEqualTo("GF");
        assertThat(cliente("Distribuidora Bajío").iniciales()).isEqualTo("DB");
        assertThat(cliente("Interno").iniciales()).isEqualTo("IN");
    }

    private static Cliente cliente(String nombreCorto) {
        return new Cliente(1L, nombreCorto, nombreCorto, null, null, null, "X", "x@x.mx", null, null, true);
    }
}
