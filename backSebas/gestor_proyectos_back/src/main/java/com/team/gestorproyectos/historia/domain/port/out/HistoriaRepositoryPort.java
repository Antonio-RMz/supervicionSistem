package com.team.gestorproyectos.historia.domain.port.out;

import java.util.List;
import java.util.Optional;

import com.team.gestorproyectos.historia.domain.model.Analista;
import com.team.gestorproyectos.historia.domain.model.FaseParaHistoria;
import com.team.gestorproyectos.historia.domain.model.HistoriaGuardada;
import com.team.gestorproyectos.historia.domain.model.NuevaHistoria;
import com.team.gestorproyectos.historia.domain.model.PreparacionHistoria.UsuarioDelSistema;

public interface HistoriaRepositoryPort {

    /** Nombre del proyecto; vacío si no existe o el usuario no es miembro, su Líder o Admin. */
    Optional<String> proyectoVisible(Long proyectoId, Long usuarioId, boolean esAdmin);

    int siguienteNumero(Long proyectoId);

    /** Solo las fases que reciben historias, con sus módulos. */
    List<FaseParaHistoria> fasesAbiertas(Long proyectoId);

    /** Miembros activos y el Líder. */
    List<Analista> equipo(Long proyectoId);

    /** Los usuarios del sistema del proyecto, en su orden. */
    List<UsuarioDelSistema> usuariosDelSistema(Long proyectoId);

    /** Guarda la historia con sus criterios y módulos, y la deja en el historial como Borrador. */
    Long crear(Long proyectoId, int numero, NuevaHistoria nueva, Long usuarioId);

    /** Vacío si la historia no existe o no es de ese proyecto. */
    Optional<HistoriaGuardada> guardada(Long proyectoId, Long historiaId);

    /** Guarda los datos editados; criterios y módulos se reemplazan completos. */
    void actualizar(Long historiaId, NuevaHistoria cambios, HistoriaGuardada antes, Long usuarioId);
}
