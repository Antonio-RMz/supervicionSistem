package com.team.gestorproyectos.shared.infrastructure.web;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import com.team.gestorproyectos.shared.domain.exception.AccesoDenegadoException;
import com.team.gestorproyectos.shared.domain.exception.NoAutorizadoException;
import com.team.gestorproyectos.shared.domain.exception.NoEncontradoException;
import com.team.gestorproyectos.shared.domain.exception.ReglaNegocioException;

/** Traduce las excepciones del dominio a respuestas HTTP. */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NoEncontradoException.class)
    ResponseEntity<ErrorResponse> noEncontrado(NoEncontradoException e) {
        return responder(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(NoAutorizadoException.class)
    ResponseEntity<ErrorResponse> noAutorizado(NoAutorizadoException e) {
        return responder(HttpStatus.UNAUTHORIZED, e.getMessage());
    }

    @ExceptionHandler(AccesoDenegadoException.class)
    ResponseEntity<ErrorResponse> accesoDenegado(AccesoDenegadoException e) {
        return responder(HttpStatus.FORBIDDEN, e.getMessage());
    }

    @ExceptionHandler(ReglaNegocioException.class)
    ResponseEntity<ErrorResponse> reglaNegocio(ReglaNegocioException e) {
        return responder(HttpStatus.CONFLICT, e.getMessage());
    }

    /** Una llave foránea o un UNIQUE de la base rechazó el cambio (ej. un proyecto que no existe). */
    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<ErrorResponse> integridad(DataIntegrityViolationException e) {
        return responder(HttpStatus.CONFLICT, "Los datos no son consistentes con lo que hay guardado");
    }

    /** El cuerpo no es JSON válido o un campo trae un tipo que no corresponde (ej. texto donde va un número). */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ErrorResponse> noLegible(HttpMessageNotReadableException e) {
        return responder(HttpStatus.BAD_REQUEST, "No pudimos leer los datos enviados");
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    ResponseEntity<ErrorResponse> muyGrande(MaxUploadSizeExceededException e) {
        return responder(HttpStatus.PAYLOAD_TOO_LARGE, "El archivo pesa más de 5 MB");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ErrorResponse> datosInvalidos(MethodArgumentNotValidException e) {
        String mensaje = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst()
                .orElse("Datos inválidos");
        return responder(HttpStatus.BAD_REQUEST, mensaje);
    }

    private ResponseEntity<ErrorResponse> responder(HttpStatus status, String mensaje) {
        return ResponseEntity.status(status).body(ErrorResponse.de(status.value(), mensaje));
    }
}
