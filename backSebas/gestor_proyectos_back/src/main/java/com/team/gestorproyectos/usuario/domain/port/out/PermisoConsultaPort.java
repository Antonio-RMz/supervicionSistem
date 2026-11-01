package com.team.gestorproyectos.usuario.domain.port.out;

import com.team.gestorproyectos.usuario.domain.model.PermisoEfectivo;

public interface PermisoConsultaPort {

    PermisoEfectivo permisoGlobal(Long usuarioId, String permisoCodigo);
}
