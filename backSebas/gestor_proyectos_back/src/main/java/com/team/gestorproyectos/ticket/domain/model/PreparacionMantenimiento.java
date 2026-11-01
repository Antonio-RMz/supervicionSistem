package com.team.gestorproyectos.ticket.domain.model;

import java.util.List;
import java.util.Map;

import com.team.gestorproyectos.ticket.domain.model.PreparacionTicket.ModuloDeTicket;
import com.team.gestorproyectos.ticket.domain.model.PreparacionTicket.PersonaDeEquipo;

/**
 * Lo que necesitan los modales de Mantenimiento: Nuevo ticket (PDF pág. 38) y Reportar falla (pág. 39).
 *
 * @param modulos           todos los del proyecto: Mantenimiento no se limita a los de una fase
 * @param proximaFaseNumero con qué fase sale lo normal; null si no hay
 * @param siguientes        el número que tendría un ticket de cada tipo
 * @param historias         para "Viene de"
 * @param puedeCrear        ticket.crear: el Líder o un Desarrollador
 * @param puedeReportar     bug.reportar: cualquiera del equipo
 */
public record PreparacionMantenimiento(Long proyectoId, Long faseId, Long liderId, String enProduccion,
                                       Integer proximaFaseNumero, List<ModuloDeTicket> modulos,
                                       List<PersonaDeEquipo> equipo, Map<TipoTicket, Integer> siguientes,
                                       List<HistoriaDeOrigen> historias, boolean puedeCrear, boolean puedeReportar) {

    public record HistoriaDeOrigen(Long id, int numero, String titulo) {
    }
}
