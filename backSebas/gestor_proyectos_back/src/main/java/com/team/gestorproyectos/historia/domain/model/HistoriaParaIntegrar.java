package com.team.gestorproyectos.historia.domain.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import com.team.gestorproyectos.shared.domain.model.CodigoItem;

/**
 * Lo que se mete a develop al integrar una historia aceptada (PDF pág. 28): sus tickets con su PR por módulo,
 * la revisión previa, en qué versión queda cada ambiente y qué pasará al integrar.
 *
 * El código de cada ticket entra a develop con su PR. Un PR sin aprobar, con el CI en rojo o con conflictos
 * impide integrar; un ticket sin PR registrado solo se avisa (su rama se mezcla igual).
 *
 * @param version          la versión del release de la fase ("v1.3.0"); null si la fase no tiene
 * @param tickets          los tickets de la historia (también los cancelados, que no cuentan)
 * @param modulos          los módulos donde caen sus tickets, con cuántas historias de la fase ya integraron ahí
 * @param historiasDeFase  las historias de la fase que no se descartaron
 * @param ultimaIntegracion cuándo se integró la última historia de la fase a develop
 * @param produccion       lo que está hoy en producción; null si nunca se ha publicado
 */
public record HistoriaParaIntegrar(
        String codigo,
        String estado,
        int faseNumero,
        String version,
        String aceptadaPor,
        LocalDateTime aceptadaEn,
        List<TicketAIntegrar> tickets,
        List<Modulo> modulos,
        List<HistoriaDeFase> historiasDeFase,
        LocalDateTime ultimaIntegracion,
        Publicada produccion) {

    /** Ya están en develop. */
    private static final Set<String> EN_DEVELOP = Set.of("INTEGRADA", "INTEGRADO", "PUBLICADO", "CERRADA", "CERRADO");

    private static final Set<String> ACEPTADA_O_DESPUES = Set.of("ACEPTADA", "INTEGRADA", "CERRADA");

    public enum Resultado { OK, AVISO, FALTA }

    /** @param pr el último PR del ticket; null si no tiene */
    public record TicketAIntegrar(Long id, String tipo, int numero, String titulo, String estado, Long moduloId,
                                  String rama, PullRequest pr) {

        public String codigo() {
            return CodigoItem.de(tipo, numero);
        }

        public boolean cuenta() {
            return comoTicketDeHistoria().cuenta();
        }

        public boolean terminado() {
            return comoTicketDeHistoria().terminado();
        }

        public boolean conCodigo() {
            return moduloId != null;
        }

        public boolean enDevelop() {
            return EN_DEVELOP.contains(estado) || (pr != null && "INTEGRADO".equals(pr.estado()));
        }

        private TicketDeHistoria comoTicketDeHistoria() {
            return new TicketDeHistoria(id, tipo, numero, titulo, estado, null, null);
        }
    }

    /**
     * @param estado     ABIERTO, APROBADO, INTEGRADO, CERRADO
     * @param ci         PENDIENTE, PASO, FALLO
     * @param revisadoEn la última vez que se revisó contra develop
     */
    public record PullRequest(int numero, String estado, String ci, boolean conConflictos, LocalDateTime revisadoEn) {
    }

    /** @param integradas cuántas historias de la fase ya metieron código de este módulo a develop */
    public record Modulo(Long id, String tipo, String nombre, String tecnologia, int integradas) {

        /** "portal-api 1.3.0-rc3"; las apps móviles se publican como build. */
        String enStaging(String candidata) {
            return nombre + ("MOVIL".equals(tipo) ? " build " : " ") + candidata;
        }
    }

    public record HistoriaDeFase(Long id, int numero, String estado) {
    }

    public record Publicada(String version, LocalDateTime fecha) {
    }

    /** Un punto de la revisión previa; {@code fecha} acompaña al detalle cuando importa cuándo pasó. */
    public record Revision(String texto, Resultado resultado, String detalle, LocalDateTime fecha) {
    }

    /** @param version null si aún no hay nada de la fase en ese ambiente */
    public record Ambiente(String nombre, String version, String detalle, LocalDateTime fecha) {
    }

    public record ModuloConTickets(Modulo modulo, List<TicketAIntegrar> tickets) {
    }

    public HistoriaParaIntegrar {
        tickets = List.copyOf(tickets);
        modulos = List.copyOf(modulos);
        historiasDeFase = List.copyOf(historiasDeFase);
    }

    // ─── Qué se integra ─────────────────────────────────────────────────────

    /** Los tickets con código agrupados por módulo, en el orden de los módulos. */
    public List<ModuloConTickets> queSeIntegra() {
        Map<Long, List<TicketAIntegrar>> porModulo = conCodigo().stream()
                .collect(Collectors.groupingBy(TicketAIntegrar::moduloId));
        return modulos.stream()
                .filter(m -> porModulo.containsKey(m.id()))
                .map(m -> new ModuloConTickets(m, porModulo.get(m.id())))
                .toList();
    }

    /** Los que todavía no están en develop: lo que mezcla este integrar. */
    public List<TicketAIntegrar> porIntegrar() {
        return conCodigo().stream().filter(t -> !t.enDevelop()).toList();
    }

    /** "feature/" y "fix/" de las ramas que se mezclan. */
    public List<String> prefijosDeRama() {
        return porIntegrar().stream()
                .map(t -> t.rama() == null || !t.rama().contains("/") ? "feature/" : t.rama().substring(0, t.rama().indexOf('/') + 1))
                .distinct().toList();
    }

    // ─── Revisión previa ────────────────────────────────────────────────────

    public List<Revision> revision() {
        List<Revision> puntos = new ArrayList<>();

        puntos.add(ACEPTADA_O_DESPUES.contains(estado)
                ? new Revision("Historia aceptada por el Analista", Resultado.OK, aceptadaPor, aceptadaEn)
                : new Revision("Historia aceptada por el Analista", Resultado.FALTA, "Falta que el Analista la acepte", null));

        List<TicketAIntegrar> cuentan = tickets.stream().filter(TicketAIntegrar::cuenta).toList();
        long aprobados = cuentan.stream().filter(TicketAIntegrar::terminado).count();
        puntos.add(new Revision("Todos sus tickets aprobados en QA",
                aprobados == cuentan.size() ? Resultado.OK : Resultado.FALTA, aprobados + " de " + cuentan.size(), null));

        puntos.add(revisionDePrs());
        puntos.add(revisionDeCi());
        puntos.add(revisionDeConflictos());
        return puntos;
    }

    /** Lo que impide integrar, en palabras para el panel. Vacío si ya se puede. */
    public List<String> impedimentos() {
        List<String> faltan = new ArrayList<>();
        if (!ACEPTADA_O_DESPUES.contains(estado)) {
            faltan.add("Falta que el Analista la acepte");
        }
        tickets.stream().filter(t -> t.cuenta() && !t.terminado())
                .forEach(t -> faltan.add(t.codigo() + " aún no se aprueba en QA"));
        conPrPendiente().forEach(t -> {
            PullRequest pr = t.pr();
            if (!"APROBADO".equals(pr.estado())) {
                faltan.add("El PR #" + pr.numero() + " de " + t.codigo() + " aún no se aprueba en revisión de código");
            }
            if ("FALLO".equals(pr.ci())) {
                faltan.add("El CI falló en el PR #" + pr.numero());
            } else if (!"PASO".equals(pr.ci())) {
                faltan.add("El CI sigue corriendo en el PR #" + pr.numero());
            }
            if (pr.conConflictos()) {
                faltan.add("El PR #" + pr.numero() + " tiene conflictos con develop");
            }
        });
        return faltan;
    }

    private Revision revisionDePrs() {
        String texto = "Pull requests aprobados por revisión de código";
        List<TicketAIntegrar> sinAprobar = conPrPendiente().stream()
                .filter(t -> !"APROBADO".equals(t.pr().estado())).toList();
        if (!sinAprobar.isEmpty()) {
            return new Revision(texto, Resultado.FALTA, "Sin aprobar: " + prs(sinAprobar), null);
        }
        List<TicketAIntegrar> sinPr = porIntegrar().stream().filter(t -> t.pr() == null).toList();
        if (!sinPr.isEmpty()) {
            return new Revision(texto, Resultado.AVISO, "Sin PR registrado: "
                    + sinPr.stream().map(TicketAIntegrar::codigo).collect(Collectors.joining(", ")), null);
        }
        long pendientes = porIntegrar().size();
        long yaIntegrados = conCodigo().size() - pendientes;
        if (pendientes == 0) {
            return new Revision(texto, Resultado.OK, "Todo ya está en develop", null);
        }
        String detalle = pendientes + (pendientes == 1 ? " pendiente de merge" : " pendientes de merge");
        if (yaIntegrados > 0) {
            detalle += ", " + yaIntegrados + (yaIntegrados == 1 ? " ya integrado" : " ya integrados");
        }
        return new Revision(texto, Resultado.OK, detalle, null);
    }

    private Revision revisionDeCi() {
        String texto = "CI en verde en las ramas";
        List<TicketAIntegrar> rojos = conPrPendiente().stream().filter(t -> !"PASO".equals(t.pr().ci())).toList();
        if (!rojos.isEmpty()) {
            boolean fallo = rojos.stream().anyMatch(t -> "FALLO".equals(t.pr().ci()));
            return new Revision(texto, Resultado.FALTA, (fallo ? "Falló en " : "Corriendo en ") + prs(rojos), null);
        }
        String nombres = nombresDeModulos(conPrPendiente());
        return new Revision(texto, Resultado.OK, nombres.isEmpty() ? "Sin PR por revisar" : nombres, null);
    }

    private Revision revisionDeConflictos() {
        String texto = "Sin conflictos con develop";
        List<TicketAIntegrar> conConflictos = conPrPendiente().stream().filter(t -> t.pr().conConflictos()).toList();
        if (!conConflictos.isEmpty()) {
            return new Revision(texto, Resultado.FALTA, "Con conflictos: " + prs(conConflictos), null);
        }
        LocalDateTime revisado = conPrPendiente().stream().map(t -> t.pr().revisadoEn()).filter(Objects::nonNull)
                .max(Comparator.naturalOrder()).orElse(null);
        return new Revision(texto, Resultado.OK, revisado == null ? null : "revisado", revisado);
    }

    // ─── Ambientes y versión candidata ──────────────────────────────────────

    /** develop y staging llevan la candidata de la fase ("1.3.0-rc2"); producción, lo último publicado. */
    public List<Ambiente> ambientes() {
        long integradas = historiasDeFase.stream()
                .filter(h -> Set.of("INTEGRADA", "CERRADA").contains(h.estado())).count();
        String actual = integradas == 0 ? null : candidata((int) integradas);
        String sinNada = version == null ? null : "sin integraciones de la fase";
        String cambia = "ACEPTADA".equals(estado) && version != null ? "cambia a rc" + (integradas + 1) : null;
        return List.of(
                new Ambiente("develop", actual, actual == null ? sinNada : null, ultimaIntegracion),
                new Ambiente("staging", actual, cambia != null ? cambia : actual == null ? sinNada : null, null),
                new Ambiente("producción (main)", produccion == null ? null : produccion.version(),
                        produccion == null ? "sin publicar" : null, produccion == null ? null : produccion.fecha()));
    }

    // ─── Qué pasará al integrar ─────────────────────────────────────────────

    public List<String> pasos() {
        List<String> pasos = new ArrayList<>();
        List<TicketAIntegrar> pendientes = porIntegrar();
        Map<Long, String> nombres = modulos.stream().collect(Collectors.toMap(Modulo::id, Modulo::nombre));
        if (pendientes.isEmpty()) {
            pasos.add("Su código ya está en develop: no hay ramas que mezclar");
        } else {
            pasos.add("Merge de " + y(pendientes.stream()
                    .map(t -> (t.pr() != null ? "PR #" + t.pr().numero() : "la rama de " + t.codigo())
                            + " (" + nombres.get(t.moduloId()) + ")")
                    .toList()) + " a develop con squash");
            pasos.add("Se borran las ramas " + y(prefijosDeRama().stream().map(p -> p + "…").toList())
                    + " ya integradas");
        }
        List<String> enStaging = queSeIntegra().stream().map(ModuloConTickets::modulo)
                .map(m -> version == null ? m.nombre() : m.enStaging(candidata(m.integradas() + 1)))
                .toList();
        pasos.add(enStaging.isEmpty() ? "El CI despliega develop en staging"
                : "El CI despliega develop en staging: " + y(enStaging));
        pasos.add(codigo + " pasa a Integrada y queda incluida en el release "
                + (version == null ? "de la Fase " + faseNumero : version));
        return pasos;
    }

    /** "v1.3.0" y 2 -> "1.3.0-rc2" */
    String candidata(int numero) {
        return (version.startsWith("v") ? version.substring(1) : version) + "-rc" + numero;
    }

    // ─── Apoyo ──────────────────────────────────────────────────────────────

    private List<TicketAIntegrar> conCodigo() {
        return tickets.stream().filter(t -> t.cuenta() && t.conCodigo()).toList();
    }

    private List<TicketAIntegrar> conPrPendiente() {
        return porIntegrar().stream().filter(t -> t.pr() != null).toList();
    }

    private String nombresDeModulos(List<TicketAIntegrar> deTickets) {
        Set<Long> ids = deTickets.stream().map(TicketAIntegrar::moduloId).collect(Collectors.toSet());
        return y(modulos.stream().filter(m -> ids.contains(m.id())).map(Modulo::nombre).toList());
    }

    private static String prs(List<TicketAIntegrar> deTickets) {
        return deTickets.stream().map(t -> "PR #" + t.pr().numero()).collect(Collectors.joining(", "));
    }

    /** ["a", "b", "c"] -> "a, b y c" */
    private static String y(List<String> partes) {
        if (partes.size() <= 1) {
            return String.join("", partes);
        }
        return String.join(", ", partes.subList(0, partes.size() - 1)) + " y " + partes.get(partes.size() - 1);
    }
}
