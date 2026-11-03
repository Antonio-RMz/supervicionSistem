package com.team.gestorproyectos.notificacion.infrastructure.api;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.team.gestorproyectos.notificacion.domain.model.Bandeja;
import com.team.gestorproyectos.notificacion.domain.port.in.NotificacionesUseCase;
import com.team.gestorproyectos.shared.infrastructure.security.UsuarioSesion;

import lombok.RequiredArgsConstructor;

/** La campana de la barra superior. Cada quien ve y marca solo las suyas. */
@RestController
@RequestMapping("/api/equipo/notificaciones")
@RequiredArgsConstructor
public class NotificacionesController {

    private final NotificacionesUseCase notificaciones;

    @GetMapping
    public Bandeja bandeja(@AuthenticationPrincipal UsuarioSesion sesion) {
        return notificaciones.bandeja(sesion.id());
    }

    @PostMapping("/{id}/leida")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void marcarLeida(@PathVariable("id") Long id, @AuthenticationPrincipal UsuarioSesion sesion) {
        notificaciones.marcarLeida(sesion.id(), id);
    }

    @PostMapping("/leidas")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void marcarTodasLeidas(@AuthenticationPrincipal UsuarioSesion sesion) {
        notificaciones.marcarTodasLeidas(sesion.id());
    }
}
