package com.team.gestorproyectos.portalcliente.infrastructure.api.dto;

import java.time.LocalDate;
import java.util.List;

/**
 * Respuestas JSON del Portal de avance. Van juntas porque son pequeñas y solo las usa este módulo.
 * Los estados viajan como texto (ej. "EN_CURSO"); el front decide la etiqueta y el color.
 */
public final class PortalClienteDtos {

    private PortalClienteDtos() {
    }

    /** Tarjeta de la pantalla "Mis proyectos". */
    public record MiProyectoResponse(
            Long id,
            String codigo,
            String nombre,
            String estado,
            List<ModuloResponse> modulos,
            int avance,
            FaseResponse faseActual,
            int demosPendientes) {
    }

    /** Pantalla "Avance". */
    public record AvanceResponse(
            Long id,
            String codigo,
            String nombre,
            String estado,
            int avance,
            List<ModuloResponse> modulos,
            List<FaseResponse> fases,
            FaseResponse faseActual,
            List<ModuloAvanceResponse> modulosFaseActual,
            List<HistoriaResponse> historias,
            List<VersionResponse> versiones,
            ContactoResponse contacto,
            DemoResponse demoPendiente) {
    }

    public record ModuloResponse(String nombre, String tipo) {
    }

    /** @param estado PROXIMA, EN_CURSO, EN_PAUSA, ENTREGADA o CANCELADA */
    public record FaseResponse(
            Long id,
            int numero,
            String nombre,
            String objetivo,
            String estado,
            LocalDate fechaInicio,
            LocalDate fechaFin,
            String version,
            int avance) {
    }

    public record ModuloAvanceResponse(String nombre, String tipo, int avance) {
    }

    /** @param estado PLANEADO, EN_DESARROLLO, EN_PRUEBAS, LISTO o ENTREGADO */
    public record HistoriaResponse(String titulo, String descripcion, String estado) {
    }

    public record VersionResponse(String version, LocalDate fecha, String resumen) {
    }

    public record ContactoResponse(String nombre, String rol, String correo) {
    }

    public record DemoResponse(Long id, String titulo, String url) {
    }
}
