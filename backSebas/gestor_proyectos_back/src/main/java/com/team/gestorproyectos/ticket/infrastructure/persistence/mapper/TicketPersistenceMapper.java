package com.team.gestorproyectos.ticket.infrastructure.persistence.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import com.team.gestorproyectos.shared.domain.model.ItemDeProyecto;
import com.team.gestorproyectos.shared.domain.model.ItemTrabajo;
import com.team.gestorproyectos.shared.domain.model.Persona;
import com.team.gestorproyectos.ticket.domain.model.FaseTablero;
import com.team.gestorproyectos.ticket.domain.model.FilaTicket;
import com.team.gestorproyectos.ticket.infrastructure.persistence.repository.TicketConsultaJpaRepository.FaseFila;
import com.team.gestorproyectos.ticket.infrastructure.persistence.repository.TicketConsultaJpaRepository.FilaTicketFila;
import com.team.gestorproyectos.ticket.infrastructure.persistence.repository.TicketConsultaJpaRepository.PersonaFila;

@Mapper(componentModel = "spring")
public interface TicketPersistenceMapper {

    ItemTrabajo aItem(FilaTicketFila fila);

    default FilaTicket aFila(FilaTicketFila fila) {
        return new FilaTicket(
                new ItemDeProyecto(aItem(fila), fila.getLiderId(), fila.getUltimoEstadoAnterior()),
                fila.getFaseId(), fila.getFaseNumero(), fila.getHistoriaId(), fila.getHistoriaNumero(),
                fila.getResponsableIniciales());
    }

    default List<FilaTicket> aFilas(List<FilaTicketFila> filas) {
        return filas.stream().map(this::aFila).toList();
    }

    List<Persona> aPersonas(List<PersonaFila> filas);

    List<FaseTablero> aFases(List<FaseFila> filas);
}
