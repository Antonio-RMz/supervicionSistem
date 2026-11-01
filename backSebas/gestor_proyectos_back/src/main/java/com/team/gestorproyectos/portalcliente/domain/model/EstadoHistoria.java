package com.team.gestorproyectos.portalcliente.domain.model;

/** Estados internos de la historia de usuario. */
public enum EstadoHistoria {
    BORRADOR,
    LISTA,
    EN_DESARROLLO,
    EN_QA,
    EN_VALIDACION,
    ACEPTADA,
    INTEGRADA,
    CERRADA,
    DESCARTADA;

    public EstadoHistoriaCliente paraCliente() {
        return switch (this) {
            case BORRADOR, LISTA -> EstadoHistoriaCliente.PLANEADO;
            case EN_DESARROLLO -> EstadoHistoriaCliente.EN_DESARROLLO;
            case EN_QA, EN_VALIDACION, ACEPTADA -> EstadoHistoriaCliente.EN_PRUEBAS;
            case INTEGRADA -> EstadoHistoriaCliente.LISTO;
            case CERRADA -> EstadoHistoriaCliente.ENTREGADO;
            case DESCARTADA -> EstadoHistoriaCliente.DESCARTADO;
        };
    }
}
