package com.team.gestorproyectos.proyecto.domain.port.in;

import java.util.List;

import com.team.gestorproyectos.proyecto.domain.model.UsuarioDelSistema;
import com.team.gestorproyectos.proyecto.domain.model.UsuariosDelSistema;

/**
 * Pestaña Usuarios del sistema: quiénes usan el software que se construye. La ve todo el equipo del proyecto;
 * la editan su Líder, un Admin o quien crea historias.
 */
public interface UsuariosDelSistemaUseCase {

    UsuariosDelSistema consultar(Long proyectoId, Long usuarioId, boolean esAdmin);

    /** Reemplaza la lista completa, en el orden en que llega. */
    void guardar(Long proyectoId, Long usuarioId, boolean esAdmin, List<UsuarioDelSistema> usuarios);
}
