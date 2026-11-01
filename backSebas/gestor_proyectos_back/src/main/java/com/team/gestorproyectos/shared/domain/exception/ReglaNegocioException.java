package com.team.gestorproyectos.shared.domain.exception;

/** La petición choca con una regla del negocio (correo repetido, quitarse su propio rol...). Se responde 409. */
public class ReglaNegocioException extends RuntimeException {

    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
