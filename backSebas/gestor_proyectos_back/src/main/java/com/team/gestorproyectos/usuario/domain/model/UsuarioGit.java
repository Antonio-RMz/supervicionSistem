package com.team.gestorproyectos.usuario.domain.model;

import com.team.gestorproyectos.shared.domain.exception.ReglaNegocioException;

/** El usuario de una persona en su plataforma de git (GitHub, GitLab, Bitbucket...). Opcional. */
public final class UsuarioGit {

    public static final int LARGO_MAXIMO = 100;

    private UsuarioGit() {
    }

    /**
     * Sin espacios ni la arroba del inicio ("@ana-dev" queda "ana-dev").
     *
     * @return null si viene vacío
     */
    public static String normalizar(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        String limpio = valor.trim();
        if (limpio.startsWith("@")) {
            limpio = limpio.substring(1).trim();
        }
        if (limpio.isEmpty()) {
            return null;
        }
        if (limpio.chars().anyMatch(Character::isWhitespace)) {
            throw new ReglaNegocioException("El usuario de git no lleva espacios");
        }
        if (limpio.length() > LARGO_MAXIMO) {
            throw new ReglaNegocioException("El usuario de git es muy largo (máximo " + LARGO_MAXIMO + ")");
        }
        return limpio;
    }
}
