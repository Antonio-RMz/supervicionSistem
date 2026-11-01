package com.team.gestorproyectos.proyecto.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.team.gestorproyectos.shared.domain.exception.ReglaNegocioException;

class UsuariosDelSistemaTest {

    @Test
    void limpiaEspaciosYLaDescripcionVaciaQuedaSinDescripcion() {
        UsuarioDelSistema alumno = new UsuarioDelSistema("  Alumno ", "  ");
        assertThat(alumno.nombre()).isEqualTo("Alumno");
        assertThat(alumno.descripcion()).isNull();
    }

    @Test
    void elNombreEsObligatorio() {
        assertThatThrownBy(() -> new UsuarioDelSistema(" ", "Algo")).isInstanceOf(ReglaNegocioException.class);
    }

    @Test
    void noSeRepitenSinImportarMayusculas() {
        assertThatThrownBy(() -> UsuariosDelSistema.validar(List.of(
                new UsuarioDelSistema("Escuela", null), new UsuarioDelSistema("escuela", null))))
                .hasMessage("\"escuela\" está repetido");
        assertThat(UsuariosDelSistema.validar(null)).isEmpty();
    }
}
