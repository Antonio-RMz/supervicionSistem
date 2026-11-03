package com.team.gestorproyectos.usuario.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.team.gestorproyectos.shared.domain.exception.ReglaNegocioException;

class UsuarioGitTest {

    @Test
    void quitaEspaciosDeLosLadosYLaArrobaDelInicio() {
        assertThat(UsuarioGit.normalizar("  @ana-dev ")).isEqualTo("ana-dev");
        assertThat(UsuarioGit.normalizar("jorge.sebastian")).isEqualTo("jorge.sebastian");
    }

    @Test
    void vacioEsSinUsuario() {
        assertThat(UsuarioGit.normalizar(null)).isNull();
        assertThat(UsuarioGit.normalizar("   ")).isNull();
        assertThat(UsuarioGit.normalizar("@")).isNull();
    }

    @Test
    void noLlevaEspaciosNiEsMuyLargo() {
        assertThatThrownBy(() -> UsuarioGit.normalizar("ana dev")).isInstanceOf(ReglaNegocioException.class);
        assertThatThrownBy(() -> UsuarioGit.normalizar("a".repeat(101))).isInstanceOf(ReglaNegocioException.class);
    }
}
