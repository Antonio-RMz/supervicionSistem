package com.team.gestorproyectos.usuario.infrastructure.persistence;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.team.gestorproyectos.usuario.domain.model.Accesos;
import com.team.gestorproyectos.usuario.domain.model.NuevoUsuario;
import com.team.gestorproyectos.usuario.domain.model.PermisoExtra;
import com.team.gestorproyectos.usuario.domain.model.TipoUsuario;
import com.team.gestorproyectos.usuario.domain.model.Usuario;
import com.team.gestorproyectos.usuario.domain.model.UsuarioConProyectos;
import com.team.gestorproyectos.usuario.domain.port.out.UsuarioRepositoryPort;
import com.team.gestorproyectos.usuario.infrastructure.persistence.entity.PermisoEntity;
import com.team.gestorproyectos.usuario.infrastructure.persistence.entity.UsuarioEntity;
import com.team.gestorproyectos.usuario.infrastructure.persistence.entity.UsuarioPermisoEntity;
import com.team.gestorproyectos.usuario.infrastructure.persistence.mapper.UsuarioEntityMapper;
import com.team.gestorproyectos.usuario.infrastructure.persistence.repository.PermisoJpaRepository;
import com.team.gestorproyectos.usuario.infrastructure.persistence.repository.RolJpaRepository;
import com.team.gestorproyectos.usuario.infrastructure.persistence.repository.UsuarioJpaRepository;
import com.team.gestorproyectos.usuario.infrastructure.persistence.repository.UsuarioJpaRepository.ProyectosPorUsuarioFila;
import com.team.gestorproyectos.usuario.infrastructure.persistence.repository.UsuarioPermisoJpaRepository;

import lombok.RequiredArgsConstructor;

/** Adaptador de salida: cumple el puerto del dominio usando Spring Data JPA. */
@Component
@RequiredArgsConstructor
public class UsuarioRepositoryAdapter implements UsuarioRepositoryPort {

    private static final String EQUIPO = TipoUsuario.EQUIPO.name();

    private final UsuarioJpaRepository repository;
    private final RolJpaRepository roles;
    private final PermisoJpaRepository permisos;
    private final UsuarioPermisoJpaRepository permisosExtra;
    private final UsuarioEntityMapper mapper;

    @Override
    public Optional<Usuario> buscarPorCorreo(String correo) {
        return repository.findByCorreo(correo).map(mapper::aDominio);
    }

    @Override
    public void registrarAcceso(Long usuarioId) {
        repository.actualizarUltimoAcceso(usuarioId, LocalDateTime.now());
    }

    @Override
    public Optional<Usuario> buscarDelEquipo(Long usuarioId) {
        return repository.findByIdAndTipo(usuarioId, EQUIPO).map(mapper::aDominio);
    }

    @Override
    public List<UsuarioConProyectos> listarEquipo() {
        Map<Long, Long> proyectos = repository.contarProyectos().stream()
                .collect(Collectors.toMap(ProyectosPorUsuarioFila::getUsuarioId, ProyectosPorUsuarioFila::getProyectos));
        return repository.findByTipoOrderByNombreAscApellidosAsc(EQUIPO).stream()
                .map(entity -> new UsuarioConProyectos(mapper.aDominio(entity), proyectos.getOrDefault(entity.getId(), 0L)))
                .toList();
    }

    @Override
    public boolean existeCorreo(String correo) {
        return repository.existsByCorreo(correo);
    }

    @Override
    public Long crear(NuevoUsuario nuevo, String passwordHash) {
        UsuarioEntity entity = new UsuarioEntity();
        entity.setNombre(nuevo.nombre());
        entity.setApellidos(nuevo.apellidos());
        entity.setCorreo(nuevo.correo());
        entity.setPasswordHash(passwordHash);
        entity.setTipo(EQUIPO);
        entity.setActivo(nuevo.activo());
        entity.setCambiarPassword(nuevo.cambiarPassword());
        entity.setRoles(new HashSet<>(roles.findByCodigoIn(nuevo.roles())));
        Long id = repository.saveAndFlush(entity).getId();

        nuevo.proyectoIds().forEach(proyectoId -> repository.agregarAProyecto(proyectoId, id));
        return id;
    }

    @Override
    public Accesos accesos(Long usuarioId) {
        UsuarioEntity entity = repository.findById(usuarioId).orElseThrow();
        return new Accesos(mapper.codigosDeRol(entity.getRoles()),
                mapper.aPermisosExtra(permisosExtra.buscarDeUsuario(usuarioId)));
    }

    @Override
    public void guardarAccesos(Long usuarioId, Accesos accesos) {
        UsuarioEntity entity = repository.findById(usuarioId).orElseThrow();
        entity.setRoles(new HashSet<>(roles.findByCodigoIn(accesos.roles())));

        Map<String, Long> idsDePermiso = permisos.findByCodigoIn(
                        accesos.permisosExtra().stream().map(PermisoExtra::permisoCodigo).toList()).stream()
                .collect(Collectors.toMap(PermisoEntity::getCodigo, PermisoEntity::getId));
        permisosExtra.borrarDeUsuario(usuarioId);
        permisosExtra.saveAll(accesos.permisosExtra().stream()
                .map(p -> new UsuarioPermisoEntity(null, usuarioId, idsDePermiso.get(p.permisoCodigo()),
                        p.proyectoId(), p.otorgado()))
                .toList());
        repository.flush();
    }

    @Override
    public void cambiarActivo(Long usuarioId, boolean activo) {
        repository.actualizarActivo(usuarioId, activo);
    }
}
