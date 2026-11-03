package com.team.gestorproyectos.historia.domain.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.team.gestorproyectos.historia.domain.model.CambioHistoria.BugDeDevolucion;
import com.team.gestorproyectos.shared.domain.exception.AccesoDenegadoException;
import com.team.gestorproyectos.shared.domain.exception.ReglaNegocioException;
import com.team.gestorproyectos.shared.domain.model.CodigoItem;
import com.team.gestorproyectos.shared.domain.model.FormaTrabajo;
import com.team.gestorproyectos.shared.domain.port.PermisosPort;

/**
 * Una historia con lo necesario para decidir quién puede moverla y si ya se puede (PDF pág. 21 y 33).
 * Iniciar planeación y Marcar como Lista, de cualquier Analista del proyecto o del Líder; Regresar a planeación y
 * Descartar, de su Analista o del Líder; Aceptar y Devolver, del Analista; Integrar, Mover de fase y Reabrir, del
 * Líder del proyecto. Con su primera Actividad pasa sola a Lista para desarrollo.
 * El Admin puede todo.
 *
 * @param completa      tiene Como / Quiero / Para
 * @param criterios     cuántos criterios de aceptación tiene
 * @param modulos       en cuántos módulos se trabaja
 * @param fasesDestino  otras fases del proyecto que todavía reciben historias
 * @param formaTrabajo  en la forma Normal no hay Integrar: aceptada ya queda integrada (su código ya está en develop)
 * @param requierePr    el proyecto pide PR: al terminar el desarrollo, el Líder la revisa antes de validarse
 * @param desarrolladores con alguno, la historia les llega directo (sin Actividades): la Inician y la Terminan ellos
 * @param trabajoPendiente puntos de "Lo que hice" sin marcar
 */
