package com.team.gestorproyectos.notificacion.infrastructure.persistence;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.team.gestorproyectos.notificacion.domain.model.Notificacion;
import com.team.gestorproyectos.notificacion.domain.port.out.NotificacionesPort;
import com.team.gestorproyectos.notificacion.infrastructure.persistence.NotificacionesJpaRepository.TicketFila;
import com.team.gestorproyectos.shared.domain.model.CodigoItem;
import com.team.gestorproyectos.shared.domain.port.AvisosPort;

import lombok.RequiredArgsConstructor;

/** Guarda y lee las notificaciones. Arma el texto con el código y el título del ticket. */
@Component
@RequiredArgsConstructor
public class NotificacionesAdapter implements NotificacionesPort, AvisosPort {

    private static final int LARGO_TEXTO = 300;
    private static final Map<String, String> SEVERIDADES =
            Map.of("CRITICA", "Crítica", "ALTA", "Alta", "MEDIA", "Media", "BAJA", "Baja");

    private final NotificacionesJpaRepository repository;

    @Override
    public void avisar(Tipo tipo, Long ticketId, Long actorId, Collection<Long> destinatarios) {
        Set<Long> para = destinatariosSin(actorId, destinatarios);
        if (para.isEmpty()) {
            return;
        }
        TicketFila ticket = repository.ticket(ticketId).orElse(null);
        if (ticket == null) {
            return;
        }
        String texto = texto(tipo, repository.nombre(actorId).orElse("Alguien"), ticket);
        LocalDateTime ahora = LocalDateTime.now();
        para.forEach(usuarioId ->
                repository.guardar(usuarioId, tipo.name(), texto, ticket.getProyectoId(), ticketId, ahora));
    }

    @Override
    public void historiaAsignada(Long historiaId, Papel papel, Long actorId, Collection<Long> destinatarios) {
        Set<Long> para = destinatariosSin(actorId, destinatarios);
        if (para.isEmpty()) {
            return;
        }
        repository.historia(historiaId).ifPresent(h -> {
            String texto = textoHistoria(papel, repository.nombre(actorId).orElse("Alguien"), h.getNumero(), h.getTitulo());
            LocalDateTime ahora = LocalDateTime.now();
            para.forEach(u -> repository.guardarDeHistoria(u, texto, h.getProyectoId(), historiaId, ahora));
        });
    }

    @Override
    public void historiasAsignadas(List<Long> historiaIds, Long actorId, Long destinatario) {
        if (historiaIds.isEmpty() || destinatario == null || destinatario.equals(actorId)) {
            return;
        }
        if (historiaIds.size() == 1) {
            historiaAsignada(historiaIds.get(0), Papel.RESPONSABLE, actorId, List.of(destinatario));
            return;
        }
        repository.historia(historiaIds.get(0)).ifPresent(h -> repository.guardarDeHistoria(destinatario,
                corto(repository.nombre(actorId).orElse("Alguien") + " te asignó " + historiaIds.size()
                        + " historias como responsable"),
                h.getProyectoId(), historiaIds.get(0), LocalDateTime.now()));
    }

    /** "Ana López te asignó HU-0004 para desarrollar · Dashboard del Coordinador" */
    static String textoHistoria(Papel papel, String actor, int numero, String titulo) {
        String como = papel == Papel.RESPONSABLE ? " como responsable" : " para desarrollar";
        return corto(actor + " te asignó " + CodigoItem.historia(numero) + como + " · " + titulo);
    }

    private static Set<Long> destinatariosSin(Long actorId, Collection<Long> destinatarios) {
        Set<Long> para = new LinkedHashSet<>(destinatarios.stream().filter(Objects::nonNull).toList());
        para.remove(actorId); // nunca a quien hizo la acción
        return para;
    }

    private static String corto(String texto) {
        return texto.length() <= LARGO_TEXTO ? texto : texto.substring(0, LARGO_TEXTO - 1) + "…";
    }

    /** "Ana López te asignó BUGS-12 · Error al guardar" */
    static String texto(Tipo tipo, String actor, TicketFila t) {
        String ticket = CodigoItem.de(t.getTipo(), t.getNumero()) + " · " + t.getTitulo();
        String texto = switch (tipo) {
            case ASIGNACION -> actor + " te asignó " + ticket;
            case BUG_REPORTADO -> actor + " reportó " + ticket
                    + (t.getSeveridad() == null ? "" : " (" + SEVERIDADES.getOrDefault(t.getSeveridad(), t.getSeveridad()) + ")");
            case COMENTARIO -> actor + " comentó en " + ticket;
        };
        return corto(texto);
    }

    @Override
    public List<Notificacion> recientes(Long usuarioId, int limite) {
        return repository.recientes(usuarioId, limite).stream()
                .map(n -> new Notificacion(n.getId(), n.getTipo(), n.getTexto(), n.getProyectoId(), n.getTicketId(),
                        n.getHistoriaId(), Boolean.TRUE.equals(n.getLeida()), n.getCreadoEn()))
                .toList();
    }

    @Override
    public long noLeidas(Long usuarioId) {
        return repository.noLeidas(usuarioId);
    }

    @Override
    public void marcarLeida(Long usuarioId, Long notificacionId) {
        repository.marcarLeida(usuarioId, notificacionId);
    }

    @Override
    public void marcarTodasLeidas(Long usuarioId) {
        repository.marcarTodasLeidas(usuarioId);
    }
}
