package com.team.gestorproyectos.shared.domain.exception;

/** El usuario no pudo identificarse (credenciales malas, cuenta inactiva...). Se responde 401. */
public class NoAutorizadoException extends RuntimeException {

    public NoAutorizadoException(String mensaje) {
        super(mensaje);
    }
}
