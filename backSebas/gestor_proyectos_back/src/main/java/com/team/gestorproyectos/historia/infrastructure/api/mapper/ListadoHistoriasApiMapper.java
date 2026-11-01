package com.team.gestorproyectos.historia.infrastructure.api.mapper;

import org.mapstruct.Mapper;

import com.team.gestorproyectos.historia.domain.model.HistoriasDeFase;
import com.team.gestorproyectos.historia.domain.model.ListadoHistorias;
import com.team.gestorproyectos.historia.domain.model.ListadoHistorias.HistoriaDeLista;
import com.team.gestorproyectos.historia.infrastructure.api.dto.ListadoHistoriasDtos.HistoriaCompletaResponse;
import com.team.gestorproyectos.historia.infrastructure.api.dto.ListadoHistoriasDtos.HistoriaDeListaResponse;
import com.team.gestorproyectos.historia.infrastructure.api.dto.ListadoHistoriasDtos.HistoriasDeFaseResponse;
import com.team.gestorproyectos.historia.infrastructure.api.dto.ListadoHistoriasDtos.ListadoHistoriasResponse;

@Mapper(componentModel = "spring")
public interface ListadoHistoriasApiMapper {

    ListadoHistoriasResponse aListado(ListadoHistorias listado);

    HistoriasDeFaseResponse aHistoriasDeFase(HistoriasDeFase historias);

    default HistoriaCompletaResponse aHistoriaCompleta(HistoriasDeFase.Historia h) {
        return new HistoriaCompletaResponse(h.codigo(), h.estado(), h.titulo(), h.como(), h.quiero(), h.para(),
                h.prioridad(), h.puntos(), h.figmaUrl(), h.criterios());
    }

    /** El código ("HU-0016") sale de la regla del dominio, no de una columna. */
    default HistoriaDeListaResponse aHistoria(HistoriaDeLista h) {
        return new HistoriaDeListaResponse(h.id(), h.codigo(), h.titulo(), h.estado(), h.prioridad(), h.puntos(),
                h.faseId(), h.faseNumero(), h.faseNombre(), h.analistaNombre(), h.ticketsTerminados(), h.ticketsTotal(),
                h.actualizadoEn());
    }
}
