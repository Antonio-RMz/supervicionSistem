package com.team.gestorproyectos.proyecto.domain.model;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Detalle de un proyecto, pestaña Resumen (PDF pág. 14). "Te toca a ti" y "Actividad reciente"
 * los da el Dashboard del mismo proyecto.
 *
 * @param faseActual     null si el proyecto aún no tiene fases
 * @param ticketsListos  de la fase actual: los que ya funcionan, para "8 de 12 tickets listos"
 * @param mantenimientoAbiertos tickets abiertos en la fase de Mantenimiento (hotfix)
 * @param puedeCrearFase        tiene el permiso "fase.crear"
 * @param puedeCrearHistoria    tiene el permiso "historia.crear"
 * @param puedeCrearTicket      tiene el permiso "ticket.crear"
 */
public record ResumenProyecto(
        InfoProyecto info,
        int avance,
        List<ModuloConCi> modulos,
        List<Miembro> equipo,
        List<FaseResumen> fases,
        FaseResumen faseActual,
        long ticketsAbiertos,
        long bugsAbiertos,
        long bugsEnProduccion,
        long mantenimientoAbiertos,
        long ticketsListos,
        long ticketsFaseActual,
        Ritmo ritmo,
        Salud salud,
        List<Detenido> detenidos,
        boolean puedeEditar,
        boolean puedeCrearFase,
        boolean puedeCrearHistoria,
        boolean puedeCrearTicket) {

    /** La fase en curso más adelantada; si ninguna está en curso, la primera por empezar; si no, la última. */
    public static Optional<FaseResumen> faseActual(List<FaseResumen> fases) {
        List<FaseResumen> normales = fases.stream().filter(f -> !f.mantenimiento()).toList();
        return normales.stream()
                .filter(f -> "EN_CURSO".equals(f.estado()))
                .max(Comparator.comparingInt(FaseResumen::numero))
                .or(() -> normales.stream().filter(f -> "PLANEADA".equals(f.estado())).findFirst())
                .or(() -> normales.stream().reduce((a, b) -> b));
    }
}
