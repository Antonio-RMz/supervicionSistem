package com.team.gestorproyectos.ticket.application;

import java.time.LocalDateTime;
import java.util.List;

import com.team.gestorproyectos.shared.application.ServicioAplicacion;
import com.team.gestorproyectos.shared.application.Transaccional;
import com.team.gestorproyectos.shared.domain.exception.AccesoDenegadoException;
import com.team.gestorproyectos.shared.domain.exception.NoEncontradoException;
import com.team.gestorproyectos.shared.domain.exception.ReglaNegocioException;
import com.team.gestorproyectos.shared.domain.model.FlujoTrabajo;
import com.team.gestorproyectos.ticket.domain.model.DetalleTicket;
import com.team.gestorproyectos.ticket.domain.model.InfoTicket;
import com.team.gestorproyectos.ticket.domain.model.Parche;
import com.team.gestorproyectos.ticket.domain.model.TicketParaMover;
import com.team.gestorproyectos.ticket.domain.model.TicketParaMover.Opcion;
import com.team.gestorproyectos.ticket.domain.model.TipoMovimiento;
import com.team.gestorproyectos.ticket.domain.port.in.DetalleTicketUseCase;
import com.team.gestorproyectos.ticket.domain.port.out.TicketDetallePort;
import com.team.gestorproyectos.ticket.domain.port.out.TicketEscrituraPort;
import com.team.gestorproyectos.ticket.domain.port.out.TicketParchePort;
import com.team.gestorproyectos.ticket.domain.port.out.TicketRepositoryPort;

import lombok.RequiredArgsConstructor;

@ServicioAplicacion
@RequiredArgsConstructor
public class DetalleTicketService implements DetalleTicketUseCase {

    private static final int LARGO_COMENTARIO = 2000;
    private static final int LARGO_SUBTAREA = 300;

    private final TicketEscrituraPort proyectos;
    private final TicketRepositoryPort tickets;
    private final TicketDetallePort detalles;
    private final TicketParchePort parches;

    @Override
    @Transaccional(soloLectura = true)
    public DetalleTicket detalle(Long proyectoId, Long ticketId, Long usuarioId, boolean esAdmin) {
        InfoTicket info = buscar(proyectoId, ticketId, usuarioId, esAdmin);
        TicketParaMover ticket = tickets.buscarParaMover(ticketId).orElseThrow();
        List<Opcion> opciones = ticket.opciones(usuarioId);
        String actor = ticket.actor();
        boolean meToca = opciones.stream()
                .anyMatch(o -> o.tipo() == TipoMovimiento.AVANZAR || o.tipo() == TipoMovimiento.RECHAZAR);
        Parche parche = ticket.esHotfix() && "APROBADO".equals(info.estado())
                ? Parche.despuesDe(parches.versionEnProduccion(proyectoId).orElse(null),
                        parches.hotfixAprobados(proyectoId))
                : null;
        return new DetalleTicket(
                info,
                ticket.severidad(),
                ticket.esHotfix(),
                parche,
                actor,
                meToca,
                opciones,
                puedeMarcarSubtareas(info, usuarioId, esAdmin),
                puedeEditarSubtareas(info, ticket.liderId(), usuarioId, esAdmin),
                proyectos.editable(proyectoId, ticketId).map(t -> t.puedeEditar(usuarioId, esAdmin)).orElse(false),
                proyectos.abiertos(proyectoId).stream().filter(t -> !t.id().equals(ticketId)).toList(),
                proyectos.equipo(proyectoId));
    }

    @Override
    @Transaccional
    public void marcarSubtarea(Long proyectoId, Long ticketId, Long subtareaId, boolean hecha, Long usuarioId,
                               boolean esAdmin) {
        InfoTicket info = buscar(proyectoId, ticketId, usuarioId, esAdmin);
        if (!puedeMarcarSubtareas(info, usuarioId, esAdmin)) {
            throw new AccesoDenegadoException("Las subtareas las marca quien trabaja el ticket");
        }
        if (!detalles.marcarSubtarea(ticketId, subtareaId, hecha)) {
            throw new ReglaNegocioException("Esa subtarea no es de este ticket");
        }
    }

    @Override
    @Transaccional
    public void agregarSubtarea(Long proyectoId, Long ticketId, String descripcion, Long usuarioId,
                                boolean esAdmin) {
        verificarEdicionDeSubtareas(proyectoId, ticketId, usuarioId, esAdmin);
        String limpia = descripcion == null ? "" : descripcion.trim();
        if (limpia.isEmpty()) {
            throw new ReglaNegocioException("Escribe la subtarea");
        }
        if (limpia.length() > LARGO_SUBTAREA) {
            throw new ReglaNegocioException("La subtarea es muy larga (máximo " + LARGO_SUBTAREA + " letras)");
        }
        detalles.agregarSubtarea(ticketId, limpia);
    }

    @Override
    @Transaccional
    public void quitarSubtarea(Long proyectoId, Long ticketId, Long subtareaId, Long usuarioId, boolean esAdmin) {
        verificarEdicionDeSubtareas(proyectoId, ticketId, usuarioId, esAdmin);
        if (!detalles.quitarSubtarea(ticketId, subtareaId)) {
            throw new ReglaNegocioException("Esa subtarea no es de este ticket");
        }
    }

    private void verificarEdicionDeSubtareas(Long proyectoId, Long ticketId, Long usuarioId, boolean esAdmin) {
        InfoTicket info = buscar(proyectoId, ticketId, usuarioId, esAdmin);
        Long liderId = tickets.buscarParaMover(ticketId).orElseThrow().liderId();
        if (!puedeEditarSubtareas(info, liderId, usuarioId, esAdmin)) {
            throw new AccesoDenegadoException("Las subtareas las agregan quien trabaja el ticket o el Líder");
        }
    }

    @Override
    @Transaccional
    public void comentar(Long proyectoId, Long ticketId, String texto, Long usuarioId, boolean esAdmin) {
        buscar(proyectoId, ticketId, usuarioId, esAdmin);
        String limpio = texto == null ? "" : texto.trim();
        if (limpio.isEmpty()) {
            throw new ReglaNegocioException("Escribe el comentario");
        }
        if (limpio.length() > LARGO_COMENTARIO) {
            throw new ReglaNegocioException("El comentario es muy largo (máximo " + LARGO_COMENTARIO + " letras)");
        }
        detalles.comentar(ticketId, usuarioId, limpio, LocalDateTime.now());
    }

    private InfoTicket buscar(Long proyectoId, Long ticketId, Long usuarioId, boolean esAdmin) {
        proyectos.proyectoVisible(proyectoId, usuarioId, esAdmin)
                .orElseThrow(() -> new NoEncontradoException("No encontramos ese proyecto"));
        return detalles.info(proyectoId, ticketId)
                .orElseThrow(() -> new NoEncontradoException("No encontramos ese ticket"));
    }

    private static boolean puedeMarcarSubtareas(InfoTicket info, Long usuarioId, boolean esAdmin) {
        boolean loTrabaja = esAdmin || (info.responsable() != null && usuarioId.equals(info.responsable().id()));
        return loTrabaja && !FlujoTrabajo.terminado(info.estado());
    }

    private static boolean puedeEditarSubtareas(InfoTicket info, Long liderId, Long usuarioId, boolean esAdmin) {
        return (usuarioId.equals(liderId) || puedeMarcarSubtareas(info, usuarioId, esAdmin))
                && !FlujoTrabajo.terminado(info.estado());
    }
}
