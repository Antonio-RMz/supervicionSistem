package com.team.gestorproyectos.proyecto.domain.model;

import java.util.List;

/** Una tarjeta de la lista de Proyectos (PDF pág. 13). */
public record ProyectoTarjeta(
        Long id,
        String codigo,
        String nombre,
        String estado,
        String prioridad,
        Long clienteId,
        String clienteNombre,
        List<TipoModulo> modulos,
        int avance,
        long fases,
        long historias,
        long tickets,
        List<Miembro> equipo,
        String liderNombre) {
}
