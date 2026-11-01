package com.team.gestorproyectos.usuario.application;

import com.team.gestorproyectos.shared.application.ServicioAplicacion;
import com.team.gestorproyectos.shared.application.Transaccional;
import com.team.gestorproyectos.usuario.domain.port.in.VerificarPermisoUseCase;
import com.team.gestorproyectos.usuario.domain.port.out.PermisoConsultaPort;

import lombok.RequiredArgsConstructor;

@ServicioAplicacion
@RequiredArgsConstructor
public class VerificarPermisoService implements VerificarPermisoUseCase {

    private final PermisoConsultaPort permisos;

    @Override
    @Transaccional(soloLectura = true)
    public boolean tienePermiso(Long usuarioId, String permisoCodigo) {
        return permisos.permisoGlobal(usuarioId, permisoCodigo).concedido();
    }
}
