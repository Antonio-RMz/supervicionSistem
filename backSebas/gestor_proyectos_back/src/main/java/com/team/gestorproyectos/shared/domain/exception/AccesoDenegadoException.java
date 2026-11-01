package com.team.gestorproyectos.shared.domain.exception;

/** Sabemos quién es, pero no tiene permiso para hacer eso. Se responde 403. */
public class AccesoDenegadoException extends RuntimeException {

    public AccesoDenegadoException(String mensaje) {
        super(mensaje);
    }
}
