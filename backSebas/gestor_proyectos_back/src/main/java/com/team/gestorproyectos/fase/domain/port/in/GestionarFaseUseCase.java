package com.team.gestorproyectos.fase.domain.port.in;

import java.util.List;

import com.team.gestorproyectos.fase.domain.model.AccionDeLista;
import com.team.gestorproyectos.fase.domain.model.AccionFase;
import com.team.gestorproyectos.fase.domain.model.DatosAccionFase;
import com.team.gestorproyectos.fase.domain.model.DetalleFase;
import com.team.gestorproyectos.fase.domain.model.ReleaseFase;
import com.team.gestorproyectos.fase.domain.model.ResultadoAccionFase;

/** Detalle de la fase y sus acciones por estado (PDF pág. 20, 25 y 32). Pide ser miembro del proyecto. */
public interface GestionarFaseUseCase {

    /** El botón de cada fase del proyecto que esta persona ve en el árbol; las fases sin botón no vienen. */
    List<AccionDeLista> accionesDeLista(Long proyectoId, Long usuarioId, boolean esAdmin);

    DetalleFase detalle(Long proyectoId, Long faseId, Long usuarioId, boolean esAdmin);

    ResultadoAccionFase ejecutar(Long proyectoId, Long faseId, Long usuarioId, boolean esAdmin, AccionFase accion,
                                 DatosAccionFase datos);

    /** La vista del release de la fase (PDF pág. 27). */
    ReleaseFase release(Long proyectoId, Long faseId, Long usuarioId, boolean esAdmin);

    /** Guarda las notas del release y si se envían al cliente al publicar. */
    void guardarNotas(Long proyectoId, Long faseId, Long usuarioId, boolean esAdmin, String notas,
                      boolean enviarAlCliente);

    /** Marca un criterio manual; si era el último, la fase pasa sola a Lista para release. */
    ResultadoAccionFase marcarCriterio(Long proyectoId, Long faseId, Long criterioId, Long usuarioId, boolean esAdmin);

    /** La borra con sus historias. Líder o Admin, solo si está Planeada y nada se ha iniciado. */
    void eliminar(Long proyectoId, Long faseId, Long usuarioId, boolean esAdmin);
}
