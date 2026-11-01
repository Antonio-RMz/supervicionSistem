package com.team.gestorproyectos.proyecto.application;

import java.util.List;

import com.team.gestorproyectos.proyecto.domain.model.ReleasesProyecto;
import com.team.gestorproyectos.proyecto.domain.model.ReleasesProyecto.ParcheEnPreparacion;
import com.team.gestorproyectos.proyecto.domain.model.ReleasesProyecto.Publicado;
import com.team.gestorproyectos.proyecto.domain.port.in.ConsultarReleasesUseCase;
import com.team.gestorproyectos.proyecto.domain.port.out.EquipoProyectoPort;
import com.team.gestorproyectos.proyecto.domain.port.out.EquipoProyectoPort.ProyectoDelEquipo;
import com.team.gestorproyectos.proyecto.domain.port.out.ProyectoConsultaPort;
import com.team.gestorproyectos.proyecto.domain.port.out.ReleasesProyectoPort;
import com.team.gestorproyectos.proyecto.domain.port.out.ReleasesProyectoPort.HotfixAbiertos;
import com.team.gestorproyectos.shared.application.ServicioAplicacion;
import com.team.gestorproyectos.shared.application.Transaccional;
import com.team.gestorproyectos.shared.domain.exception.NoEncontradoException;
import com.team.gestorproyectos.shared.domain.port.PermisosPort;
import com.team.gestorproyectos.ticket.domain.model.Parche;

import lombok.RequiredArgsConstructor;

@ServicioAplicacion
@RequiredArgsConstructor
public class ConsultarReleasesService implements ConsultarReleasesUseCase {

    private final ReleasesProyectoPort releases;
    private final EquipoProyectoPort equipos;
    private final ProyectoConsultaPort consulta;
    private final PermisosPort permisos;

    @Override
    @Transaccional(soloLectura = true)
    public ReleasesProyecto releases(Long proyectoId, Long usuarioId, boolean esAdmin) {
        if (!consulta.visible(proyectoId, usuarioId, esAdmin)) {
            throw new NoEncontradoException("No encontramos ese proyecto");
        }
        ProyectoDelEquipo proyecto = equipos.proyecto(proyectoId)
                .orElseThrow(() -> new NoEncontradoException("No encontramos ese proyecto"));
        List<Publicado> publicados = ReleasesProyecto.marcarProduccion(releases.publicados(proyectoId));
        String enProduccion = publicados.stream().filter(Publicado::enProduccion).map(Publicado::version)
                .findFirst().orElse(null);
        HotfixAbiertos hotfix = releases.hotfixAbiertos(proyectoId);
        ParcheEnPreparacion parche = hotfix.aprobados() + hotfix.enCamino() == 0 ? null
                : new ParcheEnPreparacion(Parche.siguienteVersion(enProduccion), hotfix.aprobados(), hotfix.enCamino(),
                        releases.faseMantenimiento(proyectoId).orElse(null));
        boolean puedeIntegrar = (esAdmin || usuarioId.equals(proyecto.liderId()))
                && permisos.puede(usuarioId, PermisosPort.INTEGRAR_HISTORIAS);
        return new ReleasesProyecto(releases.historiasAceptadas(proyectoId), puedeIntegrar,
                releases.proximaFase(proyectoId).orElse(null), parche, publicados);
    }
}
