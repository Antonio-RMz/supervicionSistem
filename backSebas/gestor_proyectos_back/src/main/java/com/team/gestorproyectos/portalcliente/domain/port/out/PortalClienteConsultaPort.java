package com.team.gestorproyectos.portalcliente.domain.port.out;

import java.util.List;
import java.util.Optional;

import com.team.gestorproyectos.shared.domain.model.ConteoTickets;
import com.team.gestorproyectos.portalcliente.domain.model.Contacto;
import com.team.gestorproyectos.portalcliente.domain.model.DemoPendiente;
import com.team.gestorproyectos.portalcliente.domain.model.Fase;
import com.team.gestorproyectos.portalcliente.domain.model.HistoriaCliente;
import com.team.gestorproyectos.portalcliente.domain.model.ProyectoBase;
import com.team.gestorproyectos.portalcliente.domain.model.VersionEntregada;

/** Lo que el portal necesita leer de la base de datos. */
public interface PortalClienteConsultaPort {

    List<ProyectoBase> proyectosDelCliente(Long clienteId);

    Optional<ProyectoBase> proyectoDelCliente(Long proyectoId, Long clienteId);

    /** Fases normales en orden (sin la de Mantenimiento). */
    List<Fase> fasesDelProyecto(Long proyectoId);

    List<ConteoTickets> conteoDeTickets(Long proyectoId);

    /** Sin las descartadas. */
    List<HistoriaCliente> historiasDeLaFase(Long faseId);

    /** De la más nueva a la más vieja. */
    List<VersionEntregada> versionesEntregadas(Long proyectoId);

    Optional<Contacto> contactoDelProyecto(Long proyectoId);

    List<DemoPendiente> demosPendientes(Long proyectoId);
}
