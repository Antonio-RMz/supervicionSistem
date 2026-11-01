package com.team.gestorproyectos.proyecto.infrastructure.api.mapper;

import org.mapstruct.Mapper;

import com.team.gestorproyectos.proyecto.domain.model.ModuloProyecto;
import com.team.gestorproyectos.proyecto.domain.model.ModulosProyecto;
import com.team.gestorproyectos.proyecto.domain.model.ModulosProyecto.Ambiente;
import com.team.gestorproyectos.proyecto.domain.model.ModulosProyecto.Build;
import com.team.gestorproyectos.proyecto.domain.model.ModulosProyecto.Responsable;
import com.team.gestorproyectos.proyecto.domain.model.ModulosProyecto.VistaModulo;
import com.team.gestorproyectos.proyecto.infrastructure.api.dto.ModulosDtos.AmbienteResponse;
import com.team.gestorproyectos.proyecto.infrastructure.api.dto.ModulosDtos.BuildResponse;
import com.team.gestorproyectos.proyecto.infrastructure.api.dto.ModulosDtos.ModuloRequest;
import com.team.gestorproyectos.proyecto.infrastructure.api.dto.ModulosDtos.ModuloResponse;
import com.team.gestorproyectos.proyecto.infrastructure.api.dto.ModulosDtos.ModulosResponse;
import com.team.gestorproyectos.proyecto.infrastructure.api.dto.ModulosDtos.ResponsableResponse;

@Mapper(componentModel = "spring")
public interface ModulosApiMapper {

    ModulosResponse aModulos(ModulosProyecto modulos);

    default ModuloResponse aModulo(VistaModulo v) {
        ModuloProyecto m = v.modulo();
        return new ModuloResponse(m.id(), m.tipo().name(), m.nombre(), m.tecnologia(), v.tecnologias(), m.repositorio(),
                m.ramaBase(), aResponsable(v.responsable()), v.ambientes().stream().map(this::aAmbiente).toList(),
                aBuild(v.ultimoBuild()), v.porHacer(), v.enProgreso(), v.enQa(), v.bugsProduccion(), v.conTrabajo());
    }

    ResponsableResponse aResponsable(Responsable responsable);

    AmbienteResponse aAmbiente(Ambiente ambiente);

    BuildResponse aBuild(Build build);

    default ModuloProyecto aModulo(Long id, ModuloRequest r) {
        return new ModuloProyecto(id, r.tipo(), r.nombre(), r.tecnologia(), r.repositorio(), r.ramaBase());
    }
}
