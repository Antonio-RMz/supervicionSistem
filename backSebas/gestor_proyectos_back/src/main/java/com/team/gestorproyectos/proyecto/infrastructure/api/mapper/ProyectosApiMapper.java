package com.team.gestorproyectos.proyecto.infrastructure.api.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.team.gestorproyectos.proyecto.domain.model.ConfiguracionProyecto;
import com.team.gestorproyectos.proyecto.domain.model.DatosProyecto;
import com.team.gestorproyectos.proyecto.domain.model.FaseArbol;
import com.team.gestorproyectos.proyecto.domain.model.ListadoProyectos;
import com.team.gestorproyectos.proyecto.domain.model.ModuloConCi;
import com.team.gestorproyectos.proyecto.domain.model.ResumenProyecto;
import com.team.gestorproyectos.proyecto.infrastructure.api.dto.ProyectosDtos.ConfiguracionResponse;
import com.team.gestorproyectos.proyecto.infrastructure.api.dto.ProyectosDtos.FaseArbolResponse;
import com.team.gestorproyectos.proyecto.infrastructure.api.dto.ProyectosDtos.ListadoResponse;
import com.team.gestorproyectos.proyecto.infrastructure.api.dto.ProyectosDtos.ModuloResponse;
import com.team.gestorproyectos.proyecto.infrastructure.api.dto.ProyectosDtos.ProyectoRequest;
import com.team.gestorproyectos.proyecto.infrastructure.api.dto.ProyectosDtos.ResumenResponse;

/** DTO <-> modelo de dominio de Proyectos. Los enums viajan como texto. */
@Mapper(componentModel = "spring")
public interface ProyectosApiMapper {

    ListadoResponse aListado(ListadoProyectos listado);

    ResumenResponse aResumen(ResumenProyecto resumen);

    @Mapping(target = "id", source = "modulo.id")
    @Mapping(target = "tipo", source = "modulo.tipo")
    @Mapping(target = "nombre", source = "modulo.nombre")
    @Mapping(target = "tecnologia", source = "modulo.tecnologia")
    @Mapping(target = "repositorio", source = "modulo.repositorio")
    @Mapping(target = "ramaBase", source = "modulo.ramaBase")
    ModuloResponse aModulo(ModuloConCi modulo);

    List<FaseArbolResponse> aArbol(List<FaseArbol> arbol);

    DatosProyecto aDatos(ProyectoRequest request);

    @Mapping(target = "nombre", source = "datos.nombre")
    @Mapping(target = "clienteId", source = "datos.clienteId")
    @Mapping(target = "prioridad", source = "datos.prioridad")
    @Mapping(target = "estado", source = "datos.estado")
    @Mapping(target = "fechaInicio", source = "datos.fechaInicio")
    @Mapping(target = "fechaEntrega", source = "datos.fechaEntrega")
    @Mapping(target = "descripcion", source = "datos.descripcion")
    @Mapping(target = "liderId", source = "datos.liderId")
    @Mapping(target = "miembroIds", source = "datos.miembroIds")
    @Mapping(target = "modulos", source = "datos.modulos")
    @Mapping(target = "reglas", source = "datos.reglas")
    ConfiguracionResponse aConfiguracion(ConfiguracionProyecto configuracion);
}
