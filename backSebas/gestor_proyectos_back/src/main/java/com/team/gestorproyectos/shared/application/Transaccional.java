package com.team.gestorproyectos.shared.application;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * El método (o todos los de la clase) corre en una sola transacción: si lanza una excepción
 * no comprobada, se deshace todo. La infraestructura decide cómo se abre y se cierra.
 */
@Documented
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface Transaccional {

    /** Solo consulta: permite a la base de datos optimizar y no escribir nada. */
    boolean soloLectura() default false;
}
