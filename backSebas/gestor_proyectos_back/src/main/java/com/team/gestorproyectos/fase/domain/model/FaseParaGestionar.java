package com.team.gestorproyectos.fase.domain.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import com.team.gestorproyectos.shared.domain.exception.AccesoDenegadoException;
import com.team.gestorproyectos.shared.domain.exception.ReglaNegocioException;
import com.team.gestorproyectos.shared.domain.port.PermisosPort;

/**
 * Una fase con lo necesario para decidir quién puede moverla y si ya se puede (PDF pág. 20).
 * Las acciones son del Líder del proyecto o del responsable de la fase; el Admin puede todo
 * y es el único que reabre una fase cancelada.
 *
 * @param otraFaseActiva  "Fase 4 · Portal móvil" si otra ocupa el turno; una fase a la vez
 * @param historias       las historias que tiene (sin las descartadas); sin ninguna no se puede iniciar
 * @param listas          de esas, las Listas para desarrollo (con sus Actividades); con al menos una ya se inicia
 *                        y las demás arrancan solas al terminar de planearse
 * @param tickets         sus tickets vivos (sin cancelados ni rechazados); con alguno ya no se edita
 * @param versionAnterior la última publicada antes de esta, a la que vuelve producción al revertir
 * @param ultimaPublicada si es la versión que está hoy en producción; solo esa se puede revertir
 * @param bugReversion    el BUGS que se creó al revertirla; null si nunca se revirtió
 * @param equipo          ids de quienes pueden recibir el BUGS de la reversión
 */
