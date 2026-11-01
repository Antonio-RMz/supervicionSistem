package com.team.gestorproyectos.usuario.domain.exception;

import com.team.gestorproyectos.shared.domain.exception.NoAutorizadoException;

public class CredencialesInvalidasException extends NoAutorizadoException {

    public CredencialesInvalidasException() {
        super("Correo o contraseña incorrectos");
    }
}
