package com.team.gestorproyectos.proyecto.domain.model;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import com.team.gestorproyectos.shared.domain.model.ConteoTickets;

/**
 * La pestaña Módulos del proyecto (PDF pág. 35): cada parte del sistema con su tecnología, repositorio,
 * ambientes, último build y tickets abiertos.
 *
 * Los ambientes siguen la misma regla que Integrar historia (pág. 28): develop y staging llevan la versión
 * candidata de la fase en curso ("1.3.0-rc2", una rc por cada historia integrada que tocó el módulo) y
 * producción, lo último publicado.
 *
 * @param faltan         los tipos de módulo que el proyecto aún no tiene ("¿Falta una parte del sistema?")
 * @param puedeGestionar el Líder del proyecto o un Admin: agrega, edita y quita módulos
 */
public record ModulosProyecto(List<VistaModulo> modulos, List<TipoModulo> faltan, boolean puedeGestionar) {

    private static final Set<String> POR_HACER = Set.of("BORRADOR", "POR_HACER", "REPORTADO", "CONFIRMADO", "PROPUESTA");
    private static final Set<String> EN_PROGRESO = Set.of("EN_PROGRESO", "EN_CORRECCION", "INVESTIGANDO", "REDACTANDO",
            "BLOQUEADA");
    private static final Set<String> EN_QA = Set.of("EN_QA", "EN_REVISION", "CONCLUSIONES");

    /**
     * @param tecnologias    "Java 21 · Spring Boot 3" separado en chips
     * @param ultimoBuild    el CI del PR más reciente; null si nunca ha corrido
     * @param bugsProduccion bugs abiertos del módulo en la fase de Mantenimiento
     * @param conTrabajo     ya tiene tickets, fases o PR: no se puede quitar
     */
    public record VistaModulo(ModuloProyecto modulo, List<String> tecnologias, Responsable responsable,
                              List<Ambiente> ambientes, Build ultimoBuild, int porHacer, int enProgreso, int enQa,
                              int bugsProduccion, boolean conTrabajo) {
    }

    public record Responsable(Long id, String nombre) {
    }

    /** @param version null si aún no hay nada de la fase en ese ambiente */
    public record Ambiente(String nombre, String version, LocalDateTime fecha) {
    }

    /** @param estado PASO, FALLO o PENDIENTE */
    public record Build(String estado, LocalDateTime fecha) {
    }

    /** Lo que se sabe de un módulo para armar su tarjeta. */
    public record DatosModulo(ModuloConCi modulo, Responsable responsable, List<ConteoTickets> tickets,
                              List<ConteoTickets> mantenimiento, int historiasIntegradas, LocalDateTime ultimaIntegracion,
                              boolean conTrabajo) {
    }

    /**
     * @param versionFase  la versión del release de la fase en curso ("v1.3.0"); null si no hay fase en curso
     * @param produccion   la última versión publicada y cuándo; null si nunca se ha publicado
     */
    public static ModulosProyecto armar(List<DatosModulo> datos, String versionFase, Ambiente produccion,
                                        boolean puedeGestionar) {
        List<VistaModulo> modulos = datos.stream().map(d -> vista(d, versionFase, produccion)).toList();
        Set<TipoModulo> tiene = modulos.stream().map(m -> m.modulo().tipo()).collect(java.util.stream.Collectors.toSet());
        return new ModulosProyecto(modulos, Arrays.stream(TipoModulo.values()).filter(t -> !tiene.contains(t)).toList(),
                puedeGestionar);
    }

    private static VistaModulo vista(DatosModulo d, String versionFase, Ambiente produccion) {
        ModuloProyecto m = d.modulo().modulo();
        String candidata = versionFase == null || d.historiasIntegradas() == 0 ? null
                : (versionFase.startsWith("v") ? versionFase.substring(1) : versionFase) + "-rc" + d.historiasIntegradas();
        List<Ambiente> ambientes = List.of(
                new Ambiente("develop", candidata, candidata == null ? null : d.ultimaIntegracion()),
                new Ambiente("staging", candidata, candidata == null ? null : d.ultimaIntegracion()),
                new Ambiente("producción", produccion == null ? null : produccion.version(),
                        produccion == null ? null : produccion.fecha()));
        return new VistaModulo(m, tecnologias(m.tecnologia()), d.responsable(), ambientes,
                d.modulo().ciEstado() == null ? null : new Build(d.modulo().ciEstado(), d.modulo().ciFecha()),
                contar(d.tickets(), POR_HACER), contar(d.tickets(), EN_PROGRESO), contar(d.tickets(), EN_QA),
                (int) d.mantenimiento().stream().filter(c -> c.cuenta() && !c.terminado()).mapToLong(ConteoTickets::cantidad).sum(),
                d.conTrabajo());
    }

    /** "Java 21 · Spring Boot 3, MySQL 8" -> [Java 21, Spring Boot 3, MySQL 8] */
    static List<String> tecnologias(String texto) {
        return texto == null ? List.of()
                : Arrays.stream(texto.split("[·,]")).map(String::trim).filter(t -> !t.isEmpty()).toList();
    }

    private static int contar(List<ConteoTickets> conteos, Set<String> estados) {
        return (int) conteos.stream().filter(c -> estados.contains(c.estado())).mapToLong(ConteoTickets::cantidad).sum();
    }
}
