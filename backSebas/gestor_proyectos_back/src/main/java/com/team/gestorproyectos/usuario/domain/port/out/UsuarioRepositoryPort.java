package com.team.gestorproyectos.usuario.domain.port.out;

import java.util.List;
import java.util.Optional;

import com.team.gestorproyectos.usuario.domain.model.Accesos;
import com.team.gestorproyectos.usuario.domain.model.NuevoUsuario;
import com.team.gestorproyectos.usuario.domain.model.Usuario;
import com.team.gestorproyectos.usuario.domain.model.UsuarioConProyectos;

/** Puerto de salida: lo que el dominio necesita de la base de datos. Lo implementa un adaptador JPA. */
public interface UsuarioRepositoryPort {

    Optional<Usuario> buscarPorCorreo(String correo);

    void registrarAcceso(Long usuarioId);

    /** Solo personas del equipo; vacío si no existe o es un contacto de cliente. */
    Optional<Usuario> buscarDelEquipo(Long usuarioId);

    List<UsuarioConProyectos> listarEquipo();

    boolean existeCorreo(String correo);

    /** Guarda la cuenta, sus roles y sus proyectos. Devuelve el id nuevo. */
    Long crear(NuevoUsuario nuevo, String passwordHash);

    Accesos accesos(Long usuarioId);

    void guardarAccesos(Long usuarioId, Accesos accesos);

    void cambiarActivo(Long usuarioId, boolean activo);
}
