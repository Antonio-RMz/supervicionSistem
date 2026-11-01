package com.team.gestorproyectos.fase.domain.model;

import java.util.List;

import com.team.gestorproyectos.fase.domain.model.FaseParaGestionar.BugReversion;

/**
 * El detalle de la fase (PDF pág. 32) con el panel de Acciones de quien la ve (PDF pág. 20).
 *
 * @param avance           porcentaje, igual que en el árbol del proyecto
 * @param rol              "Líder" o "Admin" si ve botones; null si solo la consulta
 * @param acciones         sus botones en el estado actual, con lo que les falta para habilitarse
 * @param puedeEliminar    la puede borrar con sus historias: Planeada, nada iniciado, y es el Líder o un Admin
 * @param historiasEliminables las historias que puede borrar desde la lista: aún no se inician y es el Líder o un Admin
 */
public record DetalleFase(
        InfoFase info,
        int avance,
        long ticketsTerminados,
        long ticketsTotal,
        List<GrupoTickets.Conteo> ticketsPorEstado,
        List<CriterioCierre> criterios,
        String versionAnterior,
        BugReversion bugReversion,
        String rol,
        boolean esResponsable,
        List<AccionDisponible> acciones,
        boolean puedeMarcarCriterios,
        boolean puedeCrearHistoria,
        boolean puedeEditar,
        boolean puedeEliminar,
        List<Long> historiasEliminables) {

    public record AccionDisponible(AccionFase accion, String texto, List<String> impedimentos) {
    }
}
