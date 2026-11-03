package com.team.gestorproyectos.ticket.domain.model;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.team.gestorproyectos.shared.domain.model.CodigoItem;
import com.team.gestorproyectos.shared.domain.model.ConteoTickets;

/**
 * La vista de Mantenimiento (PDF pág. 37): la fase permanente donde viven las correcciones de producción
 * y el trabajo técnico que no es de ninguna fase. Nunca se cierra.
 *
 * Lo urgente (bugs Crítica o Alta) va por hotfix desde main y sale en un parche; lo normal (Media, Baja,
 * tareas técnicas, mejoras) se trabaja desde develop y sale con la siguiente fase.
 *
 * @param urgentes          hotfix aún en camino y bugs reportados que esperan su severidad
 * @param listos            hotfix aprobados en QA que saldrán en el siguiente parche
 * @param normales          el resto del trabajo abierto: sale con la siguiente fase
 * @param proximaFaseNumero con qué fase sale lo normal; null si no hay fase planeada ni en curso
 * @param horasMedioHotfix  de reportado a publicado, en los hotfix de los últimos 30 días; null si no hubo
 * @param puedePublicar     el Líder del proyecto con el permiso parche.publicar
 */
public record Mantenimiento(Long faseId, String enProduccion, String siguienteParche, List<TicketDeMantenimiento> urgentes,
                            List<TicketDeMantenimiento> listos, List<TicketDeMantenimiento> normales,
                            Integer proximaFaseNumero, Double horasMedioHotfix, List<ParchePublicado> parches,
                            boolean puedePublicar) {

    private static final Set<String> HOTFIX = Set.of("CRITICA", "ALTA");
    private static final Map<String, Integer> ORDEN_SEVERIDAD = Map.of("CRITICA", 0, "ALTA", 1);

    /**
     * @param severidad   de un bug: CRITICA, ALTA, MEDIA o BAJA; null si aún no se confirma
     * @param descripcion lo que se escribió al reportarlo; null si solo trae imagen
     * @param imagenId    la primera imagen, para la miniatura de la tarjeta; null si no tiene
     * @param imagenes    cuántas imágenes tiene
     */
    public record TicketDeMantenimiento(Long id, String tipo, int numero, String titulo, String estado,
                                        String severidad, String rama, Long responsableId, String responsableNombre,
                                        String descripcion, Long imagenId, int imagenes) {

        public String codigo() {
            return CodigoItem.de(tipo, numero);
        }

        /** Igual que TicketParaMover.esHotfix: bug Crítico o Alto de Mantenimiento. */
        public boolean hotfix() {
            return "BUGS".equals(tipo) && severidad != null && HOTFIX.contains(severidad);
        }

        /** Reportado y sin severidad: el Líder aún no decide si es urgente. */
        public boolean porConfirmar() {
            return "BUGS".equals(tipo) && "REPORTADO".equals(estado) && severidad == null;
        }

        boolean abierto() {
            ConteoTickets c = new ConteoTickets(null, null, estado, 1);
            return c.cuenta() && !c.terminado();
        }
    }

    public record ParchePublicado(String version, LocalDateTime fecha, int bugs) {
    }

    public static Mantenimiento armar(Long faseId, List<TicketDeMantenimiento> tickets, String enProduccion,
                                      Integer proximaFaseNumero, List<Long> minutosHotfix,
                                      List<ParchePublicado> parches, boolean puedePublicar) {
        Comparator<TicketDeMantenimiento> porSeveridad = Comparator
                .comparing((TicketDeMantenimiento t) -> t.severidad() == null ? 2 : ORDEN_SEVERIDAD.getOrDefault(t.severidad(), 2))
                .thenComparing(TicketDeMantenimiento::numero);
        List<TicketDeMantenimiento> urgentes = tickets.stream()
                .filter(t -> (t.hotfix() && t.abierto()) || t.porConfirmar()).sorted(porSeveridad).toList();
        List<TicketDeMantenimiento> listos = tickets.stream()
                .filter(t -> t.hotfix() && "APROBADO".equals(t.estado())).sorted(porSeveridad).toList();
        List<TicketDeMantenimiento> normales = tickets.stream()
                .filter(t -> t.abierto() && !t.hotfix() && !t.porConfirmar())
                .sorted(Comparator.comparing(TicketDeMantenimiento::tipo).thenComparing(TicketDeMantenimiento::numero))
                .toList();
        Double horas = minutosHotfix.isEmpty() ? null
                : minutosHotfix.stream().mapToLong(Long::longValue).average().orElse(0) / 60.0;
        return new Mantenimiento(faseId, enProduccion, Parche.siguienteVersion(enProduccion), urgentes, listos, normales,
                proximaFaseNumero, horas, parches, puedePublicar);
    }

    /** "Urgentes en producción": los hotfix en camino, sin contar los que esperan severidad. */
    public long urgentesConfirmados() {
        return urgentes.stream().filter(TicketDeMantenimiento::hotfix).count();
    }

    public long criticos() {
        return urgentes.stream().filter(t -> "CRITICA".equals(t.severidad())).count();
    }
}
