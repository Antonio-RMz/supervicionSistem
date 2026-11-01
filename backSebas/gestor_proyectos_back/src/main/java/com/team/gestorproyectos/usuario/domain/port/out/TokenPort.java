package com.team.gestorproyectos.usuario.domain.port.out;

import com.team.gestorproyectos.usuario.domain.model.Usuario;

public interface TokenPort {

    String generar(Usuario usuario);
}
