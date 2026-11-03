package com.team.gestorproyectos.proyecto.domain.port.in;

import java.util.Optional;

import com.team.gestorproyectos.historia.domain.model.NuevaImagen;

/**
 * El ícono del proyecto: lo ve cualquiera que vea el proyecto; lo cambian o quitan quienes pueden editarlo
 * (su Líder o un Administrador). Sin ícono, la app muestra las iniciales del proyecto.
 */
public interface IconoProyectoUseCase {

    long TAMANO_MAXIMO = 1024L * 1024;

    void subir(Long proyectoId, Long usuarioId, boolean esAdmin, NuevaImagen imagen);

    /** Vacío si el proyecto no tiene ícono. */
    Optional<Icono> ver(Long proyectoId, Long usuarioId, boolean esAdmin);

    void quitar(Long proyectoId, Long usuarioId, boolean esAdmin);

    record Icono(String tipoMime, byte[] contenido) {
    }
}
