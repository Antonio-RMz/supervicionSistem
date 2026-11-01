package com.team.gestorproyectos.shared.infrastructure.security;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/**
 * Quién hace la petición. Se arma a partir del JWT y llega a los controllers con
 * {@code @AuthenticationPrincipal UsuarioSesion sesion}.
 *
 * @param tipo      EQUIPO o CLIENTE
 * @param clienteId solo para usuarios CLIENTE: la empresa cuyos proyectos puede ver
 * @param roles     códigos de rol (ADMIN, LIDER...). Vacío para clientes.
 */
public record UsuarioSesion(Long id, String correo, String tipo, Long clienteId, List<String> roles) {

    public boolean esAdmin() {
        return roles.contains("ADMIN");
    }

    public List<GrantedAuthority> authorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + tipo));
        roles.forEach(rol -> authorities.add(new SimpleGrantedAuthority("ROLE_" + rol)));
        return authorities;
    }
}