public record HistoriaParaGestionar(
        Long id,
        Long proyectoId,
        int numero,
        String titulo,
        String estado,
        Long analistaId,
        Long liderId,
        Long faseId,
        String faseEstado,
        boolean completa,
        int criterios,
        int modulos,
        List<TicketDeHistoria> tickets,
        List<FaseDestino> fasesDestino,
        FormaTrabajo formaTrabajo,
        boolean requierePr,
        List<DesarrolladorDeHistoria> desarrolladores,
        int trabajoPendiente) {

    /** Los estados de la fase en que se puede iniciar una historia o reabrirla. */
    private static final Set<String> FASE_RECIBE_HISTORIAS = Set.of("PLANEADA", "EN_CURSO", "PAUSADA");

    private static final Map<String, String> ESTADOS = Map.ofEntries(
            Map.entry("BORRADOR", "Borrador"),
            Map.entry("EN_PLANEACION", "En planeación"),
            Map.entry("LISTA", "Lista"),
            Map.entry("LISTA_DESARROLLO", "Lista para desarrollo"),
            Map.entry("EN_DESARROLLO", "En desarrollo"),
            Map.entry("EN_REVISION", "En revisión"),
            Map.entry("EN_VALIDACION", "En validación"),
            Map.entry("ACEPTADA", "Aceptada"),
            Map.entry("INTEGRADA", "Integrada"),
            Map.entry("CERRADA", "Cerrada"),
            Map.entry("DESCARTADA", "Descartada"));

    private static final int LARGO_TITULO_BUG = 200;

    /** Mientras se define: con tickets ya no se edita. */
    private static final Set<String> EDITABLE = Set.of("BORRADOR", "EN_PLANEACION", "LISTA");

    /** Las que su Analista o el Líder deciden por igual: el permiso lo tienen los dos. */
    private static final Set<AccionHistoria> ANALISTA_O_LIDER = Set.of(AccionHistoria.REGRESAR_A_PLANEACION,
            AccionHistoria.DESCARTAR);

    /** Mientras se define, cualquier Analista del proyecto (o Líder) la completa y la marca Lista. */
    private static final Set<AccionHistoria> DE_CUALQUIER_ANALISTA = Set.of(AccionHistoria.INICIAR_PLANEACION,
            AccionHistoria.MARCAR_LISTA);
    private static final Set<String> PLANEANDOSE = Set.of("BORRADOR", "EN_PLANEACION");

    /** Aún no se inicia: se puede eliminar si tampoco tiene actividades. */
    private static final Set<String> SIN_INICIAR = Set.of("BORRADOR", "EN_PLANEACION", "LISTA");

    /** Ya integrada o fuera: sus imágenes quedan como están. */
    private static final Set<String> SIN_IMAGENES_NUEVAS = Set.of("INTEGRADA", "CERRADA", "DESCARTADA");

    public record FaseDestino(Long id, String titulo) {
    }

    public HistoriaParaGestionar {
        tickets = List.copyOf(tickets);
        fasesDestino = List.copyOf(fasesDestino);
        desarrolladores = List.copyOf(desarrolladores);
    }

    /** Se trabaja sin Actividades: le llega directo a sus Desarrolladores. */
    public boolean trabajoDirecto() {
        return !desarrolladores.isEmpty();
    }

    public boolean esDesarrollador(Long usuarioId) {
        return desarrolladores.stream().anyMatch(d -> d.usuarioId().equals(usuarioId));
    }

    /** "Lo que hice": lo agregan sus Desarrolladores, su Analista o el Líder mientras no se acepta. */
    public boolean puedeAnotarTrabajo(QuienGestiona quien) {
        return trabajoDirecto() && !Set.of("ACEPTADA", "INTEGRADA", "CERRADA", "DESCARTADA").contains(estado)
                && (quien.esAdmin() || esDesarrollador(quien.id()) || quien.id().equals(analistaId)
                        || quien.id().equals(liderId));
    }

    /** Lo marcan como hecho sus Desarrolladores mientras la desarrollan. */
    public boolean puedeMarcarTrabajo(QuienGestiona quien) {
        return trabajoDirecto() && "EN_DESARROLLO".equals(estado)
                && (quien.esAdmin() || esDesarrollador(quien.id()));
    }

    /** Cada Desarrollador marca cuando terminó su parte, mientras está en desarrollo. */
    public void verificarMiParte(QuienGestiona quien) {
        if (!esDesarrollador(quien.id())) {
            throw new AccesoDenegadoException("Solo sus Desarrolladores marcan su parte");
        }
        if (!"EN_DESARROLLO".equals(estado)) {
            throw new ReglaNegocioException("La historia no está en desarrollo");
        }
    }

    /** Al terminar el desarrollo: si el proyecto pide PR, primero la revisa el Líder. */
    private boolean pasaPorRevision() {
        return requierePr || formaTrabajo == FormaTrabajo.GITFLOW;
    }

    public String codigo() {
        return CodigoItem.historia(numero);
    }

    /** Quién decide en este estado (PDF pág. 21): "Analista", "Líder" o "Nadie" si avanza sola. */
    public String actor() {
        if (trabajoDirecto()) {
            switch (estado) {
                case "LISTA_DESARROLLO", "EN_DESARROLLO" -> {
                    return "Desarrollador";
                }
                case "EN_REVISION" -> {
                    return "Líder";
                }
                default -> {
                    // Lo demás, igual que cualquier historia.
                }
            }
        }
        return switch (estado) {
            case "BORRADOR", "EN_PLANEACION", "EN_VALIDACION" -> "Analista";
            // Lista para desarrollo espera a que el Líder inicie la fase.
            case "LISTA", "LISTA_DESARROLLO", "ACEPTADA", "DESCARTADA" -> "Líder";
            default -> "Nadie";
        };
    }

    /** Las acciones de este estado que esta persona tiene (aunque aún les falte algo para poder hacerse). */
    public List<AccionHistoria> acciones(QuienGestiona quien) {
        return Arrays.stream(AccionHistoria.values())
                .filter(a -> a.aplicaEn(estado) && puede(quien, a))
                .filter(a -> a != AccionHistoria.INTEGRAR || formaTrabajo == FormaTrabajo.GITFLOW)
                .filter(a -> !a.deTrabajoDirecto() || trabajoDirecto())
                .toList();
    }

    public boolean puede(QuienGestiona quien, AccionHistoria accion) {
        if (accion.permiso() != null && !quien.tiene(accion.permiso())) {
            return false;
        }
        if (quien.esAdmin()) {
            return true;
        }
        if (accion.decide() == AccionHistoria.Decide.DESARROLLADOR) {
            return esDesarrollador(quien.id());
        }
        // El permiso solo lo tienen Analistas y Líderes, y quien ve la historia es del proyecto.
        if (DE_CUALQUIER_ANALISTA.contains(accion)) {
            return true;
        }
        boolean esAnalista = quien.id().equals(analistaId);
        boolean esLider = quien.id().equals(liderId);
        if (ANALISTA_O_LIDER.contains(accion)) {
            return esAnalista || esLider;
        }
        return switch (accion.decide()) {
            case ANALISTA -> esAnalista;
            case LIDER -> esLider;
            case DESARROLLADOR -> false;
        };
    }

    /** El Analista marca los criterios mientras la prueba en staging. */
    public boolean puedeVerificarCriterios(QuienGestiona quien) {
        return Set.of("EN_DESARROLLO", "EN_VALIDACION").contains(estado)
                && (quien.esAdmin() || quien.id().equals(analistaId));
    }

    /**
     * Corregir lo que se capturó (título, Como/Quiero/Para, criterios, módulos...): su Analista o el Líder,
     * mientras no tenga tickets; mientras se define, también cualquier Analista del proyecto. Con tickets el trabajo
     * ya se repartió sobre lo escrito.
     */
    public boolean puedeEditar(QuienGestiona quien) {
        boolean defineLa = PLANEANDOSE.contains(estado) && quien.tiene(PermisosPort.MARCAR_HISTORIA_LISTA);
        return EDITABLE.contains(estado) && tickets.stream().noneMatch(TicketDeHistoria::cuenta)
                && (quien.esAdmin() || quien.id().equals(analistaId) || quien.id().equals(liderId) || defineLa);
    }

    /** Sus imágenes las suben y quitan su Analista, el Líder o un Admin hasta que se integra. */
    public boolean puedeAdjuntar(QuienGestiona quien) {
        return !SIN_IMAGENES_NUEVAS.contains(estado)
                && (quien.esAdmin() || quien.id().equals(analistaId) || quien.id().equals(liderId));
    }

    /** Por qué no puede subir ni quitar imágenes, para el 403. */
    public String motivoSinImagenes() {
        return SIN_IMAGENES_NUEVAS.contains(estado) ? "La historia ya no recibe imágenes"
                : "Las imágenes las suben su Analista o el Líder del proyecto";
    }

    /**
     * Eliminarla del todo (no Descartar): solo el Líder del proyecto o un Admin, y solo si aún no se inicia.
     *
     * @param ticketsQueVienenDe tickets de Mantenimiento que la tienen como "Viene de"
     * @return por qué no se puede; vacío si ya se puede
     */
    public List<String> impedimentosParaEliminar(long ticketsQueVienenDe) {
        List<String> faltan = new ArrayList<>();
        if (!SIN_INICIAR.contains(estado)) {
            faltan.add("La historia ya se inició (" + ESTADOS.getOrDefault(estado, estado).toLowerCase(Locale.ROOT)
                    + "): descártala en lugar de eliminarla");
        }
        if (!tickets.isEmpty()) {
            faltan.add(tickets.size() == 1 ? "Ya tiene 1 actividad o ticket" : "Ya tiene " + tickets.size()
                    + " actividades o tickets");
        }
        if (ticketsQueVienenDe > 0) {
            faltan.add("Hay tickets de Mantenimiento que vienen de ella");
        }
        return faltan;
    }

    public boolean puedeEliminar(QuienGestiona quien) {
        return quien.esAdmin() || quien.id().equals(liderId);
    }

    /** Lanza el error si esta persona no puede eliminarla o si ya se inició. */
    public void verificarEliminar(QuienGestiona quien, long ticketsQueVienenDe) {
        if (!puedeEliminar(quien)) {
            throw new AccesoDenegadoException("La historia solo la eliminan el Líder del proyecto o un Admin");
        }
        impedimentosParaEliminar(ticketsQueVienenDe).stream().findFirst().ifPresent(falta -> {
            throw new ReglaNegocioException(falta);
        });
    }

    /** Por qué no puede editarla, para el 403. */
    public String motivoSinEdicion() {
        if (!EDITABLE.contains(estado)) {
            return "La historia ya está " + ESTADOS.getOrDefault(estado, estado).toLowerCase(Locale.ROOT)
                    + ": ya no se edita";
        }
        if (tickets.stream().anyMatch(TicketDeHistoria::cuenta)) {
            return "La historia ya tiene tickets: ya no se edita";
        }
        return "La historia la edita su Analista o el Líder del proyecto";
    }

    /** Lo que todavía impide hacer la acción, en palabras para el panel. Vacío si ya se puede. */
    public List<String> impedimentos(AccionHistoria accion) {
        List<String> faltan = new ArrayList<>();
        switch (accion) {
            case MARCAR_LISTA -> {
                if (!completa) {
                    faltan.add("Falta completar Como / Quiero / Para");
                }
                if (criterios == 0) {
                    faltan.add("Falta al menos un criterio de aceptación");
                }
                if (modulos == 0) {
                    faltan.add("Falta elegir al menos un repositorio");
                }
                // La historia le llega directo a sus Desarrolladores (ya no hay Actividades).
                if (!trabajoDirecto()) {
                    faltan.add("Falta asignar al menos un Desarrollador");
                }
            }
            case INICIAR_DESARROLLO -> {
                if (!"EN_CURSO".equals(faseEstado)) {
                    faltan.add("Su fase aún no está en curso");
                }
            }
            case TERMINAR_DESARROLLO -> {
                desarrolladores.stream().filter(d -> !d.terminado())
                        .forEach(d -> faltan.add("Falta que " + d.nombre() + " marque su parte como terminada"));
                if (trabajoPendiente > 0) {
                    faltan.add(trabajoPendiente == 1 ? "Hay 1 punto de \"Lo que hice\" sin marcar"
                            : "Hay " + trabajoPendiente + " puntos de \"Lo que hice\" sin marcar");
                }
            }
            case DESCARTAR, REGRESAR_A_PLANEACION -> {
                long empezados = tickets.stream().filter(TicketDeHistoria::empezado).count();
                if (empezados > 0) {
                    faltan.add(empezados == 1 ? "Ya hay 1 ticket iniciado" : "Ya hay " + empezados + " tickets iniciados");
                }
            }
            case MOVER_DE_FASE -> {
                // Una fase que ya terminó (o que está saliendo a producción) no suelta historias ni recibe otras.
                if (!FASE_RECIBE_HISTORIAS.contains(faseEstado)) {
                    faltan.add("Su fase ya terminó o está en release: sus historias ya no se mueven");
                } else if (fasesDestino.isEmpty()) {
                    faltan.add("No hay otra fase que reciba historias");
                }
            }
            case DEVOLVER -> {
                // Siempre se puede: si ningún ticket se puede reabrir, se crea un bug.
            }
            case REABRIR -> {
                if (!FASE_RECIBE_HISTORIAS.contains(faseEstado)) {
                    faltan.add("Su fase ya no recibe historias");
                }
            }
            default -> {
                // Aceptar e Integrar no esperan nada más que el estado.
            }
        }
        return faltan;
    }

    /** Tickets que se cancelarían al descartarla: los que nadie ha empezado. */
    public List<TicketDeHistoria> ticketsSinEmpezar() {
        return tickets.stream().filter(t -> t.cuenta() && !t.empezado()).toList();
    }

    public List<TicketDeHistoria> ticketsReabribles() {
        return tickets.stream().filter(TicketDeHistoria::reabrible).toList();
    }

    public CambioHistoria ejecutar(QuienGestiona quien, AccionHistoria accion, DatosAccionHistoria datos) {
        if (!estado.equals(datos.estadoEsperado())) {
            throw new ReglaNegocioException(codigo() + " ya cambió de estado. Recarga para ver cómo está ahora.");
        }
        if (!accion.aplicaEn(estado)) {
            throw new ReglaNegocioException("\"" + accion.texto() + "\" no se puede hacer con la historia "
                    + ESTADOS.getOrDefault(estado, estado));
        }
        if (accion == AccionHistoria.INTEGRAR && formaTrabajo == FormaTrabajo.NORMAL) {
            throw new ReglaNegocioException("En este proyecto no se integra a develop: la historia se integra al aceptarla");
        }
        if (!puede(quien, accion)) {
            throw new AccesoDenegadoException("\"" + accion.texto() + "\" le toca al " + switch (accion.decide()) {
                case ANALISTA -> "Analista de la historia";
                case LIDER -> "Líder del proyecto";
                case DESARROLLADOR -> "Desarrollador de la historia";
            });
        }
        impedimentos(accion).stream().findFirst().ifPresent(falta -> {
            throw new ReglaNegocioException(falta);
        });

        String motivo = datos.motivo() == null || datos.motivo().isBlank() ? null : datos.motivo().trim();
        if (accion.pideMotivo() && motivo == null) {
            throw new ReglaNegocioException("Escribe el motivo");
        }

        Long ticketAReabrir = null;
        String estadoAlReabrir = null;
        BugDeDevolucion bug = null;
        // Con Desarrolladores no hay ticket que reabrir: regresa a ellos con el motivo.
        if (accion.regresaADesarrollo() && !trabajoDirecto()) {
            if (datos.crearBug()) {
                bug = bugDeDevolucion(motivo);
            } else {
                TicketDeHistoria ticket = ticketsReabribles().stream()
                        .filter(t -> Objects.equals(t.id(), datos.ticketId())).findFirst()
                        .orElseThrow(() -> new ReglaNegocioException(datos.ticketId() == null
                                ? "Elige el ticket a reabrir o crea un bug"
                                : "Ese ticket no se puede reabrir: no es de la historia o aún no se aprueba"));
                ticketAReabrir = ticket.id();
                estadoAlReabrir = ticket.estadoAlReabrir();
            }
        }

        Long faseDestinoId = null;
        if (accion == AccionHistoria.MOVER_DE_FASE) {
            faseDestinoId = fasesDestino.stream().map(FaseDestino::id)
                    .filter(id -> id.equals(datos.faseDestinoId())).findFirst()
                    .orElseThrow(() -> new ReglaNegocioException("Elige una fase del proyecto que reciba historias"));
        }

        String estadoNuevo = accion.destino() == null ? estado : accion.destino();
        if (accion == AccionHistoria.TERMINAR_DESARROLLO && pasaPorRevision()) {
            estadoNuevo = "EN_REVISION";
        }
        // Igual que cuando se marca Lista su Actividad: si ya tiene una, no se queda en Lista.
        String avanzaSolaA = accion == AccionHistoria.MARCAR_LISTA && tieneActividadLista()
                ? ("EN_CURSO".equals(faseEstado) ? "EN_DESARROLLO" : "LISTA_DESARROLLO")
                : null;
        // Con Desarrolladores ya está lista para ellos: la inician cuando su fase corre.
        if (accion == AccionHistoria.MARCAR_LISTA && trabajoDirecto()) {
            avanzaSolaA = "LISTA_DESARROLLO";
        }
        // En la forma Normal todo sube directo a develop: al aceptarla queda integrada sin que el Líder haga nada.
        if (accion == AccionHistoria.ACEPTAR && formaTrabajo == FormaTrabajo.NORMAL) {
            avanzaSolaA = "INTEGRADA";
        }
        return new CambioHistoria(id, proyectoId, faseId, accion, estado, estadoNuevo, motivo, ticketAReabrir,
                estadoAlReabrir, bug, faseDestinoId, avanzaSolaA);
    }

    /** Alguna Actividad ya salió de Borrador (el Líder la marcó Lista) y no está cancelada. */
    public boolean tieneActividadLista() {
        return tickets.stream().anyMatch(t -> "ACTV".equals(t.tipo()) && t.cuenta() && !"BORRADOR".equals(t.estado()));
    }

    /**
     * Valida que se pueda marcar o desmarcar un criterio de aceptación (PDF pág. 24).
     *
     * @param criterioIds los criterios que tiene la historia
     */
    public void verificarCriterio(QuienGestiona quien, Long criterioId, List<Long> criterioIds) {
        if (!puedeVerificarCriterios(quien)) {
            throw new AccesoDenegadoException(Set.of("EN_DESARROLLO", "EN_VALIDACION").contains(estado)
                    ? "Los criterios los verifica el Analista de la historia"
                    : "Los criterios se verifican mientras la historia está en desarrollo o en validación");
        }
        if (!criterioIds.contains(criterioId)) {
            throw new ReglaNegocioException("Ese criterio no es de esta historia");
        }
    }

    /**
     * El bug que nace al devolver por algo que no cubría ningún ticket. Va con quien más tickets
     * trabaja en la historia; como sus demás tickets, lo revisa el Líder en su PR y vuelve a validarse con ella.
     */
    private BugDeDevolucion bugDeDevolucion(String motivo) {
        Long responsable = masFrecuente(TicketDeHistoria::responsableId)
                .orElseThrow(() -> new ReglaNegocioException("La historia no tiene Desarrollador a quien asignar el bug"));
        String titulo = motivo.length() > LARGO_TITULO_BUG ? motivo.substring(0, LARGO_TITULO_BUG - 1) + "…" : motivo;
        return new BugDeDevolucion(titulo, "Encontrado al validar " + codigo() + ": " + motivo, responsable,
                null);
    }

    private Optional<Long> masFrecuente(Function<TicketDeHistoria, Long> persona) {
        return tickets.stream().filter(TicketDeHistoria::cuenta).map(persona).filter(Objects::nonNull)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet().stream().max(Map.Entry.comparingByValue(Comparator.naturalOrder()))
                .map(Map.Entry::getKey);
    }
}
