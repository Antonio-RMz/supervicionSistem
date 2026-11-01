package com.team.gestorproyectos.shared.infrastructure.web;

import java.time.LocalDateTime;

/** Forma única de responder errores al front. */
public record ErrorResponse(int status, String mensaje, LocalDateTime fecha) {

    public static ErrorResponse de(int status, String mensaje) {
        return new ErrorResponse(status, mensaje, LocalDateTime.now());
    }
}
