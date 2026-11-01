package com.team.gestorproyectos.fase.domain.port.in;

import com.team.gestorproyectos.fase.domain.model.DisenoFase;
import com.team.gestorproyectos.fase.domain.model.EdicionFase;
import com.team.gestorproyectos.fase.domain.model.NuevaFase;

/**
 * Corregir lo que se capturó en Nueva fase. Lo hace el Líder del proyecto o el responsable de la fase
 * (permiso "fase.gestionar") mientras la fase está Planeada, En curso o Pausada.
 */
public interface EditarFaseUseCase {

    EdicionFase preparar(Long proyectoId, Long faseId, Long usuarioId, boolean esAdmin);

    void editar(Long proyectoId, Long faseId, Long usuarioId, boolean esAdmin, NuevaFase cambios);

    /** El enlace de Figma de toda la fase y si lo heredan las historias sin diseño propio. Mismos permisos que editar. */
    void guardarDiseno(Long proyectoId, Long faseId, Long usuarioId, boolean esAdmin, DisenoFase diseno);
}
