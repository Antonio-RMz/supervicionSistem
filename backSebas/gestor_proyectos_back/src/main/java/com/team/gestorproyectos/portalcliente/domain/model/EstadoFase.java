package com.team.gestorproyectos.portalcliente.domain.model;

/** Estados internos de la fase (los que maneja el equipo). */
public enum EstadoFase {
    PLANEADA,
    EN_CURSO,
    PAUSADA,
    LISTA_PARA_RELEASE,
    EN_PUBLICACION,
    PUBLICADA,
    REVERTIDA,
    CANCELADA;

    /** El cliente no ve el detalle técnico: solo si ya se entregó, va en curso o viene después. */
    public EstadoFaseCliente paraCliente() {
        return switch (this) {
            case PLANEADA -> EstadoFaseCliente.PROXIMA;
            case PAUSADA -> EstadoFaseCliente.EN_PAUSA;
            case PUBLICADA -> EstadoFaseCliente.ENTREGADA;
            case CANCELADA -> EstadoFaseCliente.CANCELADA;
            case EN_CURSO, LISTA_PARA_RELEASE, EN_PUBLICACION, REVERTIDA -> EstadoFaseCliente.EN_CURSO;
        };
    }
}
