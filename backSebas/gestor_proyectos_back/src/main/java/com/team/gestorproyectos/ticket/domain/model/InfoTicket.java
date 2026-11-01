package com.team.gestorproyectos.ticket.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Lo que se lee de un ticket para su detalle (PDF pág. 29). Los botones los decide {@link TicketParaMover}.
 *
 * @param historia      la historia que trabaja; null si va suelto
 * @param criterios     los criterios de su historia que cubre
 * @param bloqueadoPor  "ACTV-07" si lo detiene otro ticket
 * @param rechazosEnQa  cuántas veces lo han regresado de QA
 */
public record InfoTicket(
        Long id,
        Long proyectoId,
        String proyectoNombre,
        Long faseId,
        int faseNumero,
        String faseNombre,
        Historia historia,
        String tipo,
        int numero,
        String titulo,
        String descripcion,
        String estado,
        String prioridad,
        List<String> capas,
        Modulo modulo,
        Persona responsable,
        Persona tester,
        Persona creadoPor,
        BigDecimal estimacionHoras,
        BigDecimal horasRegistradas,
        LocalDate fechaLimite,
        String rama,
        String bloqueadoPor,
        long rechazosEnQa,
        List<Dependencia> dependencias,
        List<Subtarea> subtareas,
        List<PullRequest> pullRequests,
        List<Criterio> criterios,
        List<CambioDeEstado> historial,
        List<Comentario> comentarios) {

    public record Historia(Long id, int numero, String titulo, String estado, String como, String quiero,
                           String para) {
    }

    public record Modulo(String tipo, String nombre, String tecnologia, String repositorio, String ramaBase) {
    }

    public record Persona(Long id, String nombre) {
    }

    public record Dependencia(String codigo, String titulo, String estado) {
    }

    public record Subtarea(Long id, String descripcion, boolean hecha) {
    }

    public record PullRequest(int numero, String ramaOrigen, String ramaDestino, String estado, String ciEstado,
                              String url) {
    }

    /** @param soloEste lo cubre solo este ticket: si se cancela, se queda sin cubrir */
    public record Criterio(String descripcion, boolean verificado, boolean soloEste) {
    }

    /** @param usuario nombre corto; null si fue automático */
    public record CambioDeEstado(String estadoAnterior, String estado, String motivo, boolean automatico,
                                 String usuario, LocalDateTime fecha) {
    }

    public record Comentario(String usuario, String texto, LocalDateTime fecha) {
    }
}
