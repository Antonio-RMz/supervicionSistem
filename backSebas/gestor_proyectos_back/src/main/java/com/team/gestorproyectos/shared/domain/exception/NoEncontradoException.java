package com.team.gestorproyectos.shared.domain.exception;

/** Lo que se buscó no existe o el usuario no tiene acceso a verlo. Se responde 404. */
public class NoEncontradoException extends RuntimeException {

    public NoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
