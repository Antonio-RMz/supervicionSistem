package com.team.gestorproyectos.fase.domain.port.out;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.team.gestorproyectos.fase.domain.model.BloqueosEliminarFase;
import com.team.gestorproyectos.fase.domain.model.CambioFase;
import com.team.gestorproyectos.fase.domain.model.FaseParaGestionar;
import com.team.gestorproyectos.fase.domain.model.InfoFase;
import com.team.gestorproyectos.fase.domain.model.InfoRelease;

public interface FaseGestionPort {

    /** Las fases del proyecto, en orden. */
    List<Long> fases(Long proyectoId);

    /** Vacío si la fase no existe o no es de ese proyecto. */
    Optional<FaseParaGestionar> buscarParaGestionar(Long proyectoId, Long faseId);

    InfoFase info(Long proyectoId, Long faseId);

    /**
     * Guarda el cambio con sus efectos: cancela lo no integrado, prepara y publica el release,
     * cierra o reabre historias, crea el BUGS de la reversión. Todo queda en el historial.
     *
     * @return el código del BUGS creado al revertir ("BUGS-50"); null en las demás acciones
     */
    String guardar(CambioFase cambio, Long usuarioId, LocalDateTime ahora);

    InfoRelease release(Long proyectoId, Long faseId);

    /** Crea el release de la fase si aún no existe (Preparando) y le guarda las notas. */
    void guardarNotas(Long proyectoId, Long faseId, String version, String resumen, String notas, boolean enviar);

    void marcarCriterio(Long faseId, Long criterioId, Long usuarioId, LocalDateTime ahora);

    /** El paso automático de En curso a Lista para release al cumplirse todos los criterios. */
    void pasarAListaParaRelease(Long faseId, LocalDateTime ahora);

    BloqueosEliminarFase bloqueosParaEliminar(Long faseId);

    /** Las rutas de las imágenes de sus historias, para borrar los archivos. */
    List<String> rutasDeImagenes(Long faseId);

    /** Borra la fase con sus historias (criterios, módulos, imágenes, comentarios), criterios de cierre e historial. */
    void eliminar(Long faseId);
}
