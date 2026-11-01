package com.team.gestorproyectos.ticket.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

import com.team.gestorproyectos.shared.domain.exception.ReglaNegocioException;

/**
 * Lo que se captura en "Nuevo ticket de trabajo" (PDF pág. 19). Todo ticket de trabajo vive dentro de una historia.
 *
 * @param capas        partes del código que toca: Base de datos, Servicio, Controller REST...
 * @param criterioIds  criterios de la historia que este ticket cubre
 * @param subtareas    pasos para quien lo desarrolla; no cambian el estado del ticket
 * @param dependeDe    tickets que deben cerrarse antes de poder iniciar este
 * @param testerId     quien lo prueba en QA (null si el Líder)
 */
public record NuevoTicket(
        Long faseId,
        Long historiaId,
        TipoTicket tipo,
        String titulo,
        String descripcion,
        Long moduloId,
        Set<String> capas,
        Set<Long> criterioIds,
        List<String> subtareas,
        Set<Long> dependeDe,
        Long responsableId,
        Long testerId,
        String prioridad,
        BigDecimal estimacionHoras,
        LocalDate fechaLimite) {

    /** En este orden se muestran y se guardan. */
    public static final List<String> CAPAS = List.of(
            "Base de datos", "Entidad / Repository", "Servicio", "Controller REST", "Seguridad", "Pruebas",
            "Pantalla", "Componente", "Estilos", "Infraestructura");

    private static final Set<String> PRIORIDADES = Set.of("BAJA", "MEDIA", "ALTA");
    private static final BigDecimal MAXIMO_HORAS = new BigDecimal("999");

    public NuevoTicket {
        if (faseId == null || historiaId == null) {
            throw new ReglaNegocioException("Elige la fase y la historia a la que pertenece el ticket");
        }
        if (tipo == null) {
            throw new ReglaNegocioException("Elige el tipo de ticket");
        }
        titulo = titulo == null ? "" : titulo.trim();
        if (titulo.isEmpty()) {
            throw new ReglaNegocioException("Escribe qué se hará (el título)");
        }
        descripcion = descripcion == null || descripcion.isBlank() ? null : descripcion.trim();
        if (moduloId == null) {
            throw new ReglaNegocioException("Elige el repositorio: un ticket pertenece a un solo repositorio");
        }
        capas = capas == null ? Set.of() : Set.copyOf(capas);
        capas.stream().filter(c -> !CAPAS.contains(c)).findFirst().ifPresent(c -> {
            throw new ReglaNegocioException("La capa " + c + " no existe");
        });
        criterioIds = criterioIds == null ? Set.of() : Set.copyOf(criterioIds);
        subtareas = subtareas == null ? List.of()
                : subtareas.stream().filter(Objects::nonNull).map(String::trim).filter(s -> !s.isEmpty()).toList();
        dependeDe = dependeDe == null ? Set.of() : Set.copyOf(dependeDe);
        if (responsableId == null) {
            throw new ReglaNegocioException("Elige al responsable");
        }
        prioridad = prioridad == null ? "MEDIA" : prioridad.trim().toUpperCase(Locale.ROOT);
        if (!PRIORIDADES.contains(prioridad)) {
            throw new ReglaNegocioException("La prioridad debe ser Baja, Media o Alta");
        }
        if (estimacionHoras != null
                && (estimacionHoras.signum() <= 0 || estimacionHoras.compareTo(MAXIMO_HORAS) > 0)) {
            throw new ReglaNegocioException("La estimación debe ser de más de 0 y hasta 999 horas");
        }
    }

    /** Capas en el orden del catálogo, para guardarlas como texto: "Base de datos, Servicio". */
    public String capasComoTexto() {
        List<String> ordenadas = CAPAS.stream().filter(capas::contains).toList();
        return ordenadas.isEmpty() ? null : String.join(", ", ordenadas);
    }
}
