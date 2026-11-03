package com.team.gestorproyectos.historia.domain.model;

import java.util.List;

/**
 * Lo que necesita el formulario de Nueva historia.
 *
 * @param numero     el que tendrá: HU-0019
 * @param analistas  el equipo; quienes tienen rol Analista van primero
 * @param usuariosDelSistema quiénes usan el software que se construye: las opciones del "Como"
 * @param desarrolladores    los del equipo con rol Desarrollador: a quienes les llega la historia
 */
public record PreparacionHistoria(Long proyectoId, String proyectoNombre, int numero, List<FaseParaHistoria> fases,
                                  List<Analista> analistas, List<UsuarioDelSistema> usuariosDelSistema,
                                  List<Analista> desarrolladores) {

    /** Alguien que usa el sistema (Administrador, Alumno...), con lo que hace en él. */
    public record UsuarioDelSistema(String nombre, String descripcion) {
    }
}
