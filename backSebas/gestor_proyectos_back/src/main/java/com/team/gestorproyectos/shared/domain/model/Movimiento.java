package com.team.gestorproyectos.shared.domain.model;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Un cambio de estado del historial. Sabe contarse en una frase: "Ana rechazó ACTV-30 en QA".
 *
 * @param entidad FASE, HISTORIA o TICKET
 * @param tipo    ACTV, BUGS... (HIST para historias, FASE para fases)
 * @param usuario nombre corto de quien lo hizo; null si fue automático
 */
public record Movimiento(
        String usuario,
        String entidad,
        String tipo,
        int numero,
        String titulo,
        String estadoAnterior,
        String estadoNuevo,
        boolean automatico,
        LocalDateTime fecha) {

    private static final Map<String, String> ESTADOS_LEGIBLES = Map.ofEntries(
            Map.entry("EN_VALIDACION", "En validación"),
            Map.entry("EN_DESARROLLO", "En desarrollo"),
            Map.entry("LISTA_PARA_RELEASE", "Lista para release"),
            Map.entry("EN_PUBLICACION", "En publicación"),
            Map.entry("EN_CORRECCION", "En corrección"),
            Map.entry("EN_REVISION", "En revisión"),
            Map.entry("POR_HACER", "Por hacer"));

    /** Qué tipo de paso fue; el front le pone color en el historial de una persona. */
    public enum Clase {
        INICIO, ENVIO, RECHAZO, CIERRE, OTRO
    }

    public String descripcion() {
        String objeto = nombrar(entidad, tipo, numero, titulo);
        if (automatico || usuario == null) {
            return capitalizar(objeto) + " pasó a " + legible(estadoNuevo);
        }
        return usuario + " " + verbo(objeto);
    }

    /** Sin el nombre de quien lo hizo, para su propio historial: "Envió MEJR-07 a QA". */
    public String queHizo() {
        return capitalizar(verbo(nombrar(entidad, tipo, numero, titulo)));
    }

    public Clase clase() {
        boolean veniaDeRevision = "EN_QA".equals(estadoAnterior) || "EN_REVISION".equals(estadoAnterior);
        return switch (estadoNuevo) {
            case "EN_PROGRESO", "EN_CORRECCION" -> veniaDeRevision ? Clase.RECHAZO : Clase.INICIO;
            case "EN_DESARROLLO" -> "EN_VALIDACION".equals(estadoAnterior) || "EN_QA".equals(estadoAnterior)
                    ? Clase.RECHAZO : Clase.INICIO;
            case "INVESTIGANDO", "REDACTANDO", "EN_CURSO" -> Clase.INICIO;
            case "EN_QA", "EN_REVISION", "CONCLUSIONES", "EN_VALIDACION" -> Clase.ENVIO;
            case "APROBADA", "APROBADO", "APROBADA_QA", "HECHA", "INTEGRADA", "INTEGRADO", "ACEPTADA",
                 "CERRADA", "CERRADO", "PUBLICADA", "PUBLICADO", "REVISADA" -> Clase.CIERRE;
            default -> Clase.OTRO;
        };
    }

    /** "BUGS-31", o "la fase 2 · Desarrollo" para una fase. */
    public static String nombrar(String entidad, String tipo, int numero, String titulo) {
        return "FASE".equals(entidad)
                ? "la fase " + numero + " · " + titulo
                : CodigoItem.de(tipo, numero);
    }

    private String verbo(String objeto) {
        if (estadoAnterior == null) {
            return ("BUGS".equals(tipo) ? "reportó " : "creó ") + objeto;
        }
        boolean veniaDeQa = "EN_QA".equals(estadoAnterior);
        boolean veniaDePr = "EN_REVISION".equals(estadoAnterior);
        return switch (estadoNuevo) {
            case "EN_QA" -> "HIST".equals(tipo) ? "pasó " + objeto + " a QA" : "envió " + objeto + " a QA";
            case "EN_PROGRESO", "EN_CORRECCION" -> veniaDeQa ? "rechazó " + objeto + " en QA"
                    : veniaDePr ? "pidió cambios al PR de " + objeto : "inició " + objeto;
            case "EN_CURSO" -> "PAUSADA".equals(estadoAnterior) ? "reanudó " + objeto : "inició " + objeto;
            case "PLANEADA" -> "CANCELADA".equals(estadoAnterior) ? "reabrió " + objeto : "movió " + objeto + " a planeada";
            case "EN_DESARROLLO" -> "EN_VALIDACION".equals(estadoAnterior) ? "devolvió " + objeto + " a desarrollo"
                    : veniaDeQa ? "rechazó " + objeto + " en QA"
                    : "LISTA".equals(estadoAnterior) ? "dejó las Actividades de " + objeto + " listas y arrancó"
                    : "inició " + objeto;
            case "POR_HACER" -> "BORRADOR".equals(estadoAnterior) ? "marcó " + objeto + " como Lista"
                    : "movió " + objeto + " a Por hacer";
            case "BORRADOR" -> "DESCARTADA".equals(estadoAnterior) ? "reabrió " + objeto
                    : "regresó " + objeto + " a Borrador";
            case "INVESTIGANDO", "REDACTANDO" -> "inició " + objeto;
            case "APROBADA", "APROBADO", "APROBADA_QA" -> veniaDeQa ? "aprobó " + objeto + " en QA"
                    : veniaDePr ? "aprobó el PR de " + objeto
                    // Forma Normal sin PR: el Desarrollador la termina
                    : "EN_PROGRESO".equals(estadoAnterior) ? "terminó " + objeto : "aprobó " + objeto;
            case "BLOQUEADA" -> "bloqueó " + objeto;
            case "CONFIRMADO" -> "confirmó " + objeto;
            case "EN_REVISION" -> "envió el PR de " + objeto;
            case "EN_VALIDACION" -> veniaDeQa ? "aprobó " + objeto + " en QA" : "envió " + objeto + " a validación";
            case "HECHA" -> "aprobó la revisión de " + objeto;
            case "INTEGRADA", "INTEGRADO" -> "integró " + objeto + " a develop";
            case "EN_PLANEACION" -> "LISTA".equals(estadoAnterior) || "LISTA_DESARROLLO".equals(estadoAnterior)
                    ? "regresó " + objeto + " a planeación" : "inició la planeación de " + objeto;
            case "LISTA" -> "marcó " + objeto + " como Lista";
            case "LISTA_DESARROLLO" -> "dejó las Actividades de " + objeto + " listas";
            case "ACEPTADA" -> "aceptó " + objeto;
            case "CERRADA", "CERRADO" -> "cerró " + objeto;
            case "PUBLICADA", "PUBLICADO" -> "publicó " + objeto;
            case "REVISADA" -> "revisó " + objeto;
            case "CANCELADA" -> "canceló " + objeto;
            case "DESCARTADA" -> "descartó " + objeto;
            case "PAUSADA" -> "pausó " + objeto;
            case "LISTA_PARA_RELEASE" -> "dejó " + objeto + " lista para release";
            case "EN_PUBLICACION" -> "subió " + objeto + " a main";
            case "REVERTIDA" -> "revirtió " + objeto;
            default -> "movió " + objeto + " a " + legible(estadoNuevo);
        };
    }

    private static String legible(String estado) {
        String texto = ESTADOS_LEGIBLES.getOrDefault(estado, estado.replace('_', ' ').toLowerCase());
        return texto.toLowerCase();
    }

    private static String capitalizar(String texto) {
        return texto.isEmpty() ? texto : Character.toUpperCase(texto.charAt(0)) + texto.substring(1);
    }
}
