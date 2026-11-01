package com.team.gestorproyectos.ticket.domain.model;

/** Los tres tipos de ticket de trabajo y el estado con que nace cada uno (su flujo vive en FlujoTrabajo). */
public enum TipoTicket {
    /** Funcionalidad nueva: nace en Borrador y el Líder la marca Lista (Por hacer) cuando termina de definirla */
    ACTV("BORRADOR"),
    /** Algo que no funciona: el Líder lo confirma antes de corregirlo */
    BUGS("REPORTADO"),
    /** Cambio a algo existente: el Líder la aprueba antes de hacerla */
    MEJR("PROPUESTA");

    private final String estadoInicial;

    TipoTicket(String estadoInicial) {
        this.estadoInicial = estadoInicial;
    }

    public String estadoInicial() {
        return estadoInicial;
    }

    /** Aún nadie lo empieza: en su estado inicial o, una Actividad, ya Lista (Por hacer). */
    public boolean sinEmpezar(String estado) {
        return estadoInicial.equals(estado) || (this == ACTV && "POR_HACER".equals(estado));
    }
}
