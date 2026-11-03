package com.team.gestorproyectos.proyecto.domain.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.team.gestorproyectos.proyecto.domain.port.out.ProyectoConsultaPort.HistoriaBase;
import com.team.gestorproyectos.proyecto.domain.port.out.ProyectoConsultaPort.TicketBase;
import com.team.gestorproyectos.shared.domain.exception.ReglaNegocioException;
import com.team.gestorproyectos.shared.domain.model.ConteoTickets;
import com.team.gestorproyectos.shared.domain.model.FlujoTrabajo;
import com.team.gestorproyectos.shared.domain.model.ItemTrabajo;
import com.team.gestorproyectos.shared.domain.model.CodigoItem;

/**
 * Quién trabaja en el proyecto, con qué rol y cuánta carga tiene (PDF pág. 34, pestaña Equipo).
 * Los roles salen de cada usuario; aquí se ve cómo se usan en el proyecto.
 *
 * @param cobertura cada rol del flujo y quién lo cubre
 * @param alertas   avisos de carga: alguien con muchos tickets, sobre todo si es el único de su rol
 */
public record EquipoProyecto(Long liderId, List<MiembroEquipo> miembros, List<Cobertura> cobertura,
                             List<String> alertas) {

    /** Con esto o más tickets abiertos, la carga es alta. */
    static final int CARGA_ALTA = 7;
    /** Con esto o más, media. */
    static final int CARGA_MEDIA = 4;

    /** Las historias en estos estados ya no esperan a nadie. */
    private static final Set<String> HISTORIA_CERRADA = Set.of("INTEGRADA", "CERRADA", "DESCARTADA");

    public enum Carga { BAJA, MEDIA, ALTA }

    /** OK: alguien lo cubre; AVISO: solo una persona y ya va cargada; FALTA: nadie. */
    public enum Resultado { OK, AVISO, FALTA }

    /** Alguien del equipo como viene de la base. @param roles ADMIN, LIDER, ANALISTA, DESARROLLADOR */
    public record Persona(Long id, String nombre, String apellidos, List<String> roles, LocalDateTime ultimoAcceso) {

        public String nombreCompleto() {
            return apellidos == null || apellidos.isBlank() ? nombre : nombre + " " + apellidos.trim();
        }

        /** "Ana López": nombre y primer apellido. */
        public String nombreCorto() {
            return apellidos == null || apellidos.isBlank() ? nombre : nombre + " " + apellidos.trim().split("\\s+")[0];
        }

        public String iniciales() {
            return (nombre.substring(0, 1) + (apellidos == null || apellidos.isBlank() ? "" : apellidos.trim().substring(0, 1)))
                    .toUpperCase();
        }

        boolean tiene(String rol) {
            return roles.contains(rol);
        }
    }

    /**
     * @param modulos         los módulos donde tiene tickets abiertos
     * @param ticketsAbiertos los que trabaja o prueba y no se han terminado
     * @param leToca          tickets e historias del proyecto que esperan su acción
     */
    public record MiembroEquipo(Persona persona, boolean esLider, List<TipoModulo> modulos, int ticketsAbiertos,
                                Carga carga, int leToca) {
    }

    /** @param quien "Ana", "Ana y Luis", "Solo Pedro" o "Nadie" */
    public record Cobertura(String rol, Resultado resultado, String quien) {
    }

    /** Qué tickets cambian de manos al reasignar los de alguien que se ausenta. */
    public record Reasignacion(Long deId, Long responsableNuevoId, List<Long> comoResponsable, Long testerNuevoId,
                               List<Long> comoTester) {
    }

    public static EquipoProyecto armar(Long liderId, List<Persona> personas, List<TicketBase> tickets,
                                       List<HistoriaBase> historias) {
        List<TicketBase> abiertos = abiertos(tickets);
        List<ItemTrabajo> pendientes = new ArrayList<>(abiertos.stream().map(t -> t.item().item()).toList());
        historias.stream().filter(h -> !HISTORIA_CERRADA.contains(h.estado()))
                .map(h -> new ItemTrabajo(h.id(), "HIST", h.numero(), h.titulo(), h.estado(), null, h.analistaId(),
                        null, null, null, null, null, true))
                .forEach(pendientes::add);

        List<MiembroEquipo> miembros = personas.stream().map(p -> {
            List<TicketBase> suyos = abiertos.stream().filter(participa(p.id())).toList();
            int leToca = (int) pendientes.stream()
                    .filter(i -> FlujoTrabajo.accionPendiente(i)
                            .map(a -> p.id().equals(FlujoTrabajo.quienActua(i, a.actor(), liderId))).orElse(false))
                    .count();
            return new MiembroEquipo(p, p.id().equals(liderId),
                    suyos.stream().map(TicketBase::modulo).filter(Objects::nonNull).distinct().sorted().toList(),
                    suyos.size(), carga(suyos.size()), leToca);
        }).sorted(Comparator.comparing((MiembroEquipo m) -> !m.esLider())
                .thenComparing(m -> m.persona().nombre())).toList();

        List<Persona> desarrolladores = personas.stream().filter(p -> p.tiene("DESARROLLADOR")).toList();
        List<String> alertas = miembros.stream().filter(m -> m.carga() == Carga.ALTA).map(m -> {
            boolean unico = desarrolladores.size() == 1 && desarrolladores.get(0).id().equals(m.persona().id());
            return m.persona().nombre() + " tiene " + m.ticketsAbiertos() + " tickets abiertos"
                    + (unico ? " y es el único desarrollador" : "")
                    + ". Si se ausenta, usa “Reasignar tickets” desde su menú.";
        }).toList();

        return new EquipoProyecto(liderId, miembros, cobertura(liderId, personas), alertas);
    }

    /** Lo que impide quitar a alguien del proyecto: ser su Líder o tener trabajo abierto. */
    public List<String> impedimentosParaQuitar(Long personaId, List<TicketBase> tickets, List<HistoriaBase> historias) {
        List<String> faltan = new ArrayList<>();
        if (personaId.equals(liderId)) {
            faltan.add("Es el Líder del proyecto: cambia el Líder al editar el proyecto");
        }
        long abiertos = abiertos(tickets).stream().filter(participa(personaId)).count();
        if (abiertos > 0) {
            faltan.add(abiertos == 1 ? "Tiene 1 ticket abierto: reasígnalo primero"
                    : "Tiene " + abiertos + " tickets abiertos: reasígnalos primero");
        }
        String analista = historias.stream()
                .filter(h -> personaId.equals(h.analistaId()) && !HISTORIA_CERRADA.contains(h.estado()))
                .map(h -> CodigoItem.historia(h.numero())).collect(Collectors.joining(", "));
        if (!analista.isEmpty()) {
            faltan.add("Es Analista de " + analista);
        }
        return faltan;
    }

    /**
     * Valida y arma la reasignación de los tickets abiertos de alguien: los que trabaja pasan a
     * {@code responsableNuevoId} y los que prueba, a {@code testerNuevoId} (cualquiera puede ser null si no se cambia).
     */
    public Reasignacion reasignar(Long deId, Long responsableNuevoId, Long testerNuevoId, List<TicketBase> tickets) {
        if (!esMiembro(deId)) {
            throw new ReglaNegocioException("Esa persona no es del equipo del proyecto");
        }
        for (Long nuevo : new Long[] {responsableNuevoId, testerNuevoId}) {
            if (nuevo != null && (nuevo.equals(deId) || !esMiembro(nuevo))) {
                throw new ReglaNegocioException("Elige a otra persona del equipo del proyecto");
            }
        }
        List<TicketBase> abiertos = abiertos(tickets);
        List<Long> comoResponsable = responsableNuevoId == null ? List.of()
                : abiertos.stream().filter(t -> deId.equals(t.item().item().responsableId()))
                        .map(t -> t.item().item().id()).toList();
        List<Long> comoTester = testerNuevoId == null ? List.of()
                : abiertos.stream().filter(t -> deId.equals(t.item().item().testerId()))
                        .map(t -> t.item().item().id()).toList();
        if (comoResponsable.isEmpty() && comoTester.isEmpty()) {
            throw new ReglaNegocioException("No hay tickets abiertos que reasignar");
        }
        return new Reasignacion(deId, responsableNuevoId, comoResponsable, testerNuevoId, comoTester);
    }

    public boolean esMiembro(Long personaId) {
        return miembros.stream().anyMatch(m -> m.persona().id().equals(personaId));
    }

    static Carga carga(int abiertos) {
        return abiertos >= CARGA_ALTA ? Carga.ALTA : abiertos >= CARGA_MEDIA ? Carga.MEDIA : Carga.BAJA;
    }

    /** Líder, Analista y Desarrollador: quién los cubre en el proyecto. */
    private static List<Cobertura> cobertura(Long liderId, List<Persona> personas) {
        List<Cobertura> roles = new ArrayList<>();
        personas.stream().filter(p -> p.id().equals(liderId)).findFirst()
                .ifPresentOrElse(p -> roles.add(new Cobertura("Líder", Resultado.OK, p.nombre())),
                        () -> roles.add(new Cobertura("Líder", Resultado.FALTA, "Nadie")));
        roles.add(cubre("Analista", personas, "ANALISTA", false));
        roles.add(cubre("Desarrollador", personas, "DESARROLLADOR", true));
        return roles;
    }

    /**
     * @param avisaSiEsUno con un solo Desarrollador se avisa: lleva casi todo el trabajo y no hay quien lo supla
     */
    private static Cobertura cubre(String nombre, List<Persona> personas, String rol, boolean avisaSiEsUno) {
        List<Persona> con = personas.stream().filter(p -> p.tiene(rol)).toList();
        if (con.isEmpty()) {
            return new Cobertura(nombre, Resultado.FALTA, "Nadie");
        }
        if (con.size() == 1) {
            String unica = con.get(0).nombre();
            return avisaSiEsUno ? new Cobertura(nombre, Resultado.AVISO, "Solo " + unica)
                    : new Cobertura(nombre, Resultado.OK, unica);
        }
        List<String> nombres = con.stream().map(Persona::nombre).toList();
        return new Cobertura(nombre, Resultado.OK, String.join(", ", nombres.subList(0, nombres.size() - 1))
                + " y " + nombres.get(nombres.size() - 1));
    }

    /** Tickets que aún no se terminan (aprobado en QA o más adelante ya no cuenta como carga). */
    private static List<TicketBase> abiertos(List<TicketBase> tickets) {
        return tickets.stream().filter(t -> {
            ConteoTickets c = new ConteoTickets(null, null, t.item().item().estado(), 1);
            return c.cuenta() && !c.terminado();
        }).toList();
    }

    private static Predicate<TicketBase> participa(Long personaId) {
        return t -> personaId.equals(t.item().item().responsableId()) || personaId.equals(t.item().item().testerId());
    }
}
