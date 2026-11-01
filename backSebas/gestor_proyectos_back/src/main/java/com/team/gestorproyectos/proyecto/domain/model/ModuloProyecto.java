package com.team.gestorproyectos.proyecto.domain.model;

/**
 * @param id        null si es nuevo
 * @param ramaBase  de donde salen las ramas de los tickets; "develop" si no se dice
 */
public record ModuloProyecto(Long id, TipoModulo tipo, String nombre, String tecnologia, String repositorio,
                             String ramaBase) {

    public ModuloProyecto {
        nombre = nombre == null ? "" : nombre.trim();
        tecnologia = vacioANull(tecnologia);
        repositorio = vacioANull(repositorio);
        ramaBase = vacioANull(ramaBase) == null ? "develop" : ramaBase.trim();
    }

    private static String vacioANull(String texto) {
        return texto == null || texto.isBlank() ? null : texto.trim();
    }
}
