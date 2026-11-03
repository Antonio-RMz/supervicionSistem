package com.team.gestorproyectos.shared.domain.port;

import java.util.Collection;
import java.util.List;

/**
 * Deja una notificación a las personas a las que les afecta algo de un ticket. La usan los casos de uso de
 * tickets; quién la recibe lo decide cada caso de uso.
 */
public interface AvisosPort {

    enum Tipo {
        /** Le asignaron el ticket (como responsable). */
        ASIGNACION,
        /** Reportaron un bug en un proyecto que lidera. */
        BUG_REPORTADO,
        /** Comentaron un ticket suyo (lo tiene asignado o lo creó). */
        COMENTARIO
    }

    /**
     * Nunca se avisa a quien hizo la acción; los null y repetidos se ignoran.
     *
     * @param actorId quien hizo la acción: aparece en el texto ("Ana te asignó…")
     */
    void avisar(Tipo tipo, Long ticketId, Long actorId, Collection<Long> destinatarios);

    /** Cómo le toca la historia a quien se le asigna. */
    enum Papel {
        /** Analista o Líder responsable de la historia */
        RESPONSABLE,
        /** Uno de sus Desarrolladores */
        DESARROLLADOR
    }

    /**
     * Le asignaron una historia: le llega igual que un ticket asignado y lleva a la historia.
     * Las mismas reglas que {@link #avisar}: nunca a quien hizo la acción; null y repetidos se ignoran.
     */
    void historiaAsignada(Long historiaId, Papel papel, Long actorId, Collection<Long> destinatarios);

    /** Varias historias importadas de una vez: un solo aviso ("te asignó 12 historias…") que lleva a la primera. */
    void historiasAsignadas(List<Long> historiaIds, Long actorId, Long destinatario);
}
