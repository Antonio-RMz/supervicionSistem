package com.team.gestorproyectos.usuario.domain.port.in;

/** Lo usan otros módulos para saber si alguien puede hacer algo, ej. "cliente.gestionar". */
public interface VerificarPermisoUseCase {

    /** Permisos que no dependen de un proyecto: roles + permisos extra en "Todos los proyectos". */
    boolean tienePermiso(Long usuarioId, String permisoCodigo);
}
