package com.team.gestorproyectos.proyecto.application;

import java.util.List;

import com.team.gestorproyectos.proyecto.domain.model.ProyectoResumen;
import com.team.gestorproyectos.proyecto.domain.port.in.ListarProyectosDelUsuarioUseCase;
import com.team.gestorproyectos.proyecto.domain.port.out.ProyectoRepositoryPort;
import com.team.gestorproyectos.shared.application.ServicioAplicacion;
import com.team.gestorproyectos.shared.application.Transaccional;

import lombok.RequiredArgsConstructor;

@ServicioAplicacion
@RequiredArgsConstructor
public class ListarProyectosDelUsuarioService implements ListarProyectosDelUsuarioUseCase {

    private final ProyectoRepositoryPort proyectos;

    @Override
    @Transaccional(soloLectura = true)
    public List<ProyectoResumen> listar(Long usuarioId, boolean esAdmin) {
        return esAdmin ? proyectos.todos() : proyectos.dondeEsMiembro(usuarioId);
    }
}
