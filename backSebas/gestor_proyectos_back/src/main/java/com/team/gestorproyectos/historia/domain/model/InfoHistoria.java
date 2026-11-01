package com.team.gestorproyectos.historia.domain.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Lo que se lee de una historia para su detalle (PDF pág. 24). Las acciones las decide
 * {@link HistoriaParaGestionar}.
 *
 * @param figmaUrl     su propio enlace o, si no tiene, el de su fase
 * @param disenoDeFase el enlace viene de la fase
 */
public record InfoHistoria(
        Long id,
        Long proyectoId,
        String proyectoNombre,
        Long faseId,
        int faseNumero,
        String faseNombre,
        String faseEstado,
        LocalDate faseInicio,
        LocalDate faseFin,
        int numero,
        String titulo,
        String como,
        String quiero,
        String para,
        String estado,
        String prioridad,
        Integer puntos,
        String analistaNombre,
        String figmaUrl,
        boolean disenoDeFase,
        boolean disenoAprobado,
        String disenoAprobadoPor,
        LocalDateTime disenoAprobadoEn,
        List<String> modulos,
        List<Criterio> criterios,
        List<Ticket> tickets,
        List<CambioDeEstado> historial,
        List<Adjunto> adjuntos,
        List<Comentario> comentarios) {

    /** @param tickets códigos de los tickets que lo cubren ("ACTV-41") */
    public record Criterio(Long id, String descripcion, boolean verificado, List<String> tickets) {
    }

    /** @param dependeDe códigos de los tickets que deben cerrarse antes de iniciarlo */
    public record Ticket(Long id, String tipo, int numero, String titulo, String estado, String moduloTipo,
                         String responsableNombre, List<String> dependeDe) {
    }

    /** @param usuario nombre corto; null si fue automático */
    public record CambioDeEstado(String estado, String motivo, boolean automatico, String usuario,
                                 LocalDateTime fecha) {
    }

    /** Una imagen de la historia; su contenido se pide aparte. */
    public record Adjunto(Long id, String nombre, String tipoMime, Long tamanoBytes) {
    }

    public record Comentario(String usuario, String texto, LocalDateTime fecha) {
    }
}
