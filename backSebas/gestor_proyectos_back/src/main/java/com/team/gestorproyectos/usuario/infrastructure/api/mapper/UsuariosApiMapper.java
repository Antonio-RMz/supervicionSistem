package com.team.gestorproyectos.usuario.infrastructure.api.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.team.gestorproyectos.usuario.domain.model.Accesos;
import com.team.gestorproyectos.usuario.domain.model.NuevoUsuario;
import com.team.gestorproyectos.usuario.domain.model.PerfilUsuario;
import com.team.gestorproyectos.usuario.domain.model.Permiso;
import com.team.gestorproyectos.usuario.domain.model.PermisoExtra;
import com.team.gestorproyectos.usuario.domain.model.Rol;
import com.team.gestorproyectos.usuario.domain.model.Tarea;
import com.team.gestorproyectos.usuario.domain.model.UsuarioConProyectos;
import com.team.gestorproyectos.usuario.infrastructure.api.dto.UsuariosDtos.AccesosRequest;
import com.team.gestorproyectos.usuario.infrastructure.api.dto.UsuariosDtos.AccesosResponse;
import com.team.gestorproyectos.usuario.infrastructure.api.dto.UsuariosDtos.MetricasPerfilResponse;
import com.team.gestorproyectos.usuario.infrastructure.api.dto.UsuariosDtos.NuevoUsuarioRequest;
import com.team.gestorproyectos.usuario.infrastructure.api.dto.UsuariosDtos.PerfilResponse;
import com.team.gestorproyectos.usuario.infrastructure.api.dto.UsuariosDtos.PermisoExtraRequest;
import com.team.gestorproyectos.usuario.infrastructure.api.dto.UsuariosDtos.PermisoResponse;
import com.team.gestorproyectos.usuario.infrastructure.api.dto.UsuariosDtos.RolResponse;
import com.team.gestorproyectos.usuario.infrastructure.api.dto.UsuariosDtos.TareaResponse;
import com.team.gestorproyectos.usuario.infrastructure.api.dto.UsuariosDtos.UsuarioFilaResponse;

/** DTO <-> modelo de dominio de Usuarios y roles. */
@Mapper(componentModel = "spring")
public interface UsuariosApiMapper {

    @Mapping(target = "id", source = "usuario.id")
    @Mapping(target = "nombreCompleto", expression = "java(fila.usuario().nombreCompleto())")
    @Mapping(target = "iniciales", expression = "java(fila.usuario().iniciales())")
    @Mapping(target = "correo", source = "usuario.correo")
    @Mapping(target = "roles", source = "usuario.roles")
    @Mapping(target = "activo", source = "usuario.activo")
    UsuarioFilaResponse aFila(UsuarioConProyectos fila);

    List<UsuarioFilaResponse> aFilas(List<UsuarioConProyectos> filas);

    NuevoUsuario aNuevoUsuario(NuevoUsuarioRequest request);

    AccesosResponse aAccesosResponse(Accesos accesos);

    Accesos aAccesos(AccesosRequest request);

    @Mapping(target = "permisoNombre", ignore = true)
    @Mapping(target = "proyectoNombre", ignore = true)
    PermisoExtra aPermisoExtra(PermisoExtraRequest request);

    List<RolResponse> aRoles(List<Rol> roles);

    List<PermisoResponse> aPermisos(List<Permiso> permisos);

    @Mapping(target = "id", source = "usuario.id")
    @Mapping(target = "nombreCompleto", expression = "java(perfil.usuario().nombreCompleto())")
    @Mapping(target = "iniciales", expression = "java(perfil.usuario().iniciales())")
    @Mapping(target = "correo", source = "usuario.correo")
    @Mapping(target = "roles", source = "usuario.roles")
    @Mapping(target = "activo", source = "usuario.activo")
    @Mapping(target = "ultimoAcceso", source = "usuario.ultimoAcceso")
    @Mapping(target = "metricas", expression = "java(metricas(perfil))")
    @Mapping(target = "trabajandoAhora", source = "trabajo.trabajandoAhora")
    @Mapping(target = "leToca", source = "trabajo.leToca")
    @Mapping(target = "esperandoAOtros", source = "trabajo.esperandoAOtros")
    PerfilResponse aPerfil(PerfilUsuario perfil);

    default MetricasPerfilResponse metricas(PerfilUsuario perfil) {
        return new MetricasPerfilResponse(
                perfil.trabajo().trabajandoAhora().size(),
                perfil.trabajo().leToca().size(),
                perfil.trabajo().esperandoAOtros().size(),
                perfil.cerradosEsteMes(),
                perfil.rechazadosEnQaEsteMes());
    }

    @Mapping(target = "id", source = "pendiente.item.id")
    @Mapping(target = "tipo", source = "pendiente.item.tipo")
    @Mapping(target = "codigo", expression = "java(tarea.pendiente().item().codigo())")
    @Mapping(target = "titulo", source = "pendiente.item.titulo")
    @Mapping(target = "estado", source = "pendiente.item.estado")
    @Mapping(target = "accion", source = "pendiente.accion")
    @Mapping(target = "actorNombre", source = "pendiente.actor.nombre")
    @Mapping(target = "actorRol", source = "pendiente.rolActor")
    @Mapping(target = "actualizadoEn", source = "pendiente.item.actualizadoEn")
    TareaResponse aTarea(Tarea tarea);
}
