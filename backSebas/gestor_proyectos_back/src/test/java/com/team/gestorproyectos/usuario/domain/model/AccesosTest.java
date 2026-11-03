package com.team.gestorproyectos.usuario.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.team.gestorproyectos.shared.domain.exception.ReglaNegocioException;

class AccesosTest {

    private static PermisoExtra extra(String codigo, Long proyectoId) {
        return new PermisoExtra(codigo, null, proyectoId, null, true);
    }

    @Test
    void necesitaAlMenosUnRol() {
        assertThatThrownBy(() -> new Accesos(Set.of(), List.of()))
                .isInstanceOf(ReglaNegocioException.class);
    }

    @Test
    void elMismoPermisoPuedeAjustarseEnProyectosDistintos() {
        var accesos = new Accesos(Set.of("ANALISTA"), List.of(
                extra("ticket.cerrar", 1L), extra("ticket.cerrar", 2L), extra("ticket.cerrar", null)));

        assertThat(accesos.permisosExtra()).hasSize(3);
    }

    @Test
    void noSeRepiteUnPermisoEnElMismoAlcance() {
        assertThatThrownBy(() -> new Accesos(Set.of("ANALISTA"), List.of(
                extra("ticket.crear", null), extra("ticket.crear", null))))
                .isInstanceOf(ReglaNegocioException.class);
    }

    @Test
    void nuevoUsuarioNormalizaElCorreoYPideContrasenaLarga() {
        var nuevo = new NuevoUsuario(" Ana ", " ", " Ana@Equipo.MX ", "Temporal1", true, true, Set.of("ANALISTA"), null, null);
        assertThat(nuevo.correo()).isEqualTo("ana@equipo.mx");
        assertThat(nuevo.nombre()).isEqualTo("Ana");
        assertThat(nuevo.apellidos()).isNull();
        assertThat(nuevo.proyectoIds()).isEmpty();

        assertThatThrownBy(() -> new NuevoUsuario("Ana", null, "ana@equipo.mx", "corta", true, true, Set.of("ANALISTA"), Set.of(), null))
                .isInstanceOf(ReglaNegocioException.class);
    }
}
