package com.team.gestorproyectos.historia.infrastructure.api.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import com.team.gestorproyectos.historia.domain.model.EdicionHistoria;
import com.team.gestorproyectos.historia.domain.model.HistoriaImportada;
import com.team.gestorproyectos.historia.domain.model.NuevaHistoria;
import com.team.gestorproyectos.historia.domain.model.PreparacionHistoria;
import com.team.gestorproyectos.historia.domain.model.ResultadoImportacion;
import com.team.gestorproyectos.historia.infrastructure.api.dto.HistoriasDtos.EdicionResponse;
import com.team.gestorproyectos.historia.infrastructure.api.dto.HistoriasDtos.HistoriaImportadaRequest;
import com.team.gestorproyectos.historia.infrastructure.api.dto.HistoriasDtos.NuevaHistoriaRequest;
import com.team.gestorproyectos.historia.infrastructure.api.dto.HistoriasDtos.PreparacionResponse;
import com.team.gestorproyectos.historia.infrastructure.api.dto.HistoriasDtos.ResultadoImportacionResponse;

@Mapper(componentModel = "spring")
public interface HistoriasApiMapper {

    PreparacionResponse aPreparacion(PreparacionHistoria preparacion);

    NuevaHistoria aNuevaHistoria(NuevaHistoriaRequest request);

    EdicionResponse aEdicion(EdicionHistoria edicion);

    List<HistoriaImportada> aImportadas(List<HistoriaImportadaRequest> historias);

    ResultadoImportacionResponse aResultado(ResultadoImportacion resultado);
}
