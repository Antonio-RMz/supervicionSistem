package com.team.gestorproyectos.proyecto.application;

import java.util.List;

import com.team.gestorproyectos.proyecto.domain.model.UsuarioDelSistema;
import com.team.gestorproyectos.proyecto.domain.model.UsuariosDelSistema;
import com.team.gestorproyectos.proyecto.domain.port.in.UsuariosDelSistemaUseCase;
import com.team.gestorproyectos.proyecto.domain.port.out.EquipoProyectoPort;
import com.team.gestorproyectos.proyecto.domain.port.out.EquipoProyectoPort.ProyectoDelEquipo;
import com.team.gestorproyectos.proyecto.domain.port.out.ProyectoConsultaPort;
import com.team.gestorproyectos.proyecto.domain.port.out.UsuariosDelSistemaPort;
import com.team.gestorproyectos.shared.application.ServicioAplicacion;
import com.team.gestorproyectos.shared.application.Transaccional;
import com.team.gestorproyectos.shared.domain.exception.AccesoDenegadoException;
import com.team.gestorproyectos.shared.domain.exception.NoEncontradoException;
import com.team.gestorproyectos.shared.domain.port.PermisosPort;

import lombok.RequiredArgsConstructor;

@ServicioAplicacion
@RequiredArgsConstructor
public class UsuariosDelSistemaService implements UsuariosDelSistemaUseCase {

    private final UsuariosDelSistemaPort usuarios;
    private final EquipoProyectoPort equipos;
    private final ProyectoConsultaPort consulta;
    private final PermisosPort permisos;

    @Override
    @Transaccional(soloLectura = true)
    public UsuariosDelSistema consultar(Long proyectoId, Long usuarioId, boolean esAdmin) {
        ProyectoDelEquipo proyecto = verificar(proyectoId, usuarioId, esAdmin);
        return new UsuariosDelSistema(usuarios.usuarios(proyectoId), puedeEditar(proyecto, usuarioId, esAdmin));
    }

    @Override
    @Transaccional
    public void guardar(Long proyectoId, Long usuarioId, boolean esAdmin, List<UsuarioDelSistema> lista) {
        if (!puedeEditar(verificar(proyectoId, usuarioId, esAdmin), usuarioId, esAdmin)) {
            throw new AccesoDenegadoException("Los usuarios del sistema los editan el Líder, un Admin o quien crea historias");
        }
        usuarios.reemplazar(proyectoId, UsuariosDelSistema.validar(lista));
    }

    private ProyectoDelEquipo verificar(Long proyectoId, Long usuarioId, boolean esAdmin) {
        if (!consulta.visible(proyectoId, usuarioId, esAdmin)) {
            throw new NoEncontradoException("No encontramos ese proyecto");
        }
        return equipos.proyecto(proyectoId).orElseThrow(() -> new NoEncontradoException("No encontramos ese proyecto"));
    }

    private boolean puedeEditar(ProyectoDelEquipo proyecto, Long usuarioId, boolean esAdmin) {
        return esAdmin || usuarioId.equals(proyecto.liderId()) || permisos.puede(usuarioId, PermisosPort.CREAR_HISTORIAS);
    }
}
