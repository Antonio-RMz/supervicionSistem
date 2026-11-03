package com.team.gestorproyectos.historia.domain.port.in;

import java.util.List;

import com.team.gestorproyectos.historia.domain.model.AccionDeLista;
import com.team.gestorproyectos.historia.domain.model.AccionHistoria;
import com.team.gestorproyectos.historia.domain.model.DatosAccionHistoria;
import com.team.gestorproyectos.historia.domain.model.DetalleHistoria;
import com.team.gestorproyectos.historia.domain.model.IntegracionHistoria;
import com.team.gestorproyectos.historia.domain.model.ResultadoAccionHistoria;

/** Detalle de la historia y sus acciones por estado (PDF pág. 21, 24, 28 y 33). Pide ser miembro del proyecto. */
public interface GestionarHistoriaUseCase {

    /** El botón de cada historia del proyecto que esta persona ve en las listas; las que no tienen, no vienen. */
    List<AccionDeLista> accionesDeLista(Long proyectoId, Long usuarioId, boolean esAdmin);

    DetalleHistoria detalle(Long proyectoId, Long historiaId, Long usuarioId, boolean esAdmin);

    /** La vista Integrar historia (PDF pág. 28). Integrar se hace con {@link #ejecutar} y la acción INTEGRAR. */
    IntegracionHistoria integracion(Long proyectoId, Long historiaId, Long usuarioId, boolean esAdmin);

    ResultadoAccionHistoria ejecutar(Long proyectoId, Long historiaId, Long usuarioId, boolean esAdmin,
                                     AccionHistoria accion, DatosAccionHistoria datos);

    void verificarCriterio(Long proyectoId, Long historiaId, Long criterioId, boolean verificado, Long usuarioId,
                           boolean esAdmin);


    void comentar(Long proyectoId, Long historiaId, String texto, Long usuarioId, boolean esAdmin);

    // ─── Historia con Desarrolladores ───

    /** El Desarrollador marca (o desmarca) que terminó su parte. */
    void marcarMiParte(Long proyectoId, Long historiaId, boolean terminado, Long usuarioId, boolean esAdmin);

    /** Agrega un punto a "Lo que hice"; moduloId opcional (uno de los repositorios de la historia). */
    void agregarTrabajo(Long proyectoId, Long historiaId, String descripcion, Long moduloId, Long usuarioId,
                        boolean esAdmin);

    void marcarTrabajo(Long proyectoId, Long historiaId, Long trabajoId, boolean hecho, Long usuarioId,
                       boolean esAdmin);

    void quitarTrabajo(Long proyectoId, Long historiaId, Long trabajoId, Long usuarioId, boolean esAdmin);

    /** La borra del todo con sus criterios, imágenes, comentarios e historial. Líder o Admin, si aún no se inicia. */
    void eliminar(Long proyectoId, Long historiaId, Long usuarioId, boolean esAdmin);
}
