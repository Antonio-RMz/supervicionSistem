package com.team.gestorproyectos.usuario.application;

import java.util.List;

import com.team.gestorproyectos.shared.application.ServicioAplicacion;
import com.team.gestorproyectos.shared.application.Transaccional;
import com.team.gestorproyectos.usuario.domain.model.Permiso;
import com.team.gestorproyectos.usuario.domain.model.Rol;
import com.team.gestorproyectos.usuario.domain.port.in.ConsultarCatalogoAccesosUseCase;
import com.team.gestorproyectos.usuario.domain.port.out.CatalogoAccesosPort;

import lombok.RequiredArgsConstructor;

@ServicioAplicacion
@RequiredArgsConstructor
public class ConsultarCatalogoAccesosService implements ConsultarCatalogoAccesosUseCase {

    private final CatalogoAccesosPort catalogo;

    @Override
    @Transaccional(soloLectura = true)
    public List<Rol> roles() {
        return catalogo.roles();
    }

    @Override
    @Transaccional(soloLectura = true)
    public List<Permiso> permisos() {
        return catalogo.permisos();
    }
}
