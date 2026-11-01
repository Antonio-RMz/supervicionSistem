package com.team.gestorproyectos.ticket.infrastructure.api.dto;

import java.util.List;

import com.team.gestorproyectos.ticket.domain.model.TipoMovimiento;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** JSON del Tablero y de mover un ticket. Columnas: POR_HACER, EN_PROGRESO, EN_QA, APROBADA, CERRADA. */
public final class TableroDtos {

    private TableroDtos() {
    }

    public record TableroResponse(List<FaseResponse> fases, Long faseId, List<TarjetaResponse> tarjetas) {
    }

    public record FaseResponse(Long id, int numero, String nombre, String estado, boolean mantenimiento) {
    }

    /**
     * @param avance       lo que yo puedo hacer para avanzarla; null si no me toca
     * @param rechazo      regresarla (si soy quien revisa); null si no
     * @param bloqueadaPor "Analista", "Líder"... si la siguiente acción es de otra persona
     */
    public record TarjetaResponse(
            Long id,
            String tipo,
            String codigo,
            String titulo,
            String estado,
            String prioridad,
            String columna,
            Long historiaId,
            String historiaCodigo,
            Long responsableId,
            String responsableNombre,
            String responsableIniciales,
            MovimientoResponse avance,
            MovimientoResponse rechazo,
            String bloqueadaPor,
            boolean mia) {
    }

    public record MovimientoResponse(String tipo, String accion, String columnaDestino) {
    }

    /**
     * @param bloqueadoPorId al bloquear: el ticket que lo detiene; null si es algo externo
     * @param destrabadorId  al bloquear: quién puede destrabarlo (opcional)
     * @param severidad      al confirmar un bug: CRITICA, ALTA, MEDIA o BAJA
     * @param responsableId  al confirmar un bug: el Desarrollador que lo corrige
     */
    public record MoverRequest(
            @NotNull TipoMovimiento tipo,
            @NotBlank String estadoEsperado,
            @Size(max = 1000) String motivo,
            Long bloqueadoPorId,
            Long destrabadorId,
            String severidad,
            Long responsableId) {
    }

    /** @param rama la rama que se creó al iniciar, para mostrarla; null si no se creó */
    public record MoverResponse(String accion, String estado, String rama) {
    }
}
