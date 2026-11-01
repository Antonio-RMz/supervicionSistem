package com.team.gestorproyectos.fase.domain.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import com.team.gestorproyectos.shared.domain.model.Movimiento;

/**
 * Lo que se lee de una fase para su detalle (PDF pág. 32). Las acciones y los criterios
 * los decide {@link FaseParaGestionar}.
 *
 * @param figmaParaTodas las historias sin diseño propio heredan {@code figmaUrl}
 * @param enEstadoDesde  cuándo entró al estado actual ("Lleva 6 días en pausa")
 * @param tickets        cuántos tickets hay de cada tipo y estado
 * @param aCancelar      lo que se cancelaría con la fase (PDF pág. 25)
 */
public record InfoFase(
        Long id,
        Long proyectoId,
        String proyectoNombre,
        String proyectoCodigo,
        String clienteNombre,
        int numero,
        String nombre,
        String objetivo,
        String estado,
        String prioridad,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        String responsableNombre,
        String versionRelease,
        boolean mantenimiento,
        String figmaUrl,
        boolean figmaParaTodas,
        String motivoEstado,
        LocalDate reanudarEl,
        LocalDateTime enEstadoDesde,
        String cambioEstadoPor,
        List<ModuloDeFase> modulos,
        List<TicketsEnEstado> tickets,
        List<HistoriaDeFase> historias,
        List<Movimiento> actividad,
        Impacto aCancelar,
        List<PersonaDeFase> equipo) {

    public record ModuloDeFase(String tipo, String nombre) {
    }

    public record TicketsEnEstado(String tipo, String estado, long cantidad) {
    }

    /**
     * @param terminados   tickets aprobados o más adelante, de {@code total}
     * @param incompleta   le falta algo para marcarla Lista: Como / Quiero / Para, criterios o repositorios
     * @param disenoPropio tiene su propio enlace de Figma (no hereda el de la fase)
     * @param sinTickets   no tiene tickets (ni cancelados) ni hay tickets de Mantenimiento que vengan de ella
     */
    public record HistoriaDeFase(Long id, int numero, String titulo, String estado, long terminados, long total,
                                 String analistaIniciales, boolean incompleta, boolean disenoPropio,
                                 boolean sinTickets) {

        /** Aún no se inicia: se puede eliminar (las mismas condiciones que en el módulo de historias). */
        private static final Set<String> SIN_INICIAR = Set.of("BORRADOR", "EN_PLANEACION", "LISTA");

        public boolean eliminable() {
            return sinTickets && SIN_INICIAR.contains(estado);
        }
    }

    public record Impacto(long historias, long tickets) {
    }

    public record PersonaDeFase(Long id, String nombreCompleto, List<String> roles) {
    }
}
