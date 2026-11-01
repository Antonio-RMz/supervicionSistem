package com.team.gestorproyectos.portalcliente.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.team.gestorproyectos.shared.domain.model.ConteoTickets;
import com.team.gestorproyectos.portalcliente.domain.model.Contacto;
import com.team.gestorproyectos.portalcliente.domain.model.DemoPendiente;
import com.team.gestorproyectos.portalcliente.domain.model.Fase;
import com.team.gestorproyectos.portalcliente.domain.model.HistoriaCliente;
import com.team.gestorproyectos.portalcliente.domain.model.ProyectoBase;
import com.team.gestorproyectos.portalcliente.domain.model.VersionEntregada;
import com.team.gestorproyectos.portalcliente.domain.port.out.PortalClienteConsultaPort;
import com.team.gestorproyectos.portalcliente.infrastructure.persistence.mapper.PortalClientePersistenceMapper;
import com.team.gestorproyectos.proyecto.infrastructure.persistence.entity.ProyectoEntity;
import com.team.gestorproyectos.proyecto.infrastructure.persistence.repository.DemoJpaRepository;
import com.team.gestorproyectos.proyecto.infrastructure.persistence.repository.FaseJpaRepository;
import com.team.gestorproyectos.proyecto.infrastructure.persistence.repository.HistoriaJpaRepository;
import com.team.gestorproyectos.proyecto.infrastructure.persistence.repository.ModuloJpaRepository;
import com.team.gestorproyectos.proyecto.infrastructure.persistence.repository.ProyectoJpaRepository;
import com.team.gestorproyectos.proyecto.infrastructure.persistence.repository.TicketJpaRepository;
import com.team.gestorproyectos.proyecto.infrastructure.persistence.repository.VersionReleaseJpaRepository;

import lombok.RequiredArgsConstructor;

/** Adaptador de salida del portal: lee con los repositorios JPA del módulo proyecto (solo lectura). */
@Component
@RequiredArgsConstructor
public class PortalClienteConsultaAdapter implements PortalClienteConsultaPort {

    private final ProyectoJpaRepository proyectos;
    private final ModuloJpaRepository modulos;
    private final FaseJpaRepository fases;
    private final HistoriaJpaRepository historias;
    private final TicketJpaRepository tickets;
    private final VersionReleaseJpaRepository versiones;
    private final DemoJpaRepository demos;
    private final PortalClientePersistenceMapper mapper;

    @Override
    public List<ProyectoBase> proyectosDelCliente(Long clienteId) {
        return proyectos.findByClienteIdOrderByFechaInicio(clienteId).stream()
                .map(this::conModulos)
                .toList();
    }

    @Override
    public Optional<ProyectoBase> proyectoDelCliente(Long proyectoId, Long clienteId) {
        return proyectos.findByIdAndClienteId(proyectoId, clienteId).map(this::conModulos);
    }

    @Override
    public List<Fase> fasesDelProyecto(Long proyectoId) {
        return mapper.aFases(fases.findByProyectoIdAndEsMantenimientoFalseOrderByNumero(proyectoId));
    }

    @Override
    public List<ConteoTickets> conteoDeTickets(Long proyectoId) {
        return mapper.aConteos(tickets.contarPorFaseModuloYEstado(proyectoId));
    }

    @Override
    public List<HistoriaCliente> historiasDeLaFase(Long faseId) {
        return mapper.aHistorias(historias.findByFaseIdAndEstadoNotOrderByNumero(faseId, "DESCARTADA"));
    }

    @Override
    public List<VersionEntregada> versionesEntregadas(Long proyectoId) {
        return mapper.aVersiones(versiones.findByProyectoIdAndEstadoOrderByPublicadoEnDesc(proyectoId, "PUBLICADA"));
    }

    @Override
    public Optional<Contacto> contactoDelProyecto(Long proyectoId) {
        return proyectos.buscarAnalista(proyectoId).map(mapper::aContacto);
    }

    @Override
    public List<DemoPendiente> demosPendientes(Long proyectoId) {
        return mapper.aDemos(demos.buscarPendientesDelProyecto(proyectoId));
    }

    private ProyectoBase conModulos(ProyectoEntity proyecto) {
        return mapper.aProyecto(proyecto, mapper.aModulos(modulos.findByProyectoIdOrderById(proyecto.getId())));
    }
}
