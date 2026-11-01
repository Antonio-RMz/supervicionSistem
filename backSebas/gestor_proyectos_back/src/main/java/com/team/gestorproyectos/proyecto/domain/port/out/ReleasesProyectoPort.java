package com.team.gestorproyectos.proyecto.domain.port.out;

import java.util.List;
import java.util.Optional;

import com.team.gestorproyectos.proyecto.domain.model.ReleasesProyecto.HistoriaLista;
import com.team.gestorproyectos.proyecto.domain.model.ReleasesProyecto.Publicado;

/** Lecturas de la pestaña Releases (PDF pág. 36). */
public interface ReleasesProyectoPort {

    List<HistoriaLista> historiasAceptadas(Long proyectoId);

    /** La fase que se trabaja ahora o, si no hay, la siguiente planeada (sin Mantenimiento). */
    Optional<Long> proximaFase(Long proyectoId);

    Optional<Long> faseMantenimiento(Long proyectoId);

    /** Hotfix (bugs Crítica o Alta de Mantenimiento) por estado: APROBADO y los que aún se corrigen. */
    HotfixAbiertos hotfixAbiertos(Long proyectoId);

    /** Publicados y revertidos, con su contenido; sin marcar cuál está en producción. */
    List<Publicado> publicados(Long proyectoId);

    record HotfixAbiertos(int aprobados, int enCamino) {
    }
}
