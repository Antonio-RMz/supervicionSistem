package com.team.gestorproyectos.portalcliente.domain.model;

import java.util.List;

/**
 * Todo lo que muestra la pantalla "Avance" del portal.
 *
 * @param faseActual   puede ser null si todas las fases ya se entregaron
 * @param contacto     puede ser null si el proyecto aún no tiene Analista
 * @param demoPendiente puede ser null si no hay demos por revisar
 */
public record AvanceProyecto(
        ProyectoCliente proyecto,
        AvanceFase faseActual,
        List<AvanceModulo> modulosFaseActual,
        List<HistoriaCliente> historiasFaseActual,
        List<VersionEntregada> versiones,
        Contacto contacto,
        DemoPendiente demoPendiente) {
}
