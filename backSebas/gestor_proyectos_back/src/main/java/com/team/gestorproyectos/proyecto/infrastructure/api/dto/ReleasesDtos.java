package com.team.gestorproyectos.proyecto.infrastructure.api.dto;

import java.time.LocalDateTime;
import java.util.List;

/** JSON de la pestaña Releases del proyecto (PDF pág. 36). */
public final class ReleasesDtos {

    private ReleasesDtos() {
    }

    public record ReleasesResponse(List<HistoriaListaResponse> listasParaIntegrar, boolean puedeIntegrar,
                                   Long proximaFaseId, ParcheResponse parche, List<PublicadoResponse> publicados) {
    }

    public record HistoriaListaResponse(Long id, int numero, String titulo) {
    }

    public record ParcheResponse(String version, int aprobados, int enCamino, Long mantenimientoFaseId) {
    }

    /** @param tipo FASE o PARCHE; @param estado PUBLICADA o REVERTIDA */
    public record PublicadoResponse(Long id, String version, String tipo, String estado, Long faseId, Integer faseNumero,
                                    String faseNombre, LocalDateTime fecha, int historias, int tickets,
                                    List<BugResponse> bugs, String publicadoPor, boolean enProduccion) {
    }

    public record BugResponse(Long id, String codigo, String titulo) {
    }
}
