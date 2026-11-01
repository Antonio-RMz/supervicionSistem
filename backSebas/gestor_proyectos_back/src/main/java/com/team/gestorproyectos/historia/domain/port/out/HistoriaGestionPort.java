package com.team.gestorproyectos.historia.domain.port.out;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.team.gestorproyectos.historia.domain.model.CambioHistoria;
import com.team.gestorproyectos.historia.domain.model.HistoriaParaGestionar;
import com.team.gestorproyectos.historia.domain.model.HistoriaParaIntegrar;
import com.team.gestorproyectos.historia.domain.model.ImagenGuardada;
import com.team.gestorproyectos.historia.domain.model.InfoHistoria;
import com.team.gestorproyectos.historia.domain.model.NuevaImagen;

public interface HistoriaGestionPort {

    /** Las historias del proyecto que esperan una decisión (Borrador, Lista, En validación, Aceptada). */
    List<Long> historiasPorDecidir(Long proyectoId);

    /** Vacío si la historia no existe o no es de ese proyecto. */
    Optional<HistoriaParaGestionar> buscarParaGestionar(Long proyectoId, Long historiaId);

    InfoHistoria info(Long proyectoId, Long historiaId);

    /** Sus tickets con su PR, los módulos, las demás historias de la fase y los ambientes (PDF pág. 28). */
    HistoriaParaIntegrar paraIntegrar(Long proyectoId, Long historiaId);

    /**
     * Guarda el cambio con sus efectos: cancela tickets sin empezar al descartar, reabre un ticket o crea
     * un bug al devolver, integra los tickets aprobados y sus PR, la cambia de fase. Todo queda en el historial.
     *
     * @return el código del bug creado al devolver ("BUGS-33"); null en las demás acciones
     */
    String guardar(CambioHistoria cambio, Long usuarioId, LocalDateTime ahora);

    List<Long> criterios(Long historiaId);

    void verificarCriterio(Long criterioId, boolean verificado);

    void comentar(Long historiaId, Long usuarioId, String texto, LocalDateTime ahora);

    List<ImagenGuardada> imagenes(Long historiaId);

    void agregarImagen(Long historiaId, NuevaImagen imagen, String ruta, Long usuarioId);

    void quitarImagen(Long imagenId);

    /** Tickets de Mantenimiento que la tienen como "Viene de". */
    long ticketsQueVienenDe(Long historiaId);

    /** Borra la historia con sus criterios, módulos, imágenes (solo el registro), comentarios e historial. */
    void eliminar(Long historiaId);
}
