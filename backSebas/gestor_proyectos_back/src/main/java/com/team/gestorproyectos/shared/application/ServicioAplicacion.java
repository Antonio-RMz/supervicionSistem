package com.team.gestorproyectos.shared.application;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marca una clase de la capa de aplicación (caso de uso o ayudante) que la infraestructura
 * debe registrar e inyectar. Es Java puro para que la aplicación no dependa de Spring.
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ServicioAplicacion {
}
