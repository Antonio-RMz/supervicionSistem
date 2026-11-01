package com.team.gestorproyectos.fase.domain.port.out;

import java.util.List;
import java.util.Optional;

import com.team.gestorproyectos.fase.domain.model.DisenoFase;
import com.team.gestorproyectos.fase.domain.model.FaseExistente;
import com.team.gestorproyectos.fase.domain.model.FaseGuardada;
import com.team.gestorproyectos.fase.domain.model.ModuloOpcion;
import com.team.gestorproyectos.fase.domain.model.NuevaFase;
import com.team.gestorproyectos.fase.domain.model.Responsable;

public interface FaseRepositoryPort {

    /** Vacío si no existe o el usuario no es miembro, su Líder o Admin. */
    Optional<ProyectoDeFase> proyectoVisible(Long proyectoId, Long usuarioId, boolean esAdmin);

    List<FaseExistente> fases(Long proyectoId);

    List<ModuloOpcion> modulos(Long proyectoId);

    /** Miembros activos y el Líder. */
    List<Responsable> equipo(Long proyectoId);

    /** Guarda la fase con sus módulos y criterios de cierre, y la deja en el historial. */
    Long crear(Long proyectoId, int numero, NuevaFase nueva, Long usuarioId);

    /** Vacío si la fase no existe o no es de ese proyecto. */
    Optional<FaseGuardada> guardada(Long proyectoId, Long faseId);

    /** Guarda los datos editados; módulos y criterios manuales se agregan o quitan según lo que cambió. */
    void actualizar(Long faseId, NuevaFase cambios, FaseGuardada antes);

    void guardarDiseno(Long faseId, DisenoFase diseno);

    record ProyectoDeFase(Long id, String nombre, Long liderId) {
    }
}
