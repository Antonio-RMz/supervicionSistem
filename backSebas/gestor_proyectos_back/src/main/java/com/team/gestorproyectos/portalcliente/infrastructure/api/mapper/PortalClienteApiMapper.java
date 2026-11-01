package com.team.gestorproyectos.portalcliente.infrastructure.api.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.team.gestorproyectos.portalcliente.domain.model.AvanceFase;
import com.team.gestorproyectos.portalcliente.domain.model.AvanceModulo;
import com.team.gestorproyectos.portalcliente.domain.model.AvanceProyecto;
import com.team.gestorproyectos.portalcliente.domain.model.HistoriaCliente;
import com.team.gestorproyectos.portalcliente.domain.model.ProyectoCliente;
import com.team.gestorproyectos.portalcliente.infrastructure.api.dto.PortalClienteDtos.AvanceResponse;
import com.team.gestorproyectos.portalcliente.infrastructure.api.dto.PortalClienteDtos.FaseResponse;
import com.team.gestorproyectos.portalcliente.infrastructure.api.dto.PortalClienteDtos.HistoriaResponse;
import com.team.gestorproyectos.portalcliente.infrastructure.api.dto.PortalClienteDtos.MiProyectoResponse;
import com.team.gestorproyectos.portalcliente.infrastructure.api.dto.PortalClienteDtos.ModuloAvanceResponse;

/** Modelos del dominio -> DTOs de respuesta. Los valores calculados se piden al dominio con expression. */
@Mapper(componentModel = "spring")
public interface PortalClienteApiMapper {

    @Mapping(target = "avance", expression = "java(proyecto.avance())")
    @Mapping(target = "faseActual", expression = "java(aFase(proyecto.faseActual().orElse(null)))")
    MiProyectoResponse aMiProyecto(ProyectoCliente proyecto);

    List<MiProyectoResponse> aMisProyectos(List<ProyectoCliente> proyectos);

    @Mapping(target = "id", source = "proyecto.id")
    @Mapping(target = "codigo", source = "proyecto.codigo")
    @Mapping(target = "nombre", source = "proyecto.nombre")
    @Mapping(target = "estado", source = "proyecto.estado")
    @Mapping(target = "avance", expression = "java(datos.proyecto().avance())")
    @Mapping(target = "modulos", source = "proyecto.modulos")
    @Mapping(target = "fases", source = "proyecto.fases")
    @Mapping(target = "historias", source = "historiasFaseActual")
    AvanceResponse aAvance(AvanceProyecto datos);

    @Mapping(target = "id", source = "fase.id")
    @Mapping(target = "numero", source = "fase.numero")
    @Mapping(target = "nombre", source = "fase.nombre")
    @Mapping(target = "objetivo", source = "fase.objetivo")
    @Mapping(target = "estado", expression = "java(avanceFase.estadoCliente().name())")
    @Mapping(target = "fechaInicio", source = "fase.fechaInicio")
    @Mapping(target = "fechaFin", source = "fase.fechaFin")
    @Mapping(target = "version", source = "fase.version")
    @Mapping(target = "avance", source = "porcentaje")
    FaseResponse aFase(AvanceFase avanceFase);

    @Mapping(target = "nombre", source = "modulo.nombre")
    @Mapping(target = "tipo", source = "modulo.tipo")
    @Mapping(target = "avance", source = "porcentaje")
    ModuloAvanceResponse aModuloAvance(AvanceModulo avanceModulo);

    @Mapping(target = "descripcion", expression = "java(historia.descripcion())")
    @Mapping(target = "estado", expression = "java(historia.estadoCliente().name())")
    HistoriaResponse aHistoria(HistoriaCliente historia);
}
