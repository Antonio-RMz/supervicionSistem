package com.team.gestorproyectos.fase.domain.model;

/**
 * La vista del release de la fase (PDF pág. 27) para quien la ve.
 *
 * @param puedeEditarNotas el Líder (o el responsable) mientras la fase no se publica
 */
public record ReleaseFase(InfoRelease info, String notas, boolean puedeEditarNotas) {
}
