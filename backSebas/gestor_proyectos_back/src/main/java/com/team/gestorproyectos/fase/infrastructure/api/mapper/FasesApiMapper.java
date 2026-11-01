package com.team.gestorproyectos.fase.infrastructure.api.mapper;

import org.mapstruct.Mapper;

import com.team.gestorproyectos.fase.domain.model.EdicionFase;
import com.team.gestorproyectos.fase.domain.model.NuevaFase;
import com.team.gestorproyectos.fase.domain.model.PreparacionFase;
import com.team.gestorproyectos.fase.infrastructure.api.dto.FasesDtos.EdicionResponse;
import com.team.gestorproyectos.fase.infrastructure.api.dto.FasesDtos.NuevaFaseRequest;
import com.team.gestorproyectos.fase.infrastructure.api.dto.FasesDtos.PreparacionResponse;

@Mapper(componentModel = "spring")
public interface FasesApiMapper {

    PreparacionResponse aPreparacion(PreparacionFase preparacion);

    NuevaFase aNuevaFase(NuevaFaseRequest request);

    EdicionResponse aEdicion(EdicionFase edicion);
}
