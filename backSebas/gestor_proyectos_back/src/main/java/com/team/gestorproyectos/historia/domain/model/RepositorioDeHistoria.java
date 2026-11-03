package com.team.gestorproyectos.historia.domain.model;

/**
 * Un repositorio en que se trabaja la historia, con su rama base (para los pasos de git).
 *
 * @param url su dirección (https://github.com/...); null si el proyecto no la tiene
 */
public record RepositorioDeHistoria(Long id, String tipo, String nombre, String tecnologia, String ramaBase,
                                    String url) {
}
