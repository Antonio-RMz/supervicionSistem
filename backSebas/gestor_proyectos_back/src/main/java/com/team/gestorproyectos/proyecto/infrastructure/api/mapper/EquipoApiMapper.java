package com.team.gestorproyectos.proyecto.infrastructure.api.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.team.gestorproyectos.proyecto.domain.model.EquipoProyecto.MiembroEquipo;
import com.team.gestorproyectos.proyecto.domain.model.VistaEquipo;
import com.team.gestorproyectos.proyecto.domain.model.VistaEquipo.Candidato;
import com.team.gestorproyectos.proyecto.infrastructure.api.dto.EquipoDtos.CandidatoResponse;
import com.team.gestorproyectos.proyecto.infrastructure.api.dto.EquipoDtos.EquipoResponse;
import com.team.gestorproyectos.proyecto.infrastructure.api.dto.EquipoDtos.MiembroResponse;

@Mapper(componentModel = "spring")
public interface EquipoApiMapper {

    @Mapping(target = "miembros", source = "equipo.miembros")
    @Mapping(target = "cobertura", source = "equipo.cobertura")
    @Mapping(target = "alertas", source = "equipo.alertas")
    EquipoResponse aEquipo(VistaEquipo vista);

    List<CandidatoResponse> aCandidatos(List<Candidato> candidatos);

    default MiembroResponse aMiembro(MiembroEquipo m) {
        var p = m.persona();
        return new MiembroResponse(p.id(), p.nombreCompleto(), p.nombreCorto(), p.iniciales(), p.roles(),
                p.ultimoAcceso(), m.esLider(), m.modulos().stream().map(Enum::name).toList(), m.ticketsAbiertos(),
                m.carga().name(), m.leToca());
    }
}
