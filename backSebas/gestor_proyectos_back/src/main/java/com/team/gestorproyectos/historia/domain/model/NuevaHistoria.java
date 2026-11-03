package com.team.gestorproyectos.historia.domain.model;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

import com.team.gestorproyectos.shared.domain.exception.ReglaNegocioException;

/**
 * Lo que se captura en "Nueva historia de usuario" (PDF pág. 18). Siempre nace como Borrador.
 *
 * @param borrador        "Guardar borrador": basta con título, fase y analista. "Crear historia" pide además
 *                        Como / Quiero / Para, al menos un criterio de aceptación y un módulo.
 * @param disenoAprobado  el cliente ya aprobó el diseño: se registra quién y cuándo
 * @param desarrolladores quiénes la desarrollan (la historia les llega directo, sin Actividades como ticket);
 *                        cada uno, opcionalmente, en un repositorio de la historia
 */
public record NuevaHistoria(
        boolean borrador,
        Long faseId,
        String titulo,
        String como,
        String quiero,
        String para,
        List<String> criterios,
        String figmaUrl,
        boolean disenoAprobado,
        Long analistaId,
        String prioridad,
        Integer puntos,
        Set<Long> moduloIds,
        List<Desarrollador> desarrolladores) {

    /** Un Desarrollador de la historia; moduloId null = en todos sus repositorios. */
    public record Desarrollador(Long usuarioId, Long moduloId) {
    }

    /** Escala de estimación (Fibonacci), como los botones del PDF. */
    public static final Set<Integer> PUNTOS = Set.of(1, 2, 3, 5, 8, 13);

    private static final Set<String> PRIORIDADES = Set.of("BAJA", "MEDIA", "ALTA");
    private static final Pattern URL = Pattern.compile("^https?://\\S+$");

    public NuevaHistoria {
        titulo = limpio(titulo);
        if (titulo == null) {
            throw new ReglaNegocioException("Escribe el título de la historia");
        }
        if (faseId == null) {
            throw new ReglaNegocioException("Elige la fase de la historia");
        }
        if (analistaId == null) {
            throw new ReglaNegocioException("Elige al Analista responsable");
        }
        como = limpio(como);
        quiero = limpio(quiero);
        para = limpio(para);
        criterios = limpios(criterios);
        moduloIds = moduloIds == null ? Set.of() : Set.copyOf(moduloIds);
        desarrolladores = desarrolladores == null ? List.of()
                : desarrolladores.stream().filter(d -> d != null && d.usuarioId() != null).toList();
        if (desarrolladores.stream().map(Desarrollador::usuarioId).distinct().count() < desarrolladores.size()) {
            throw new ReglaNegocioException("Un Desarrollador aparece dos veces");
        }
        Set<Long> repositorios = moduloIds;
        if (desarrolladores.stream().anyMatch(d -> d.moduloId() != null && !repositorios.contains(d.moduloId()))) {
            throw new ReglaNegocioException("El repositorio de cada Desarrollador debe ser uno de la historia");
        }
        prioridad = prioridad == null ? "MEDIA" : prioridad.trim().toUpperCase(Locale.ROOT);
        if (!PRIORIDADES.contains(prioridad)) {
            throw new ReglaNegocioException("La prioridad debe ser Baja, Media o Alta");
        }
        if (puntos != null && !PUNTOS.contains(puntos)) {
            throw new ReglaNegocioException("La estimación debe ser 1, 2, 3, 5, 8 o 13 puntos");
        }
        figmaUrl = limpio(figmaUrl);
        if (figmaUrl != null && !figmaUrl.startsWith("http")) {
            figmaUrl = "https://" + figmaUrl;
        }
        if (figmaUrl != null && !URL.matcher(figmaUrl).matches()) {
            throw new ReglaNegocioException("El enlace de Figma no parece una dirección web");
        }
        if (disenoAprobado && figmaUrl == null) {
            throw new ReglaNegocioException("Para marcar el diseño como aprobado, pon el enlace de Figma");
        }

        if (!borrador) {
            if (como == null || quiero == null || para == null) {
                throw new ReglaNegocioException("Completa Como, Quiero y Para (o guárdala como borrador)");
            }
            if (criterios.isEmpty()) {
                throw new ReglaNegocioException("Agrega al menos un criterio de aceptación (o guárdala como borrador)");
            }
            if (moduloIds.isEmpty()) {
                throw new ReglaNegocioException("Elige al menos un repositorio (o guárdala como borrador)");
            }
        }
    }

    /**
     * Sus Desarrolladores deben ser del equipo del proyecto con rol Desarrollador
     * (a los que ya estaban no se les vuelve a pedir el rol).
     */
    public void verificarDesarrolladores(List<Analista> equipo, Set<Long> yaEstaban) {
        for (Desarrollador d : desarrolladores) {
            boolean valido = equipo.stream().anyMatch(a -> a.id().equals(d.usuarioId())
                    && (a.esDesarrollador() || yaEstaban.contains(a.id())));
            if (!valido) {
                throw new ReglaNegocioException("Los Desarrolladores de la historia deben ser del equipo del proyecto"
                        + " y tener el rol de Desarrollador");
            }
        }
    }

    /** Sin vacíos ni espacios sobrantes. */
    public static List<String> limpios(List<String> textos) {
        return textos == null ? List.of()
                : textos.stream().filter(Objects::nonNull).map(String::trim).filter(t -> !t.isEmpty()).toList();
    }

    private static String limpio(String texto) {
        return texto == null || texto.isBlank() ? null : texto.trim();
    }
}
