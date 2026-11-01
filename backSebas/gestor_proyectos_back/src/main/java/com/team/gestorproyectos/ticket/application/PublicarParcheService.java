package com.team.gestorproyectos.ticket.application;

import java.time.LocalDateTime;

import com.team.gestorproyectos.shared.application.ServicioAplicacion;
import com.team.gestorproyectos.shared.application.Transaccional;
import com.team.gestorproyectos.shared.domain.exception.AccesoDenegadoException;
import com.team.gestorproyectos.shared.domain.exception.NoEncontradoException;
import com.team.gestorproyectos.shared.domain.exception.ReglaNegocioException;
import com.team.gestorproyectos.shared.domain.port.PermisosPort;
import com.team.gestorproyectos.ticket.domain.model.Parche;
import com.team.gestorproyectos.ticket.domain.model.TicketParaMover;
import com.team.gestorproyectos.ticket.domain.model.TipoMovimiento;
import com.team.gestorproyectos.ticket.domain.port.in.PublicarParcheUseCase;
import com.team.gestorproyectos.ticket.domain.port.out.TicketEscrituraPort;
import com.team.gestorproyectos.ticket.domain.port.out.TicketParchePort;
import com.team.gestorproyectos.ticket.domain.port.out.TicketRepositoryPort;

import lombok.RequiredArgsConstructor;

@ServicioAplicacion
@RequiredArgsConstructor
public class PublicarParcheService implements PublicarParcheUseCase {

    private final TicketEscrituraPort proyectos;
    private final TicketRepositoryPort tickets;
    private final TicketParchePort parches;
    private final PermisosPort permisos;

    @Override
    @Transaccional
    public Parche publicar(Long proyectoId, Long ticketId, String versionEsperada, Long usuarioId, boolean esAdmin) {
        proyectos.proyectoVisible(proyectoId, usuarioId, esAdmin)
                .orElseThrow(() -> new NoEncontradoException("No encontramos ese proyecto"));
        TicketParaMover ticket = tickets.buscarParaMover(ticketId)
                .filter(t -> proyectoId.equals(t.proyectoId()))
                .orElseThrow(() -> new NoEncontradoException("No encontramos ese ticket"));
        if (!ticket.esHotfix() || !"APROBADO".equals(ticket.item().estado())) {
            throw new ReglaNegocioException("Solo un hotfix aprobado en QA sale en un parche");
        }
        boolean esSuLider = ticket.opciones(usuarioId).stream()
                .anyMatch(o -> o.tipo() == TipoMovimiento.PUBLICAR_PARCHE);
        if (!esSuLider || !permisos.puede(usuarioId, PermisosPort.PUBLICAR_PARCHES)) {
            throw new AccesoDenegadoException("El parche lo publica el Líder del proyecto");
        }

        Parche parche = Parche.despuesDe(parches.versionEnProduccion(proyectoId).orElse(null),
                parches.hotfixAprobados(proyectoId));
        if (!parche.version().equals(versionEsperada)) {
            throw new ReglaNegocioException("Ya salió otra versión. Recarga: este parche ahora será " + parche.version());
        }
        parches.publicar(proyectoId, parche, usuarioId, LocalDateTime.now());
        return parche;
    }
}