public record FaseParaGestionar(
        Long id,
        Long proyectoId,
        int numero,
        String nombre,
        String estado,
        boolean mantenimiento,
        Long liderId,
        Long responsableId,
        String versionRelease,
        String versionAnterior,
        boolean ultimaPublicada,
        String otraFaseActiva,
        long historias,
        long listas,
        long tickets,
        List<CriterioCierre> criterios,
        BugReversion bugReversion,
        Set<Long> equipo) {

    /** Estados en que se pueden marcar los criterios manuales y corregir los datos de la fase. */
    private static final Set<String> CRITERIOS_ABIERTOS = Set.of("PLANEADA", "EN_CURSO", "PAUSADA");

    private static final Map<String, String> ESTADOS = Map.of(
            "PLANEADA", "Planeada",
            "EN_CURSO", "En curso",
            "PAUSADA", "Pausada",
            "LISTA_PARA_RELEASE", "Lista para release",
            "EN_PUBLICACION", "En publicación",
            "PUBLICADA", "Publicada",
            "REVERTIDA", "Revertida",
            "CANCELADA", "Cancelada");

    public record BugReversion(Long id, String codigo, boolean abierto) {
    }

    public FaseParaGestionar {
        criterios = List.copyOf(criterios);
        equipo = Set.copyOf(equipo);
    }

    public String titulo() {
        return "Fase " + numero + " · " + nombre;
    }

    /** Las historias que aún no se inician las eliminan el Líder del proyecto o un Admin, como la fase. */
    public boolean puedeEliminarHistorias(QuienGestiona quien) {
        return quien.esAdmin() || quien.id().equals(liderId);
    }

    /** Eliminarla del todo (no Cancelar): solo el Líder del proyecto o un Admin. */
    public boolean puedeEliminar(QuienGestiona quien) {
        return !mantenimiento && (quien.esAdmin() || quien.id().equals(liderId));
    }

    /**
     * Se elimina con sus historias solo si aún no empieza nada: Planeada, sin historias iniciadas, sin tickets y sin
     * solicitudes, demos ni releases.
     *
     * @return por qué no se puede; vacío si ya se puede
     */
    public List<String> impedimentosParaEliminar(BloqueosEliminarFase bloqueos) {
        List<String> faltan = new ArrayList<>();
        if (mantenimiento) {
            faltan.add("Mantenimiento no se elimina");
            return faltan;
        }
        if (!"PLANEADA".equals(estado)) {
            faltan.add("La fase ya está " + ESTADOS.getOrDefault(estado, estado).toLowerCase(Locale.ROOT)
                    + ": cancélala en lugar de eliminarla");
        }
        if (bloqueos.historiasIniciadas() > 0) {
            faltan.add(bloqueos.historiasIniciadas() == 1 ? "1 historia ya se inició"
                    : bloqueos.historiasIniciadas() + " historias ya se iniciaron");
        }
        if (bloqueos.tickets() > 0) {
            faltan.add(bloqueos.tickets() == 1 ? "Ya tiene 1 ticket" : "Ya tiene " + bloqueos.tickets() + " tickets");
        }
        if (bloqueos.otros() > 0) {
            faltan.add("Tiene solicitudes, demos o releases");
        }
        return faltan;
    }

    /** Lanza el error si esta persona no puede eliminarla o si ya empezó algo. */
    public void verificarEliminar(QuienGestiona quien, BloqueosEliminarFase bloqueos) {
        if (!puedeEliminar(quien)) {
            throw new AccesoDenegadoException(mantenimiento ? "Mantenimiento no se elimina"
                    : "La fase solo la eliminan el Líder del proyecto o un Admin");
        }
        impedimentosParaEliminar(bloqueos).stream().findFirst().ifPresent(falta -> {
            throw new ReglaNegocioException(falta);
        });
    }

    /** Las acciones de este estado que esta persona tiene (aunque aún les falte algo para poder hacerse). */
    public List<AccionFase> acciones(QuienGestiona quien) {
        if (mantenimiento) {
            return List.of();
        }
        return Arrays.stream(AccionFase.values())
                .filter(a -> a.aplicaEn(estado) && puede(quien, a))
                .toList();
    }

    public boolean puede(QuienGestiona quien, AccionFase accion) {
        if (!quien.tiene(accion.permiso())) {
            return false;
        }
        // Reabrir es exclusivo de quien tiene el permiso (el Admin); no depende del proyecto.
        // Lo demás (iniciar, pausar, cerrar, publicar...) lo decide el Líder o el responsable, no el Admin.
        return accion == AccionFase.REABRIR || lideraLaFase(quien);
    }

    /**
     * Corregir lo que se capturó en Nueva fase (nombre, fechas, módulos...): el Líder o el responsable,
     * mientras la fase no tenga tickets. Con tickets el trabajo ya se repartió sobre lo decidido.
     */
    public boolean puedeEditar(QuienGestiona quien) {
        return !mantenimiento && CRITERIOS_ABIERTOS.contains(estado) && tickets == 0
                && quien.tiene(PermisosPort.GESTIONAR_FASES) && esLiderDeLaFase(quien);
    }

    /** Por qué no puede editarla, para el 403. */
    public String motivoSinEdicion() {
        if (mantenimiento) {
            return "Mantenimiento no se edita";
        }
        if (!CRITERIOS_ABIERTOS.contains(estado)) {
            return "Con la fase " + ESTADOS.getOrDefault(estado, estado) + " ya no se editan sus datos";
        }
        return tickets > 0 ? "La fase ya tiene tickets: ya no se edita"
                : "La fase la edita el Líder del proyecto o su responsable";
    }

    public boolean puedeMarcarCriterios(QuienGestiona quien) {
        return !mantenimiento && CRITERIOS_ABIERTOS.contains(estado)
                && quien.tiene(PermisosPort.MARCAR_CRITERIOS) && esLiderDeLaFase(quien);
    }

    /**
     * Las notas del release las edita quien publica (PDF pág. 27), mientras la fase no se haya publicado.
     */
    public boolean puedeEditarNotas(QuienGestiona quien) {
        return versionRelease != null && !mantenimiento && !Set.of("PUBLICADA", "CANCELADA").contains(estado)
                && puede(quien, AccionFase.SUBIR_A_MAIN);
    }

    /** Valida y limpia las notas del release antes de guardarlas. */
    public String notasParaGuardar(QuienGestiona quien, String notas) {
        if (!puedeEditarNotas(quien)) {
            throw new AccesoDenegadoException(versionRelease == null
                    ? "La fase no tiene versión de release"
                    : "Las notas las edita el Líder del proyecto mientras la fase no se publica");
        }
        String limpias = notas == null ? "" : notas.strip();
        if (limpias.isEmpty()) {
            throw new ReglaNegocioException("Escribe las notas del release");
        }
        if (limpias.length() > NotasRelease.LARGO_MAXIMO) {
            throw new ReglaNegocioException("Las notas son muy largas (máximo " + NotasRelease.LARGO_MAXIMO + " letras)");
        }
        return limpias;
    }

    /** Lo que todavía impide hacer la acción, en palabras para el panel. Vacío si ya se puede. */
    public List<String> impedimentos(AccionFase accion) {
        return switch (accion) {
            case INICIAR -> Stream.of(
                            otraFaseActiva == null ? null : "Primero debe terminar la " + otraFaseActiva,
                            historias > 0 ? null : "Agrega al menos una historia de usuario antes de iniciarla",
                            historias == 0 || listas > 0 ? null
                                    : "Al menos una historia debe estar Lista para desarrollo (con sus Actividades)")
                    .filter(Objects::nonNull).toList();
            case CERRAR -> {
                long faltan = criteriosFaltantes();
                yield faltan == 0 ? List.of()
                        : List.of("Para cerrar " + (faltan == 1 ? "falta 1 criterio" : "faltan " + faltan + " criterios"));
            }
            case SUBIR_A_MAIN -> versionRelease == null ? List.of("La fase no tiene versión de release") : List.of();
            case REVERTIR -> ultimaPublicada ? List.of()
                    : List.of("Solo se revierte la versión que está hoy en producción");
            case PUBLICAR_DE_NUEVO -> bugReversion != null && bugReversion.abierto()
                    ? List.of(bugReversion.codigo() + " sigue abierto") : List.of();
            default -> List.of();
        };
    }

    public long criteriosFaltantes() {
        return criterios.stream().filter(c -> !c.cumplido()).count();
    }

    public CambioFase ejecutar(QuienGestiona quien, AccionFase accion, DatosAccionFase datos, LocalDate hoy) {
        if (mantenimiento) {
            throw new ReglaNegocioException("Mantenimiento es permanente: no cambia de estado");
        }
        if (!estado.equals(datos.estadoEsperado())) {
            throw new ReglaNegocioException("La " + titulo() + " ya cambió de estado. Recarga para ver cómo está ahora.");
        }
        if (!accion.aplicaEn(estado)) {
            throw new ReglaNegocioException("\"" + accion.texto() + "\" no se puede hacer con la fase "
                    + ESTADOS.getOrDefault(estado, estado));
        }
        if (!puede(quien, accion)) {
            throw new AccesoDenegadoException(accion == AccionFase.REABRIR
                    ? "Solo el Admin puede reabrir una fase cancelada"
                    : "\"" + accion.texto() + "\" le toca al Líder del proyecto o al responsable de la fase");
        }
        impedimentos(accion).stream().findFirst().ifPresent(falta -> {
            throw new ReglaNegocioException(falta);
        });

        String motivo = datos.motivo() == null || datos.motivo().isBlank() ? null : datos.motivo().trim();
        if (accion.pideMotivo() && motivo == null) {
            throw new ReglaNegocioException(accion == AccionFase.REVERTIR ? "Escribe qué falló" : "Escribe el motivo");
        }

        LocalDate reanudarEl = accion == AccionFase.PAUSAR ? datos.reanudarEl() : null;
        if (reanudarEl != null && !reanudarEl.isAfter(hoy)) {
            throw new ReglaNegocioException("La fecha para reanudar debe ser después de hoy");
        }

        if (accion == AccionFase.SUBIR_A_MAIN) {
            String escrita = datos.versionConfirmada() == null ? "" : datos.versionConfirmada().trim();
            if (!versionRelease.equals(escrita)) {
                throw new ReglaNegocioException("Escribe " + versionRelease + " para confirmar");
            }
        }

        Long asignarBugA = null;
        if (accion == AccionFase.REVERTIR) {
            asignarBugA = datos.asignarBugA();
            if (asignarBugA == null) {
                throw new ReglaNegocioException("Elige a quién se asigna el bug");
            }
            if (!equipo.contains(asignarBugA)) {
                throw new ReglaNegocioException("El bug solo se asigna a alguien del equipo del proyecto");
            }
        }

        return new CambioFase(id, proyectoId, accion, estado, accion.destino(), motivo, reanudarEl, versionRelease,
                asignarBugA);
    }

    /**
     * Valida que se pueda marcar el criterio manual y dice si, con él, la fase queda lista para release
     * (la fase "pasa sola" cuando todo se cumple, PDF pág. 20).
     */
    public boolean marcarCriterio(QuienGestiona quien, Long criterioId) {
        if (!puedeMarcarCriterios(quien)) {
            throw new AccesoDenegadoException(CRITERIOS_ABIERTOS.contains(estado)
                    ? "Los criterios los marca el Líder del proyecto o el responsable de la fase"
                    : "Con la fase " + ESTADOS.getOrDefault(estado, estado) + " ya no se marcan criterios");
        }
        CriterioCierre criterio = criterios.stream().filter(c -> Objects.equals(c.id(), criterioId)).findFirst()
                .orElseThrow(() -> new ReglaNegocioException("Ese criterio no es de esta fase"));
        if (!criterio.manual()) {
            throw new ReglaNegocioException("Ese criterio lo revisa el sistema solo");
        }
        if (criterio.cumplido()) {
            throw new ReglaNegocioException("Ese criterio ya está cumplido");
        }
        return "EN_CURSO".equals(estado) && criteriosFaltantes() == 1;
    }

    private boolean esLiderDeLaFase(QuienGestiona quien) {
        return quien.esAdmin() || lideraLaFase(quien);
    }

    /** El Líder del proyecto o el responsable de la fase. */
    private boolean lideraLaFase(QuienGestiona quien) {
        return quien.id().equals(liderId) || quien.id().equals(responsableId);
    }
}
