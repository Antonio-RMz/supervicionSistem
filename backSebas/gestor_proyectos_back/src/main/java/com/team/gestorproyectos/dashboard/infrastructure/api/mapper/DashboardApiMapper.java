package com.team.gestorproyectos.dashboard.infrastructure.api.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.team.gestorproyectos.dashboard.domain.model.Dashboard;
import com.team.gestorproyectos.dashboard.domain.model.FaseDashboard;
import com.team.gestorproyectos.shared.domain.model.AccionPendiente;
import com.team.gestorproyectos.shared.domain.model.FlujoTrabajo;
import com.team.gestorproyectos.shared.domain.model.ItemPendiente;
import com.team.gestorproyectos.shared.domain.model.Movimiento;
import com.team.gestorproyectos.dashboard.infrastructure.api.dto.DashboardDtos.DashboardResponse;
import com.team.gestorproyectos.dashboard.infrastructure.api.dto.DashboardDtos.FaseResponse;
import com.team.gestorproyectos.dashboard.infrastructure.api.dto.DashboardDtos.ItemPendienteResponse;
import com.team.gestorproyectos.dashboard.infrastructure.api.dto.DashboardDtos.MovimientoResponse;

@Mapper(componentModel = "spring")
public interface DashboardApiMapper {

    @Mapping(target = "proyectoId", source = "proyecto.id")
    @Mapping(target = "proyectoNombre", source = "proyecto.nombre")
    DashboardResponse aResponse(Dashboard dashboard);

    @Mapping(target = "id", source = "item.id")
    @Mapping(target = "tipo", source = "item.tipo")
    @Mapping(target = "codigo", expression = "java(pendiente.item().codigo())")
    @Mapping(target = "titulo", source = "item.titulo")
    @Mapping(target = "estado", source = "item.estado")
    @Mapping(target = "actorNombre", source = "actor.nombre")
    @Mapping(target = "actorRol", source = "rolActor")
    @Mapping(target = "ejecutable", expression = "java(ejecutable(pendiente))")
    ItemPendienteResponse aItem(ItemPendiente pendiente);

    default boolean ejecutable(ItemPendiente pendiente) {
        return pendiente.item().esTicket()
                && FlujoTrabajo.accionPendiente(pendiente.item()).map(AccionPendiente::ejecutable).orElse(false);
    }

    @Mapping(target = "avance", source = "porcentaje")
    FaseResponse aFase(FaseDashboard fase);

    @Mapping(target = "descripcion", expression = "java(movimiento.descripcion())")
    MovimientoResponse aMovimiento(Movimiento movimiento);
}
