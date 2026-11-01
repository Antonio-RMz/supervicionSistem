package com.team.gestorproyectos.usuario.domain.port.in;

import com.team.gestorproyectos.usuario.domain.model.SesionIniciada;

/** Puerto de entrada: lo que se puede hacer. Lo llama el controller, lo implementa el servicio. */
public interface IniciarSesionUseCase {

    SesionIniciada iniciarSesion(String correo, String password);
}
