package com.team.gestorproyectos.usuario.infrastructure.security;

import org.springframework.stereotype.Component;

import com.team.gestorproyectos.shared.domain.port.PermisosPort;
import com.team.gestorproyectos.usuario.domain.port.in.VerificarPermisoUseCase;

import lombok.RequiredArgsConstructor;

/** Los demás módulos preguntan aquí por permisos; se resuelven con roles + permisos extra. */
@Component
@RequiredArgsConstructor
public class PermisosAdapter implements PermisosPort {

    private final VerificarPermisoUseCase verificarPermiso;

    @Override
    public boolean puede(Long usuarioId, String permisoCodigo) {
        return verificarPermiso.tienePermiso(usuarioId, permisoCodigo);
    }
}
