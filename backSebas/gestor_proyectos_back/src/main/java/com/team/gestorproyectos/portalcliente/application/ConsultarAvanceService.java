package com.team.gestorproyectos.portalcliente.application;

import java.util.List;

import com.team.gestorproyectos.portalcliente.domain.model.AvanceFase;
import com.team.gestorproyectos.portalcliente.domain.model.AvanceModulo;
import com.team.gestorproyectos.portalcliente.domain.model.AvanceProyecto;
import com.team.gestorproyectos.portalcliente.domain.model.DemoPendiente;
import com.team.gestorproyectos.portalcliente.domain.model.HistoriaCliente;
import com.team.gestorproyectos.portalcliente.domain.model.ProyectoBase;
import com.team.gestorproyectos.portalcliente.domain.model.ProyectoCliente;
import com.team.gestorproyectos.portalcliente.domain.port.in.ConsultarAvanceUseCase;
import com.team.gestorproyectos.portalcliente.domain.port.out.PortalClienteConsultaPort;
import com.team.gestorproyectos.shared.application.ServicioAplicacion;
import com.team.gestorproyectos.shared.application.Transaccional;
import com.team.gestorproyectos.shared.domain.exception.NoEncontradoException;
import com.team.gestorproyectos.shared.domain.model.ConteoTickets;

import lombok.RequiredArgsConstructor;

@ServicioAplicacion
@RequiredArgsConstructor
public class ConsultarAvanceService implements ConsultarAvanceUseCase {

    private final PortalClienteConsultaPort consulta;

    @Override
    @Transaccional(soloLectura = true)
    public AvanceProyecto consultar(Long proyectoId, Long clienteId) {
        // Si el proyecto es de otra empresa respondemos igual que si no existiera.
        ProyectoBase base = consulta.proyectoDelCliente(proyectoId, clienteId)
                .orElseThrow(() -> new NoEncontradoException("No encontramos ese proyecto"));

        List<ConteoTickets> conteos = consulta.conteoDeTickets(proyectoId);
        List<DemoPendiente> demos = consulta.demosPendientes(proyectoId);
        ProyectoCliente proyecto = ProyectoCliente.armar(base, consulta.fasesDelProyecto(proyectoId), conteos, demos.size());

        AvanceFase faseActual = proyecto.faseActual().orElse(null);
        List<AvanceModulo> modulos = List.of();
        List<HistoriaCliente> historias = List.of();
        if (faseActual != null) {
            Long faseId = faseActual.fase().id();
            modulos = AvanceModulo.calcular(base.modulos(), faseId, conteos);
            historias = consulta.historiasDeLaFase(faseId);
        }

        return new AvanceProyecto(
                proyecto,
                faseActual,
                modulos,
                historias,
                consulta.versionesEntregadas(proyectoId),
                consulta.contactoDelProyecto(proyectoId).orElse(null),
                demos.isEmpty() ? null : demos.get(0));
    }
}
