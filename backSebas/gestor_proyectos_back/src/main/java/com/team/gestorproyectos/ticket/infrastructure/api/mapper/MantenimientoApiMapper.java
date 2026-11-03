package com.team.gestorproyectos.ticket.infrastructure.api.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.team.gestorproyectos.shared.domain.model.CodigoItem;
import com.team.gestorproyectos.ticket.domain.model.Mantenimiento;
import com.team.gestorproyectos.ticket.domain.model.Mantenimiento.TicketDeMantenimiento;
import com.team.gestorproyectos.ticket.domain.model.NuevoTicketMantenimiento;
import com.team.gestorproyectos.ticket.domain.model.PreparacionMantenimiento;
import com.team.gestorproyectos.ticket.domain.model.PreparacionMantenimiento.HistoriaDeOrigen;
import com.team.gestorproyectos.ticket.domain.model.ReporteFalla;
import com.team.gestorproyectos.ticket.infrastructure.api.dto.MantenimientoDtos.HistoriaResponse;
import com.team.gestorproyectos.ticket.infrastructure.api.dto.MantenimientoDtos.MantenimientoResponse;
import com.team.gestorproyectos.ticket.infrastructure.api.dto.MantenimientoDtos.NuevoTicketRequest;
import com.team.gestorproyectos.ticket.infrastructure.api.dto.MantenimientoDtos.PreparacionResponse;
import com.team.gestorproyectos.ticket.infrastructure.api.dto.MantenimientoDtos.ReporteFallaRequest;
import com.team.gestorproyectos.ticket.infrastructure.api.dto.MantenimientoDtos.TicketResponse;

@Mapper(componentModel = "spring")
public interface MantenimientoApiMapper {

    @Mapping(target = "urgentesConfirmados", expression = "java(mantenimiento.urgentesConfirmados())")
    @Mapping(target = "criticos", expression = "java(mantenimiento.criticos())")
    MantenimientoResponse aMantenimiento(Mantenimiento mantenimiento);

    /** El código y si espera severidad salen de las reglas del dominio. */
    default TicketResponse aTicket(TicketDeMantenimiento t) {
        return new TicketResponse(t.id(), t.tipo(), t.codigo(), t.titulo(), t.estado(), t.severidad(), t.rama(),
                t.responsableId(), t.responsableNombre(), t.porConfirmar(), t.descripcion(), t.imagenId(), t.imagenes());
    }

    PreparacionResponse aPreparacion(PreparacionMantenimiento preparacion);

    default HistoriaResponse aHistoria(HistoriaDeOrigen h) {
        return new HistoriaResponse(h.id(), CodigoItem.historia(h.numero()), h.titulo());
    }

    NuevoTicketMantenimiento aNuevoTicket(NuevoTicketRequest request);

    ReporteFalla aReporte(ReporteFallaRequest request);
}
