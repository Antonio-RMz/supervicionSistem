package com.team.gestorproyectos.proyecto.infrastructure.api.dto;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.constraints.NotNull;

/** JSON de la pestaña Equipo del proyecto (PDF pág. 34). */
public final class EquipoDtos {

    private EquipoDtos() {
    }

    public record EquipoResponse(Long proyectoId, List<MiembroResponse> miembros, List<CoberturaResponse> cobertura,
                                 List<String> alertas, List<PermisoResponse> permisos, ReglasResponse reglas,
                                 boolean puedeGestionar) {
    }

    /** @param carga BAJA, MEDIA o ALTA */
    public record MiembroResponse(Long id, String nombreCompleto, String nombreCorto, String iniciales,
                                  List<String> roles, LocalDateTime ultimoAcceso, boolean esLider,
                                  List<String> modulos, int ticketsAbiertos, String carga, int leToca) {
    }

    /** @param resultado OK, AVISO o FALTA */
    public record CoberturaResponse(String rol, String resultado, String quien) {
    }

    public record PermisoResponse(String permiso, Long usuarioId, String usuario, boolean otorgado) {
    }

    public record ReglasResponse(boolean permitirAutovalidacion, boolean comentarioAlRechazar, boolean ramaAlIniciar,
                                 boolean notificarClienteFase) {
    }

    public record CandidatoResponse(Long id, String nombreCompleto, List<String> roles) {
    }

    public record AgregarMiembroRequest(@NotNull Long usuarioId) {
    }

    /** Cualquiera de los dos puede ir vacío si esa parte no se reasigna. */
    public record ReasignarRequest(Long responsableNuevoId, Long testerNuevoId) {
    }

    public record ReasignadosResponse(int tickets) {
    }
}
