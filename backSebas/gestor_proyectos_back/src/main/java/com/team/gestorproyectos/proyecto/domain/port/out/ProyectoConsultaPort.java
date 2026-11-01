package com.team.gestorproyectos.proyecto.domain.port.out;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.team.gestorproyectos.proyecto.domain.model.Detenido;
import com.team.gestorproyectos.proyecto.domain.model.InfoProyecto;
import com.team.gestorproyectos.proyecto.domain.model.Miembro;
import com.team.gestorproyectos.proyecto.domain.model.ModuloConCi;
import com.team.gestorproyectos.proyecto.domain.model.Salud;
import com.team.gestorproyectos.proyecto.domain.model.TipoModulo;
import com.team.gestorproyectos.shared.domain.model.ConteoTickets;
import com.team.gestorproyectos.shared.domain.model.ItemDeProyecto;
import com.team.gestorproyectos.shared.domain.model.Persona;

/** Lecturas de la lista y el detalle de proyectos. */
public interface ProyectoConsultaPort {

    /** Si existe y el usuario es miembro, su Líder o Admin. */
    boolean visible(Long proyectoId, Long usuarioId, boolean esAdmin);

    /** @param usuarioId null = todos (Administrador) */
    List<ProyectoBase> proyectos(Long usuarioId);

    /** Conteos de tickets por fase, módulo y estado, sin la fase de Mantenimiento. */
    Map<Long, List<ConteoTickets>> conteos(Collection<Long> proyectoIds);

    Map<Long, List<ModuloConCi>> modulos(Collection<Long> proyectoIds);

    Map<Long, List<Miembro>> equipos(Collection<Long> proyectoIds);

    Optional<InfoProyecto> info(Long proyectoId);

    /** Fases sin canceladas, en orden; Mantenimiento al final. */
    List<FaseBase> fases(Long proyectoId);

    MetricasTickets metricas(Long proyectoId);

    Salud salud(Long proyectoId, LocalDateTime inicioDeMes, LocalDateTime hace30Dias);

    /** Bloqueados, y en QA desde antes de cierta fecha. */
    List<Detenido> detenidos(Long proyectoId, LocalDateTime enQaDesdeAntesDe);

    List<HistoriaBase> historias(Long proyectoId);

    /** Tickets del proyecto en cualquier estado, salvo los que ya no se harán. */
    List<TicketBase> tickets(Long proyectoId);

    List<Persona> personasDelEquipo();

    record ProyectoBase(Long id, String codigo, String nombre, String estado, String prioridad, Long clienteId,
                        String clienteNombre, String liderNombre, long fases, long historias) {
    }

    record FaseBase(Long id, int numero, String nombre, String estado, LocalDate fechaInicio, LocalDate fechaFin,
                    String versionRelease, boolean mantenimiento, List<TipoModulo> modulos, long historias,
                    long tickets) {
    }

    record MetricasTickets(long ticketsAbiertos, long bugsAbiertos, long bugsEnProduccion,
                           long mantenimientoAbiertos) {
    }

    record HistoriaBase(Long id, Long faseId, int numero, String titulo, String estado, Long analistaId,
                        String analistaIniciales) {
    }

    record TicketBase(ItemDeProyecto item, Long faseId, Long historiaId, TipoModulo modulo,
                      String responsableIniciales) {
    }
}
