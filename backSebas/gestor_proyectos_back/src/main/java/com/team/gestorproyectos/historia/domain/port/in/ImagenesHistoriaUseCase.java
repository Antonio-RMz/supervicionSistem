package com.team.gestorproyectos.historia.domain.port.in;

import com.team.gestorproyectos.historia.domain.model.NuevaImagen;

/**
 * Las imágenes de la historia (capturas, bocetos). Las ve todo el equipo del proyecto; las suben y
 * quitan su Analista, el Líder o un Admin mientras la historia no se integre ni se descarte.
 */
public interface ImagenesHistoriaUseCase {

    void subir(Long proyectoId, Long historiaId, Long usuarioId, boolean esAdmin, NuevaImagen imagen);

    Imagen ver(Long proyectoId, Long historiaId, Long imagenId, Long usuarioId, boolean esAdmin);

    void quitar(Long proyectoId, Long historiaId, Long imagenId, Long usuarioId, boolean esAdmin);

    record Imagen(String nombre, String tipoMime, byte[] contenido) {
    }
}
