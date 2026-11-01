package com.team.gestorproyectos.proyecto.application;

import java.util.List;

import com.team.gestorproyectos.proyecto.domain.model.EquipoProyecto;
import com.team.gestorproyectos.proyecto.domain.model.EquipoProyecto.Reasignacion;
import com.team.gestorproyectos.proyecto.domain.model.VistaEquipo;
import com.team.gestorproyectos.proyecto.domain.model.VistaEquipo.Candidato;
import com.team.gestorproyectos.proyecto.domain.port.in.GestionarEquipoUseCase;
import com.team.gestorproyectos.proyecto.domain.port.out.EquipoProyectoPort;
import com.team.gestorproyectos.proyecto.domain.port.out.EquipoProyectoPort.ProyectoDelEquipo;
import com.team.gestorproyectos.proyecto.domain.port.out.ProyectoConsultaPort;
import com.team.gestorproyectos.shared.application.ServicioAplicacion;
import com.team.gestorproyectos.shared.application.Transaccional;
import com.team.gestorproyectos.shared.domain.exception.AccesoDenegadoException;
import com.team.gestorproyectos.shared.domain.exception.NoEncontradoException;
import com.team.gestorproyectos.shared.domain.exception.ReglaNegocioException;
import com.team.gestorproyectos.shared.domain.port.PermisosPort;

import lombok.RequiredArgsConstructor;

@ServicioAplicacion
@RequiredArgsConstructor
public class GestionarEquipoService implements GestionarEquipoUseCase {

    private final EquipoProyectoPort equipos;
    private final ProyectoConsultaPort consulta;
    private final PermisosPort permisos;

    @Override
    @Transaccional(soloLectura = true)
    public VistaEquipo equipo(Long proyectoId, Long usuarioId, boolean esAdmin) {
        ProyectoDelEquipo proyecto = verificar(proyectoId, usuarioId, esAdmin);
        return new VistaEquipo(proyectoId, armar(proyecto), equipos.permisosExtra(proyectoId), proyecto.reglas(),
                puedeGestionar(proyecto, usuarioId, esAdmin));
    }

    @Override
    @Transaccional(soloLectura = true)
    public List<Candidato> candidatos(Long proyectoId, Long usuarioId, boolean esAdmin) {
        verificarGestion(proyectoId, usuarioId, esAdmin);
        return equipos.candidatos(proyectoId);
    }

    @Override
    @Transaccional
    public void agregar(Long proyectoId, Long nuevoId, Long usuarioId, boolean esAdmin) {
        verificarGestion(proyectoId, usuarioId, esAdmin);
        if (equipos.candidatos(proyectoId).stream().noneMatch(c -> c.id().equals(nuevoId))) {
            throw new ReglaNegocioException("Esa persona ya está en el proyecto, no es del equipo o tiene la cuenta inactiva");
        }
        equipos.agregar(proyectoId, nuevoId);
    }

    @Override
    @Transaccional
    public void quitar(Long proyectoId, Long miembroId, Long usuarioId, boolean esAdmin) {
        ProyectoDelEquipo proyecto = verificarGestion(proyectoId, usuarioId, esAdmin);
        EquipoProyecto equipo = armar(proyecto);
        if (!equipo.esMiembro(miembroId)) {
            throw new NoEncontradoException("Esa persona no es del equipo del proyecto");
        }
        equipo.impedimentosParaQuitar(miembroId, consulta.tickets(proyectoId), consulta.historias(proyectoId))
                .stream().findFirst().ifPresent(falta -> {
                    throw new ReglaNegocioException(falta);
                });
        equipos.quitar(proyectoId, miembroId);
    }

    @Override
    @Transaccional
    public int reasignar(Long proyectoId, Long deId, Long responsableNuevoId, Long testerNuevoId, Long usuarioId,
                         boolean esAdmin) {
        ProyectoDelEquipo proyecto = verificarGestion(proyectoId, usuarioId, esAdmin);
        Reasignacion reasignacion = armar(proyecto)
                .reasignar(deId, responsableNuevoId, testerNuevoId, consulta.tickets(proyectoId));
        equipos.reasignar(reasignacion, usuarioId);
        return reasignacion.comoResponsable().size() + reasignacion.comoTester().size();
    }

    private EquipoProyecto armar(ProyectoDelEquipo proyecto) {
        return EquipoProyecto.armar(proyecto.liderId(), equipos.personas(proyecto.id()),
                consulta.tickets(proyecto.id()), consulta.historias(proyecto.id()));
    }

    private ProyectoDelEquipo verificar(Long proyectoId, Long usuarioId, boolean esAdmin) {
        if (!consulta.visible(proyectoId, usuarioId, esAdmin)) {
            throw new NoEncontradoException("No encontramos ese proyecto");
        }
        return equipos.proyecto(proyectoId).orElseThrow(() -> new NoEncontradoException("No encontramos ese proyecto"));
    }

    private ProyectoDelEquipo verificarGestion(Long proyectoId, Long usuarioId, boolean esAdmin) {
        ProyectoDelEquipo proyecto = verificar(proyectoId, usuarioId, esAdmin);
        if (!puedeGestionar(proyecto, usuarioId, esAdmin)) {
            throw new AccesoDenegadoException("Solo un Administrador arma el equipo del proyecto");
        }
        return proyecto;
    }

    private boolean puedeGestionar(ProyectoDelEquipo proyecto, Long usuarioId, boolean esAdmin) {
        return (esAdmin || usuarioId.equals(proyecto.liderId())) && permisos.puede(usuarioId, PermisosPort.ARMAR_EQUIPO);
    }
}
