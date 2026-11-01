package com.team.gestorproyectos.ticket.domain.model;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import com.team.gestorproyectos.shared.domain.exception.AccesoDenegadoException;
import com.team.gestorproyectos.shared.domain.exception.ReglaNegocioException;
import com.team.gestorproyectos.shared.domain.model.AccionPendiente;
import com.team.gestorproyectos.shared.domain.model.Actor;
import com.team.gestorproyectos.shared.domain.model.FlujoTrabajo;
import com.team.gestorproyectos.shared.domain.model.ItemTrabajo;

/**
 * Un ticket con las reglas de su proyecto, para decidir qué puede hacer cada quien con él
 * (PDF pág. 22, 23 y 26).
 *
 * @param historiaCodigo       "HU-0016", para explicar por qué aún no se puede iniciar
 * @param faseEstado           con la fase pausada nadie inicia tickets nuevos (PDF pág. 25)
 * @param faseMantenimiento    va en la fase permanente Mantenimiento
 * @param severidad            de un bug: CRITICA, ALTA, MEDIA o BAJA; null si no tiene
 * @param dependenciasAbiertas códigos de los tickets de los que depende y aún no se cierran
 */
public record TicketParaMover(
        ItemTrabajo item,
        Long proyectoId,
        Long liderId,
        String historiaCodigo,
        String faseEstado,
        boolean faseMantenimiento,
        String severidad,
        String rama,
        boolean permitirAutovalidacion,
        boolean comentarioAlRechazar,
        boolean ramaAlIniciar,
        List<String> dependenciasAbiertas) {

    private static final int LARGO_SLUG = 40;

    public static final Set<String> SEVERIDADES = Set.of("CRITICA", "ALTA", "MEDIA", "BAJA");

    /** Sin Analista disponible en una Crítica, el Líder puede aprobar QA (queda marcado, PDF pág. 23). */
    static final String APROBADO_POR_LIDER = "Aprobado por el Líder: no había Analista disponible";

    /**
     * Un botón del ticket para quien lo ve, con lo que aún le falta para poder presionarse.
     *
     * @param destino a qué estado lleva
     */
    public record Opcion(TipoMovimiento tipo, String texto, String destino, List<String> impedimentos) {
    }

    /**
     * Bug de producción Crítico o Alto en Mantenimiento: se corrige desde main y sale en un parche,
     * sin esperar a ninguna fase (PDF pág. 23). Los de severidad Media o Baja siguen el flujo normal.
     */
    public boolean esHotfix() {
        return "BUGS".equals(item.tipo()) && faseMantenimiento && ("CRITICA".equals(severidad) || "ALTA".equals(severidad));
    }

    /** Los botones que esta persona tiene con el ticket en su estado actual. */
    public List<Opcion> opciones(Long usuarioId) {
        List<Opcion> opciones = new ArrayList<>();
        boolean esLider = usuarioId.equals(liderId);
        avance().ifPresent(a -> {
            if ("BUGS:APROBADO".equals(item.tipo() + ":" + item.estado())) {
                // Solo un hotfix se publica en parche; los demás bugs salen con su fase.
                if (esHotfix() && esLider) {
                    opciones.add(new Opcion(TipoMovimiento.PUBLICAR_PARCHE, a.accion(), "PUBLICADO", List.of()));
                }
            } else if (leToca(usuarioId, a)) {
                opciones.add(new Opcion(TipoMovimiento.AVANZAR, a.accion(), a.destino(),
                        impedimentosParaAvanzar(a)));
            } else if (esLider && apruebaSinAnalista(a)) {
                opciones.add(new Opcion(TipoMovimiento.AVANZAR, a.accion() + " sin Analista", a.destino(), List.of()));
            }
        });
        FlujoTrabajo.rechazo(item).filter(a -> leToca(usuarioId, a))
                .ifPresent(a -> opciones.add(new Opcion(TipoMovimiento.RECHAZAR, a.accion(), a.destino(), List.of())));
        if (esLider && "BUGS".equals(item.tipo()) && "REPORTADO".equals(item.estado())) {
            opciones.add(new Opcion(TipoMovimiento.NO_REPRODUCIBLE, "No reproducible", "NO_REPRODUCIBLE", List.of()));
        }
        if (esLider && "NO_REPRODUCIBLE".equals(item.estado())) {
            opciones.add(new Opcion(TipoMovimiento.REABRIR, "Reabrir bug", "REPORTADO", List.of()));
        }
        boolean esResponsable = usuarioId.equals(item.responsableId());
        if (esResponsable && FlujoTrabajo.enTrabajo(item.estado())) {
            opciones.add(new Opcion(TipoMovimiento.BLOQUEAR, "Marcar bloqueada", "BLOQUEADA", List.of()));
        }
        if (esResponsable && FlujoTrabajo.enRevision(item.estado())) {
            opciones.add(new Opcion(TipoMovimiento.RETIRAR, "EN_QA".equals(item.estado()) ? "Retirar de QA" : "Retirar PR",
                    FlujoTrabajo.estadoDeTrabajo(item.tipo()), List.of()));
        }
        if (esLider && !FlujoTrabajo.terminado(item.estado())) {
            opciones.add(new Opcion(TipoMovimiento.CANCELAR, "Cancelar ticket", "CANCELADA", List.of()));
        }
        return opciones;
    }

    public Transicion mover(Long usuarioId, TipoMovimiento tipo, String estadoEsperado, String motivo) {
        return mover(usuarioId, new DatosMovimiento(tipo, estadoEsperado, motivo));
    }

    public Transicion mover(Long usuarioId, DatosMovimiento datos) {
        String codigo = item.codigo();
        if (!item.estado().equals(datos.estadoEsperado())) {
            throw new ReglaNegocioException(codigo + " ya cambió de estado. Recarga para ver cómo está ahora.");
        }
        String motivo = datos.motivo() == null || datos.motivo().isBlank() ? null : datos.motivo().trim();

        return switch (datos.tipo()) {
            case AVANZAR, RECHAZAR -> moverPorFlujo(usuarioId, datos, motivo);
            case BLOQUEAR -> moverExtra(usuarioId, datos, motivo,
                    "Solo quien lo trabaja puede marcarlo bloqueado", "Solo se marca bloqueado mientras se trabaja");
            case RETIRAR -> moverExtra(usuarioId, datos, motivo,
                    "Solo quien lo hizo puede retirarlo", codigo + " no está en revisión");
            case CANCELAR -> moverExtra(usuarioId, datos, motivo,
                    "Solo el Líder del proyecto cancela tickets", codigo + " ya no se puede cancelar");
            case NO_REPRODUCIBLE -> moverExtra(usuarioId, datos, motivo,
                    "Solo el Líder decide que un bug no se puede reproducir", "Solo un bug reportado se marca no reproducible");
            case REABRIR -> moverExtra(usuarioId, datos, motivo,
                    "Solo el Líder reabre un bug", "Solo se reabre un bug que no se pudo reproducir");
            case PUBLICAR_PARCHE -> throw new ReglaNegocioException(
                    "El parche se publica con todos los hotfix aprobados, desde el detalle del bug");
        };
    }

    /** Iniciar, Enviar a QA, Confirmar, Aprobar, Rechazar...: lo que dice el flujo de su tipo. */
    private Transicion moverPorFlujo(Long usuarioId, DatosMovimiento datos, String motivo) {
        String codigo = item.codigo();
        boolean avanza = datos.tipo() == TipoMovimiento.AVANZAR;
        AccionPendiente accion = (avanza ? avance() : FlujoTrabajo.rechazo(item))
                .orElseThrow(() -> new ReglaNegocioException(avanza
                        ? codigo + " no puede avanzar desde este estado"
                        : codigo + " no se puede regresar desde este estado"));
        if (!accion.ejecutable()) {
            throw new ReglaNegocioException("\"" + accion.accion() + "\" se hace desde su propio proceso, no desde aquí");
        }
        boolean sinAnalista = avanza && !leToca(usuarioId, accion) && usuarioId.equals(liderId)
                && apruebaSinAnalista(accion);
        if (!leToca(usuarioId, accion) && !sinAnalista) {
            throw new AccesoDenegadoException("\"" + accion.accion() + "\" le toca al " + rolDe(accion));
        }
        if (avanza && !sinAnalista) {
            impedimentosParaAvanzar(accion).stream().findFirst().ifPresent(falta -> {
                throw new ReglaNegocioException(falta);
            });
        }
        // Rechazar en QA pide motivo si el proyecto lo pide; los demás rechazos, siempre (PDF pág. 30).
        boolean siempreConMotivo = !"EN_QA".equals(item.estado());
        if (!avanza && (comentarioAlRechazar || siempreConMotivo) && motivo == null) {
            throw new ReglaNegocioException(switch (item.estado()) {
                case "PUBLICADO" -> "Escribe qué sigue fallando";
                case "PROPUESTA" -> "Escribe por qué se rechaza la mejora";
                case "EN_REVISION" -> "Escribe qué hay que cambiar en el PR";
                default -> "Escribe por qué lo regresas";
            });
        }

        String severidadNueva = null;
        Long responsableNuevo = null;
        if (avanza && "MEJR".equals(item.tipo()) && "PROPUESTA".equals(item.estado())) {
            // Aprobada y asignada: desde aquí sigue igual que una Actividad (PDF pág. 30).
            responsableNuevo = datos.responsableId() == null ? item.responsableId() : datos.responsableId();
            if (responsableNuevo == null) {
                throw new ReglaNegocioException("Aprobar la mejora pide el Desarrollador que la hará");
            }
        }
        if (avanza && "BUGS".equals(item.tipo()) && "REPORTADO".equals(item.estado())) {
            // Confirmar pide severidad y Desarrollador (PDF pág. 23).
            severidadNueva = datos.severidad() == null ? severidad : datos.severidad().trim().toUpperCase(Locale.ROOT);
            if (severidadNueva == null || !SEVERIDADES.contains(severidadNueva)) {
                throw new ReglaNegocioException("Confirmar pide la severidad: Crítica, Alta, Media o Baja");
            }
            responsableNuevo = datos.responsableId() == null ? item.responsableId() : datos.responsableId();
            if (responsableNuevo == null) {
                throw new ReglaNegocioException("Confirmar pide el Desarrollador que lo corrige");
            }
        }

        boolean inicia = FlujoTrabajo.iniciaTrabajo(item.estado(), accion.destino());
        String ramaNueva = inicia && ramaAlIniciar && rama == null ? ramaSugerida() : null;
        return new Transicion(item.id(), sinAnalista ? accion.accion() + " sin Analista" : accion.accion(), item.estado(),
                accion.destino(), sinAnalista ? APROBADO_POR_LIDER : motivo, ramaNueva, null, severidadNueva,
                responsableNuevo);
    }

    /** Quién tiene la acción principal: "Desarrollador", "Analista", "Líder" o "Nadie". */
    public String actor() {
        return avance().map(a -> FlujoTrabajo.nombreDelRol(item, a.actor())).orElse("Nadie");
    }

    private boolean leToca(Long usuarioId, AccionPendiente accion) {
        return usuarioId.equals(quienHace(accion));
    }

    private String rolDe(AccionPendiente accion) {
        return FlujoTrabajo.nombreDelRol(item, accion.actor());
    }

    /** Bloquear, Retirar, Cancelar, No reproducible y Reabrir: no son del flujo de su tipo. */
    private Transicion moverExtra(Long usuarioId, DatosMovimiento datos, String motivo, String sinPermiso,
                                  String noAplica) {
        TipoMovimiento tipo = datos.tipo();
        boolean aplica = switch (tipo) {
            case BLOQUEAR -> FlujoTrabajo.enTrabajo(item.estado());
            case RETIRAR -> FlujoTrabajo.enRevision(item.estado());
            case NO_REPRODUCIBLE -> "BUGS".equals(item.tipo()) && "REPORTADO".equals(item.estado());
            case REABRIR -> "NO_REPRODUCIBLE".equals(item.estado());
            default -> !FlujoTrabajo.terminado(item.estado());
        };
        if (!aplica) {
            throw new ReglaNegocioException(noAplica);
        }
        Opcion opcion = opciones(usuarioId).stream().filter(o -> o.tipo() == tipo).findFirst()
                .orElseThrow(() -> new AccesoDenegadoException(sinPermiso));
        boolean pideMotivo = tipo != TipoMovimiento.RETIRAR && tipo != TipoMovimiento.REABRIR;
        if (pideMotivo && motivo == null) {
            throw new ReglaNegocioException("Escribe el motivo");
        }
        Long bloqueadoPorId = tipo == TipoMovimiento.BLOQUEAR ? datos.bloqueadoPorId() : null;
        if (bloqueadoPorId != null && bloqueadoPorId.equals(item.id())) {
            throw new ReglaNegocioException("Un ticket no se puede bloquear a sí mismo");
        }
        return new Transicion(item.id(), opcion.texto(), item.estado(), opcion.destino(), motivo, null, bloqueadoPorId,
                null, null);
    }

    /**
     * La acción principal del estado, aunque aún no se pueda (para explicar por qué). Un hotfix no espera
     * a su historia: en lugar de PR lo prueba un Analista antes del parche (PDF pág. 23).
     */
    private Optional<AccionPendiente> avance() {
        if (esHotfix() && "EN_CORRECCION".equals(item.estado())) {
            return Optional.of(new AccionPendiente("Enviar a QA", Actor.RESPONSABLE, "EN_QA"));
        }
        return FlujoTrabajo.accionDelEstado(item);
    }

    private Long quienHace(AccionPendiente accion) {
        return FlujoTrabajo.quienActua(item, accion.actor(), liderId);
    }

    private boolean apruebaSinAnalista(AccionPendiente accion) {
        return "BUGS".equals(item.tipo()) && "EN_QA".equals(item.estado()) && accion.actor() == Actor.TESTER
                && "CRITICA".equals(severidad);
    }

    private List<String> impedimentosParaAvanzar(AccionPendiente accion) {
        List<String> faltan = new ArrayList<>();
        boolean inicia = FlujoTrabajo.iniciaTrabajo(item.estado(), accion.destino());
        if (inicia && FlujoTrabajo.esperaSuHistoria(item)) {
            faltan.add("Su historia " + (historiaCodigo == null ? "" : historiaCodigo + " ") + "aún no se inicia");
        }
        if (inicia && "PAUSADA".equals(faseEstado)) {
            faltan.add("La fase está pausada: no se pueden iniciar tickets nuevos");
        }
        if (inicia && !dependenciasAbiertas.isEmpty()) {
            faltan.add("Primero debe cerrarse " + String.join(", ", dependenciasAbiertas));
        }
        boolean apruebaQa = "EN_QA".equals(item.estado()) && accion.actor() == Actor.TESTER;
        if (apruebaQa && !permitirAutovalidacion && item.testerId() != null
                && item.testerId().equals(item.responsableId())) {
            faltan.add("En este proyecto nadie aprueba en QA su propio trabajo");
        }
        boolean apruebaPr = "EN_REVISION".equals(item.estado()) && accion.actor() == Actor.LIDER;
        if (apruebaPr && !permitirAutovalidacion && liderId.equals(item.responsableId())) {
            faltan.add("En este proyecto nadie aprueba su propio PR");
        }
        return faltan;
    }

    /**
     * "hotfix/bugs-50-error-500-en-facturas" (sale de main), "fix/bugs-31-error-al-refrescar-el-token-jwt",
     * "feature/actv-35-recuperar-contrasena-por-correo".
     */
    String ramaSugerida() {
        String prefijo = esHotfix() ? "hotfix/" : "BUGS".equals(item.tipo()) ? "fix/" : "feature/";
        String slug = Normalizer.normalize(item.titulo(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
        if (slug.length() > LARGO_SLUG) {
            slug = slug.substring(0, LARGO_SLUG).replaceAll("-[^-]*$", "");
        }
        return prefijo + item.codigo().toLowerCase(Locale.ROOT) + "-" + slug;
    }
}
