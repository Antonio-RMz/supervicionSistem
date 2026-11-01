package com.team.gestorproyectos.proyecto.application;

import java.util.List;
import java.util.Map;

import com.team.gestorproyectos.proyecto.domain.model.ListadoProyectos;
import com.team.gestorproyectos.proyecto.domain.model.ModuloConCi;
import com.team.gestorproyectos.proyecto.domain.model.ProyectoTarjeta;
import com.team.gestorproyectos.proyecto.domain.port.in.ListarProyectosUseCase;
import com.team.gestorproyectos.proyecto.domain.port.out.ProyectoConsultaPort;
import com.team.gestorproyectos.proyecto.domain.port.out.ProyectoConsultaPort.ProyectoBase;
import com.team.gestorproyectos.shared.application.ServicioAplicacion;
import com.team.gestorproyectos.shared.application.Transaccional;
import com.team.gestorproyectos.shared.domain.model.ConteoTickets;
import com.team.gestorproyectos.shared.domain.model.Progreso;
import com.team.gestorproyectos.shared.domain.port.PermisosPort;

import lombok.RequiredArgsConstructor;

@ServicioAplicacion
@RequiredArgsConstructor
public class ListarProyectosService implements ListarProyectosUseCase {

    private final ProyectoConsultaPort consulta;
    private final PermisosPort permisos;

    @Override
    @Transaccional(soloLectura = true)
    public ListadoProyectos listar(Long usuarioId, boolean esAdmin) {
        List<ProyectoBase> proyectos = consulta.proyectos(esAdmin ? null : usuarioId);
        List<Long> ids = proyectos.stream().map(ProyectoBase::id).toList();
        Map<Long, List<ConteoTickets>> conteos = consulta.conteos(ids);
        Map<Long, List<ModuloConCi>> modulos = consulta.modulos(ids);
        var equipos = consulta.equipos(ids);

        List<ProyectoTarjeta> tarjetas = proyectos.stream().map(p -> {
            List<ConteoTickets> deEste = conteos.getOrDefault(p.id(), List.of());
            return new ProyectoTarjeta(p.id(), p.codigo(), p.nombre(), p.estado(), p.prioridad(), p.clienteId(),
                    p.clienteNombre(),
                    modulos.getOrDefault(p.id(), List.of()).stream()
                            .map(m -> m.modulo().tipo()).distinct().sorted().toList(),
                    Progreso.de(deEste).porcentaje(),
                    p.fases(), p.historias(),
                    deEste.stream().filter(ConteoTickets::cuenta).mapToLong(ConteoTickets::cantidad).sum(),
                    equipos.getOrDefault(p.id(), List.of()),
                    p.liderNombre());
        }).toList();

        return new ListadoProyectos(tarjetas, permisos.puede(usuarioId, PermisosPort.CREAR_PROYECTOS));
    }
}
