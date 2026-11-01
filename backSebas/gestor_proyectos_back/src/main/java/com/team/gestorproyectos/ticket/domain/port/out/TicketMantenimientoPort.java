package com.team.gestorproyectos.ticket.domain.port.out;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.team.gestorproyectos.ticket.domain.model.Mantenimiento.ParchePublicado;
import com.team.gestorproyectos.ticket.domain.model.Mantenimiento.TicketDeMantenimiento;
import com.team.gestorproyectos.ticket.domain.model.NuevoTicketMantenimiento;
import com.team.gestorproyectos.ticket.domain.model.PreparacionMantenimiento.HistoriaDeOrigen;
import com.team.gestorproyectos.ticket.domain.model.PreparacionTicket.ModuloDeTicket;
import com.team.gestorproyectos.ticket.domain.model.ReporteFalla;

/** La vista de Mantenimiento (PDF pág. 37) y lo que se crea desde ella (págs. 38 y 39). */
public interface TicketMantenimientoPort {

    /** La fase permanente de Mantenimiento del proyecto. */
    Optional<Long> faseMantenimiento(Long proyectoId);

    /** Todos sus tickets (de ahí se ve cuáles siguen abiertos). */
    List<TicketDeMantenimiento> tickets(Long faseId);

    /** La fase en curso o, si no hay, la siguiente planeada (sin Mantenimiento). */
    Optional<Integer> proximaFaseNumero(Long proyectoId);

    /** Minutos de reportado a publicado de cada hotfix publicado desde cierta fecha. */
    List<Long> minutosHotfix(Long proyectoId, LocalDateTime desde);

    /** Los parches publicados, del más reciente al más antiguo, con cuántos bugs salieron en cada uno. */
    List<ParchePublicado> parches(Long proyectoId);

    /** Todos los módulos del proyecto. */
    List<ModuloDeTicket> modulos(Long proyectoId);

    /** Las historias que ya salieron de borrador y no se descartaron, de la más reciente a la más antigua. */
    List<HistoriaDeOrigen> historias(Long proyectoId);

    /** Guarda el ticket en Mantenimiento en su estado inicial y lo deja en el historial. */
    Long crear(Long proyectoId, Long faseId, int numero, NuevoTicketMantenimiento nuevo, Long usuarioId);

    /** Guarda la falla como bug Reportado con su severidad provisional y la versión que estaba en producción. */
    Long reportar(Long proyectoId, Long faseId, int numero, ReporteFalla reporte, String versionReportada,
                  Long usuarioId);
}
