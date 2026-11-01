package com.team.gestorproyectos.usuario.infrastructure.persistence;

import org.springframework.stereotype.Component;

import com.team.gestorproyectos.usuario.domain.model.PermisoEfectivo;
import com.team.gestorproyectos.usuario.domain.port.out.PermisoConsultaPort;
import com.team.gestorproyectos.usuario.infrastructure.persistence.repository.UsuarioPermisoJpaRepository;
import com.team.gestorproyectos.usuario.infrastructure.persistence.repository.UsuarioPermisoJpaRepository.PermisoGlobalFila;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PermisoConsultaAdapter implements PermisoConsultaPort {

    private final UsuarioPermisoJpaRepository repository;

    @Override
    public PermisoEfectivo permisoGlobal(Long usuarioId, String permisoCodigo) {
        PermisoGlobalFila fila = repository.permisoGlobal(usuarioId, permisoCodigo);
        Boolean ajuste = fila.getAjuste() == null ? null : fila.getAjuste() == 1;
        return new PermisoEfectivo(fila.getPorRol() != null && fila.getPorRol() == 1, ajuste);
    }
}
