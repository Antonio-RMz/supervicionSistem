package com.team.gestorproyectos.usuario.application;

import java.util.List;

import com.team.gestorproyectos.shared.application.ServicioAplicacion;
import com.team.gestorproyectos.shared.application.Transaccional;
import com.team.gestorproyectos.usuario.domain.model.UsuarioConProyectos;
import com.team.gestorproyectos.usuario.domain.port.in.ListarUsuariosUseCase;
import com.team.gestorproyectos.usuario.domain.port.out.UsuarioRepositoryPort;

import lombok.RequiredArgsConstructor;

@ServicioAplicacion
@RequiredArgsConstructor
public class ListarUsuariosService implements ListarUsuariosUseCase {

    private final UsuarioRepositoryPort usuarios;

    @Override
    @Transaccional(soloLectura = true)
    public List<UsuarioConProyectos> listar() {
        return usuarios.listarEquipo();
    }
}
