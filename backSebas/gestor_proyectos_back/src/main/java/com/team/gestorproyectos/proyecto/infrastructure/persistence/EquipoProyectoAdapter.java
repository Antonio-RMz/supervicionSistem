package com.team.gestorproyectos.proyecto.infrastructure.persistence;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.team.gestorproyectos.proyecto.domain.model.EquipoProyecto.Persona;
import com.team.gestorproyectos.proyecto.domain.model.EquipoProyecto.Reasignacion;
import com.team.gestorproyectos.proyecto.domain.model.ReglasFlujo;
import com.team.gestorproyectos.proyecto.domain.model.VistaEquipo.Candidato;
import com.team.gestorproyectos.proyecto.domain.model.VistaEquipo.PermisoDelProyecto;
import com.team.gestorproyectos.proyecto.domain.port.out.EquipoProyectoPort;
import com.team.gestorproyectos.proyecto.infrastructure.persistence.repository.EquipoProyectoJpaRepository;
import com.team.gestorproyectos.proyecto.infrastructure.persistence.repository.EquipoProyectoJpaRepository.PersonaFila;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class EquipoProyectoAdapter implements EquipoProyectoPort {

    private final EquipoProyectoJpaRepository repository;

    @Override
    public Optional<ProyectoDelEquipo> proyecto(Long proyectoId) {
        return repository.proyecto(proyectoId).map(p -> new ProyectoDelEquipo(p.getId(), p.getLiderId(),
                new ReglasFlujo(si(p.getPermitirAutovalidacion()), si(p.getComentarioAlRechazar()),
                        si(p.getRamaAlIniciar()), si(p.getNotificarClienteFase()))));
    }

    @Override
    public List<Persona> personas(Long proyectoId) {
        return repository.personas(proyectoId).stream().map(EquipoProyectoAdapter::aPersona).toList();
    }

    @Override
    public List<PermisoDelProyecto> permisosExtra(Long proyectoId) {
        return repository.permisosExtra(proyectoId).stream()
                .map(p -> new PermisoDelProyecto(p.getPermiso(), p.getUsuarioId(), p.getUsuario(), si(p.getOtorgado())))
                .toList();
    }

    @Override
    public List<Candidato> candidatos(Long proyectoId) {
        return repository.candidatos(proyectoId).stream().map(EquipoProyectoAdapter::aPersona)
                .map(p -> new Candidato(p.id(), p.nombreCompleto(), p.roles())).toList();
    }

    @Override
    public void agregar(Long proyectoId, Long usuarioId) {
        repository.agregar(proyectoId, usuarioId, LocalDateTime.now());
    }

    @Override
    public void quitar(Long proyectoId, Long usuarioId) {
        repository.quitar(proyectoId, usuarioId);
    }

    @Override
    public void reasignar(Reasignacion r, Long usuarioId) {
        LocalDateTime ahora = LocalDateTime.now();
        String de = repository.nombreCorto(r.deId());
        String quien = repository.nombreCorto(usuarioId);
        if (!r.comoResponsable().isEmpty()) {
            repository.cambiarResponsable(r.comoResponsable(), r.deId(), r.responsableNuevoId());
            String texto = "Reasignado por " + quien + ": lo trabajaba " + de + ", ahora "
                    + repository.nombreCorto(r.responsableNuevoId()) + ".";
            r.comoResponsable().forEach(id -> repository.comentar(id, usuarioId, texto, ahora));
        }
        if (!r.comoTester().isEmpty()) {
            repository.cambiarTester(r.comoTester(), r.deId(), r.testerNuevoId());
            String texto = "Reasignado por " + quien + ": lo probaba " + de + ", ahora "
                    + repository.nombreCorto(r.testerNuevoId()) + ".";
            r.comoTester().forEach(id -> repository.comentar(id, usuarioId, texto, ahora));
        }
    }

    private static Persona aPersona(PersonaFila f) {
        return new Persona(f.getId(), f.getNombre(), f.getApellidos(),
                f.getRoles() == null ? List.of() : List.of(f.getRoles().split(",")), f.getUltimoAcceso());
    }

    private static boolean si(Boolean valor) {
        return Boolean.TRUE.equals(valor);
    }
}
