package com.team.gestorproyectos.shared.infrastructure.security;

import java.io.IOException;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/** Lee el header "Authorization: Bearer ..." y, si el token es válido, deja al usuario en sesión. */
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final String PREFIJO = "Bearer ";

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith(PREFIJO)) {
            jwtService.leer(header.substring(PREFIJO.length())).ifPresent(sesion -> {
                var autenticacion = new UsernamePasswordAuthenticationToken(sesion, null, sesion.authorities());
                SecurityContextHolder.getContext().setAuthentication(autenticacion);
            });
        }
        chain.doFilter(request, response);
    }
}
