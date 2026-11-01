package com.team.gestorproyectos.shared.infrastructure.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/** Crea y lee los JWT. Es la única clase que conoce la librería jjwt. */
@Component
public class JwtService {

    private final SecretKey clave;
    private final long expiracionMs;

    public JwtService(@Value("${jwt.secret}") String secreto,
                      @Value("${jwt.expiracion}") long expiracionMs) {
        this.clave = Keys.hmacShaKeyFor(secreto.getBytes(StandardCharsets.UTF_8));
        this.expiracionMs = expiracionMs;
    }

    public String generar(UsuarioSesion sesion) {
        Date ahora = new Date();
        return Jwts.builder()
                .subject(sesion.correo())
                .claim("id", sesion.id())
                .claim("tipo", sesion.tipo())
                .claim("clienteId", sesion.clienteId())
                .claim("roles", sesion.roles())
                .issuedAt(ahora)
                .expiration(new Date(ahora.getTime() + expiracionMs))
                .signWith(clave)
                .compact();
    }

    /** Vacío si el token está mal formado, la firma no coincide o ya expiró. */
    public Optional<UsuarioSesion> leer(String token) {
        try {
            Claims claims = Jwts.parser().verifyWith(clave).build().parseSignedClaims(token).getPayload();
            List<?> roles = claims.get("roles", List.class);
            return Optional.of(new UsuarioSesion(
                    claims.get("id", Long.class),
                    claims.getSubject(),
                    claims.get("tipo", String.class),
                    claims.get("clienteId", Long.class),
                    roles == null ? List.of() : roles.stream().map(String::valueOf).toList()));
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
