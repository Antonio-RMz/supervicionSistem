package com.team.gestorproyectos.historia.domain.model;

import java.util.Set;

import com.team.gestorproyectos.shared.domain.port.PermisosPort;

/**
 * Las decisiones de personas sobre una historia (PDF pág. 21): Iniciar planeación y Marcar como Lista (Analista o
 * Líder), Aceptar o Devolver (Analista, que la prueba completa) e Integrar (Líder). Lo demás pasa solo: con su
 * primera Actividad queda Lista para desarrollo, y la fase la arranca (al iniciarse, o en ese momento si ya corre).
 *
 * @param decide  quién la hace: el Analista de la historia o el Líder del proyecto
 * @param enLista si aparece como botón en las listas; Devolver, Descartar y demás viven en el detalle
 */
public enum AccionHistoria {
    // Con lo básico capturado, se empieza a definir.
    INICIAR_PLANEACION("Iniciar planeación", Set.of("BORRADOR"), "EN_PLANEACION", PermisosPort.MARCAR_HISTORIA_LISTA,
            Decide.ANALISTA, true),
    MARCAR_LISTA("Marcar como Lista", Set.of("EN_PLANEACION"), "LISTA", PermisosPort.MARCAR_HISTORIA_LISTA,
            Decide.ANALISTA, true),
    REGRESAR_A_PLANEACION("Regresar a planeación", Set.of("LISTA", "LISTA_DESARROLLO"), "EN_PLANEACION",
            PermisosPort.MARCAR_HISTORIA_LISTA, Decide.ANALISTA, false),
    DESCARTAR("Descartar", Set.of("BORRADOR", "EN_PLANEACION", "LISTA", "LISTA_DESARROLLO"), "DESCARTADA",
            PermisosPort.DESCARTAR_HISTORIAS, Decide.ANALISTA, false),
    // Si no alcanza a integrarse, se va a otra fase con sus tickets y sigue en el estado en que estaba.
    MOVER_DE_FASE("Mover a otra fase", Set.of("BORRADOR", "EN_PLANEACION", "LISTA", "LISTA_DESARROLLO",
            "EN_DESARROLLO", "EN_VALIDACION"), null, PermisosPort.INICIAR_HISTORIAS, Decide.LIDER, false),
    // Con todos sus PR aprobados, el Analista la prueba completa: la acepta o la devuelve.
    ACEPTAR("Aceptar historia", Set.of("EN_VALIDACION"), "ACEPTADA", PermisosPort.ACEPTAR_HISTORIAS, Decide.ANALISTA,
            true),
    DEVOLVER("Devolver a desarrollo", Set.of("EN_VALIDACION"), "EN_DESARROLLO", PermisosPort.ACEPTAR_HISTORIAS,
            Decide.ANALISTA, false),
    INTEGRAR("Integrar a develop", Set.of("ACEPTADA"), "INTEGRADA", PermisosPort.INTEGRAR_HISTORIAS, Decide.LIDER,
            true),
    REABRIR("Reabrir historia", Set.of("DESCARTADA"), "BORRADOR", PermisosPort.DESCARTAR_HISTORIAS, Decide.LIDER,
            false);

    public enum Decide { ANALISTA, LIDER }

    private final String texto;
    private final Set<String> desde;
    private final String destino;
    private final String permiso;
    private final Decide decide;
    private final boolean enLista;

    AccionHistoria(String texto, Set<String> desde, String destino, String permiso, Decide decide, boolean enLista) {
        this.texto = texto;
        this.desde = desde;
        this.destino = destino;
        this.permiso = permiso;
        this.decide = decide;
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

    public Decide decide() {
        return decide;
    }

    public boolean enLista() {
        return enLista;
    }

    /** Descartar y Devolver quedan en el historial con su motivo (PDF pág. 33). */
    public boolean pideMotivo() {
        return this == DESCARTAR || this == DEVOLVER;
    }

    /** Regresa la historia a desarrollo: Devolver reabre un ticket o crea un bug. */
    public boolean regresaADesarrollo() {
        return this == DEVOLVER;
    }
}
