package com.team.gestorproyectos.usuario.domain.port.in;

import com.team.gestorproyectos.usuario.domain.model.PerfilUsuario;

public interface ConsultarPerfilUsuarioUseCase {

    PerfilUsuario consultar(Long usuarioId);
}
