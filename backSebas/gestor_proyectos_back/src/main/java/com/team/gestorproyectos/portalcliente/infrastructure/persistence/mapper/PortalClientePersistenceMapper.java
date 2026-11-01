package com.team.gestorproyectos.portalcliente.infrastructure.persistence.mapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.team.gestorproyectos.shared.domain.model.ConteoTickets;
import com.team.gestorproyectos.portalcliente.domain.model.Contacto;
import com.team.gestorproyectos.portalcliente.domain.model.DemoPendiente;
import com.team.gestorproyectos.portalcliente.domain.model.Fase;
import com.team.gestorproyectos.portalcliente.domain.model.HistoriaCliente;
import com.team.gestorproyectos.portalcliente.domain.model.Modulo;
import com.team.gestorproyectos.portalcliente.domain.model.ProyectoBase;
import com.team.gestorproyectos.portalcliente.domain.model.VersionEntregada;
import com.team.gestorproyectos.proyecto.infrastructure.persistence.entity.DemoEntity;
import com.team.gestorproyectos.proyecto.infrastructure.persistence.entity.FaseEntity;
import com.team.gestorproyectos.proyecto.infrastructure.persistence.entity.HistoriaEntity;
import com.team.gestorproyectos.proyecto.infrastructure.persistence.entity.ModuloEntity;
import com.team.gestorproyectos.proyecto.infrastructure.persistence.entity.ProyectoEntity;
import com.team.gestorproyectos.proyecto.infrastructure.persistence.entity.VersionReleaseEntity;
import com.team.gestorproyectos.proyecto.infrastructure.persistence.repository.ConteoTicketsFila;
import com.team.gestorproyectos.proyecto.infrastructure.persistence.repository.ProyectoJpaRepository.ContactoFila;

/** Entidades JPA -> modelos del dominio del portal. Los textos de estado se vuelven enums solos. */
@Mapper(componentModel = "spring")
public interface PortalClientePersistenceMapper {

    @Mapping(target = "id", source = "proyecto.id")
    @Mapping(target = "codigo", source = "proyecto.codigo")
    @Mapping(target = "nombre", source = "proyecto.nombre")
    @Mapping(target = "estado", source = "proyecto.estado")
    @Mapping(target = "modulos", source = "modulos")
    ProyectoBase aProyecto(ProyectoEntity proyecto, List<Modulo> modulos);

    Modulo aModulo(ModuloEntity entity);

    List<Modulo> aModulos(List<ModuloEntity> entities);

    @Mapping(target = "version", source = "versionRelease")
    Fase aFase(FaseEntity entity);

    List<Fase> aFases(List<FaseEntity> entities);

    HistoriaCliente aHistoria(HistoriaEntity entity);

    List<HistoriaCliente> aHistorias(List<HistoriaEntity> entities);

    ConteoTickets aConteo(ConteoTicketsFila fila);

    List<ConteoTickets> aConteos(List<ConteoTicketsFila> filas);

    @Mapping(target = "fecha", source = "publicadoEn")
    VersionEntregada aVersion(VersionReleaseEntity entity);

    List<VersionEntregada> aVersiones(List<VersionReleaseEntity> entities);

    DemoPendiente aDemo(DemoEntity entity);

    List<DemoPendiente> aDemos(List<DemoEntity> entities);

    Contacto aContacto(ContactoFila fila);

    default LocalDate soloFecha(LocalDateTime fechaHora) {
        return fechaHora == null ? null : fechaHora.toLocalDate();
    }
}
