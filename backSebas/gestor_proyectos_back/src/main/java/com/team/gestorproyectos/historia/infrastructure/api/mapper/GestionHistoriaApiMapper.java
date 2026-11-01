package com.team.gestorproyectos.historia.infrastructure.api.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import com.team.gestorproyectos.historia.domain.model.AccionDeLista;
import com.team.gestorproyectos.historia.domain.model.DatosAccionHistoria;
import com.team.gestorproyectos.historia.domain.model.DetalleHistoria;
import com.team.gestorproyectos.historia.domain.model.HistoriaParaIntegrar.ModuloConTickets;
import com.team.gestorproyectos.historia.domain.model.HistoriaParaIntegrar.PullRequest;
import com.team.gestorproyectos.historia.domain.model.HistoriaParaIntegrar.TicketAIntegrar;
import com.team.gestorproyectos.historia.domain.model.IntegracionHistoria;
import com.team.gestorproyectos.historia.domain.model.ResultadoAccionHistoria;
import com.team.gestorproyectos.historia.infrastructure.api.dto.GestionHistoriaDtos.AccionDeListaResponse;
import com.team.gestorproyectos.historia.infrastructure.api.dto.GestionHistoriaDtos.AccionHistoriaRequest;
import com.team.gestorproyectos.historia.infrastructure.api.dto.GestionHistoriaDtos.DetalleHistoriaResponse;
import com.team.gestorproyectos.historia.infrastructure.api.dto.GestionHistoriaDtos.IntegracionResponse;
import com.team.gestorproyectos.historia.infrastructure.api.dto.GestionHistoriaDtos.ModuloIntegrarResponse;
import com.team.gestorproyectos.historia.infrastructure.api.dto.GestionHistoriaDtos.ResultadoResponse;
import com.team.gestorproyectos.historia.infrastructure.api.dto.GestionHistoriaDtos.TicketIntegrarResponse;

@Mapper(componentModel = "spring")
public interface GestionHistoriaApiMapper {

    DetalleHistoriaResponse aDetalle(DetalleHistoria detalle);

    List<AccionDeListaResponse> aAccionesDeLista(List<AccionDeLista> acciones);

    DatosAccionHistoria aDatos(AccionHistoriaRequest request);

    ResultadoResponse aResultado(ResultadoAccionHistoria resultado);

    IntegracionResponse aIntegracion(IntegracionHistoria integracion);

    default ModuloIntegrarResponse aModulo(ModuloConTickets m) {
        return new ModuloIntegrarResponse(m.modulo().tipo(), m.modulo().nombre(), m.modulo().tecnologia(),
                m.tickets().stream().map(this::aTicket).toList());
    }

    default TicketIntegrarResponse aTicket(TicketAIntegrar t) {
        PullRequest pr = t.pr();
        return new TicketIntegrarResponse(t.id(), t.tipo(), t.codigo(), t.titulo(), t.rama(),
                pr == null ? null : pr.numero(), pr == null ? null : pr.estado(), pr == null ? null : pr.ci(),
                pr != null && pr.conConflictos(), t.enDevelop());
    }
}
