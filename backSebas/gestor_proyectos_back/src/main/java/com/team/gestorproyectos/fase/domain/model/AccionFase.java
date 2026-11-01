package com.team.gestorproyectos.fase.domain.model;

import java.util.Set;

import com.team.gestorproyectos.shared.domain.port.PermisosPort;

/**
 * Lo que el Líder puede hacerle a una fase en cada estado (PDF pág. 20). Pasar de En curso a
 * Lista para release lo hace el sistema solo al cumplirse los criterios; Cerrar fase es el respaldo manual.
 *
 * @param enLista si aparece como botón en el árbol del proyecto; Pausar, Cancelar y Revertir
 *                viven solo en el detalle porque piden motivo
 */
public enum AccionFase {
    INICIAR("Iniciar fase", Set.of("PLANEADA"), "EN_CURSO", PermisosPort.GESTIONAR_FASES, true),
    PAUSAR("Pausar fase", Set.of("EN_CURSO"), "PAUSADA", PermisosPort.GESTIONAR_FASES, false),
    REANUDAR("Reanudar fase", Set.of("PAUSADA"), "EN_CURSO", PermisosPort.GESTIONAR_FASES, true),
    CANCELAR("Cancelar fase", Set.of("PLANEADA", "PAUSADA"), "CANCELADA", PermisosPort.GESTIONAR_FASES, false),
    CERRAR("Cerrar fase", Set.of("EN_CURSO"), "LISTA_PARA_RELEASE", PermisosPort.GESTIONAR_FASES, false),
    SUBIR_A_MAIN("Subir a main", Set.of("LISTA_PARA_RELEASE"), "EN_PUBLICACION", PermisosPort.PUBLICAR_RELEASE, true),
    DESPLEGAR("Desplegar a producción", Set.of("EN_PUBLICACION"), "PUBLICADA", PermisosPort.PUBLICAR_RELEASE, true),
    REVERTIR("Revertir", Set.of("PUBLICADA"), "REVERTIDA", PermisosPort.REVERTIR_RELEASE, false),
    PUBLICAR_DE_NUEVO("Publicar de nuevo", Set.of("REVERTIDA"), "EN_PUBLICACION", PermisosPort.PUBLICAR_RELEASE, true),
    REABRIR("Reabrir fase", Set.of("CANCELADA"), "PLANEADA", PermisosPort.REABRIR_FASES, true);

    private final String texto;
    private final Set<String> desde;
    private final String destino;
    private final String permiso;
    private final boolean enLista;

    AccionFase(String texto, Set<String> desde, String destino, String permiso, boolean enLista) {
        this.texto = texto;
        this.desde = desde;
        this.destino = destino;
        this.permiso = permiso;
        this.enLista = enLista;
    }

    public String texto() {
        return texto;
    }

    public boolean aplicaEn(String estado) {
        return desde.contains(estado);
    }

    public String destino() {
        return destino;
    }

    public String permiso() {
        return permiso;
    }

    public boolean enLista() {
        return enLista;
    }

    /** Pausar, Cancelar y Revertir quedan en el historial con su motivo (PDF pág. 25). */
    public boolean pideMotivo() {
        return this == PAUSAR || this == CANCELAR || this == REVERTIR;
    }
}
