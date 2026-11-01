package com.team.gestorproyectos.fase.domain.model;

import java.util.regex.Pattern;

import com.team.gestorproyectos.shared.domain.exception.ReglaNegocioException;

/**
 * Un solo enlace de Figma para toda la fase.
 *
 * @param paraTodas las historias sin diseño propio lo heredan; si una necesita otro, se lo pone en su sección Diseño
 */
public record DisenoFase(String figmaUrl, boolean paraTodas) {

    private static final Pattern URL = Pattern.compile("^https?://\\S+$");

    public DisenoFase {
        figmaUrl = figmaUrl == null || figmaUrl.isBlank() ? null : figmaUrl.trim();
        if (figmaUrl != null && !figmaUrl.startsWith("http")) {
            figmaUrl = "https://" + figmaUrl;
        }
        if (figmaUrl != null && !URL.matcher(figmaUrl).matches()) {
            throw new ReglaNegocioException("El enlace de Figma no parece una dirección web");
        }
        if (figmaUrl != null && figmaUrl.length() > 300) {
            throw new ReglaNegocioException("El enlace de Figma es demasiado largo");
        }
    }
}
