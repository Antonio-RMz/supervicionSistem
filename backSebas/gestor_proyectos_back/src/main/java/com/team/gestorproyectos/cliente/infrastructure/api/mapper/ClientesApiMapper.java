package com.team.gestorproyectos.cliente.infrastructure.api.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.team.gestorproyectos.cliente.domain.model.ClienteConProyectos;
import com.team.gestorproyectos.cliente.domain.model.DatosCliente;
import com.team.gestorproyectos.cliente.domain.model.ListadoClientes;
import com.team.gestorproyectos.cliente.infrastructure.api.dto.ClientesDtos.ClienteRequest;
import com.team.gestorproyectos.cliente.infrastructure.api.dto.ClientesDtos.ClienteResponse;
import com.team.gestorproyectos.cliente.infrastructure.api.dto.ClientesDtos.ListadoClientesResponse;

@Mapper(componentModel = "spring")
public interface ClientesApiMapper {

    ListadoClientesResponse aListado(ListadoClientes listado);

    @Mapping(target = "id", source = "cliente.id")
    @Mapping(target = "razonSocial", source = "cliente.razonSocial")
    @Mapping(target = "nombreCorto", source = "cliente.nombreCorto")
    @Mapping(target = "iniciales", expression = "java(fila.cliente().iniciales())")
    @Mapping(target = "rfc", source = "cliente.rfc")
    @Mapping(target = "giro", source = "cliente.giro")
    @Mapping(target = "ciudad", source = "cliente.ciudad")
    @Mapping(target = "contactoNombre", source = "cliente.contactoNombre")
    @Mapping(target = "contactoCorreo", source = "cliente.contactoCorreo")
    @Mapping(target = "contactoTelefono", source = "cliente.contactoTelefono")
    @Mapping(target = "notas", source = "cliente.notas")
    @Mapping(target = "activo", source = "cliente.activo")
    ClienteResponse aResponse(ClienteConProyectos fila);

    DatosCliente aDatos(ClienteRequest request);
}
