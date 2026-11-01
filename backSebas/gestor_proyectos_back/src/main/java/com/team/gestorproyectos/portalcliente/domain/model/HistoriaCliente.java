package com.team.gestorproyectos.portalcliente.domain.model;

/** Una historia de usuario, con lo que al cliente le importa: qué es y en qué va. */
public record HistoriaCliente(String titulo, String como, String quiero, EstadoHistoria estado) {

    /** "Como cliente, quiero descargar mis facturas desde el celular." */
    public String descripcion() {
        if (como == null || quiero == null) {
            return null;
        }
        return "Como " + como + ", quiero " + quiero + ".";
    }

    public EstadoHistoriaCliente estadoCliente() {
        return estado.paraCliente();
    }
}
