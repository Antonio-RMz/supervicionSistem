package com.team.gestorproyectos.fase.infrastructure.api.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.team.gestorproyectos.fase.domain.model.AccionDeLista;
import com.team.gestorproyectos.fase.domain.model.DatosAccionFase;
import com.team.gestorproyectos.fase.domain.model.DetalleFase;
import com.team.gestorproyectos.fase.domain.model.ReleaseFase;
import com.team.gestorproyectos.fase.domain.model.ResultadoAccionFase;
import com.team.gestorproyectos.fase.infrastructure.api.dto.GestionFaseDtos.AccionDeListaResponse;
import com.team.gestorproyectos.fase.infrastructure.api.dto.GestionFaseDtos.AccionFaseRequest;
import com.team.gestorproyectos.fase.infrastructure.api.dto.GestionFaseDtos.ActividadResponse;
import com.team.gestorproyectos.fase.infrastructure.api.dto.GestionFaseDtos.DetalleFaseResponse;
import com.team.gestorproyectos.fase.infrastructure.api.dto.GestionFaseDtos.ReleaseFaseResponse;
import com.team.gestorproyectos.fase.infrastructure.api.dto.GestionFaseDtos.ResultadoResponse;
import com.team.gestorproyectos.shared.domain.model.Movimiento;

@Mapper(componentModel = "spring")
public interface GestionFaseApiMapper {

    DetalleFaseResponse aDetalle(DetalleFase detalle);

    ReleaseFaseResponse aRelease(ReleaseFase release);

    List<AccionDeListaResponse> aAccionesDeLista(List<AccionDeLista> acciones);

    DatosAccionFase aDatos(AccionFaseRequest request);

    ResultadoResponse aResultado(ResultadoAccionFase resultado);

    @Mapping(target = "descripcion", expression = "java(movimiento.descripcion())")
    @Mapping(target = "clase", expression = "java(movimiento.clase().name())")
    ActividadResponse aActividad(Movimiento movimiento);
}
