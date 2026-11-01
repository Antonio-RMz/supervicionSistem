package com.team.gestorproyectos.usuario.domain.port.out;

public interface PasswordPort {

    boolean coincide(String passwordPlano, String passwordHash);

    String cifrar(String passwordPlano);
}
