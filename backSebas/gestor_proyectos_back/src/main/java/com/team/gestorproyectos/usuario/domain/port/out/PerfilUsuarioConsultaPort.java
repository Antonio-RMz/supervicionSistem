package com.team.gestorproyectos.usuario.domain.port.out;

import java.time.LocalDateTime;
import java.util.List;

import com.team.gestorproyectos.shared.domain.model.Movimiento;
import com.team.gestorproyectos.shared.domain.model.Persona;
import com.team.gestorproyectos.usuario.domain.model.ComentarioHecho;
import com.team.gestorproyectos.shared.domain.model.ItemDeProyecto;
import com.team.gestorproyectos.usuario.domain.model.ProyectoDeUsuario;

/** Lo que el detalle de una persona necesita leer de todos sus proyectos. */
public interface PerfilUsuarioConsultaPort {

    /** Tickets e historias abiertos donde participa o donde es el Líder del proyecto. */
    List<ItemDeProyecto> itemsAbiertos(Long usuarioId);

    /** Todo el equipo, para poner nombre a quien tiene la siguiente acción. */
    List<Persona> personasDelEquipo();

    long ticketsCerradosDesde(Long usuarioId, LocalDateTime desde);

    long rechazosEnQaDesde(Long usuarioId, LocalDateTime desde);

    /** Cambios de estado que hizo él mismo (no los automáticos), lo más reciente primero. */
    List<Movimiento> movimientosDe(Long usuarioId, int limite);

    List<ComentarioHecho> comentariosDe(Long usuarioId, int limite);

    /** Proyectos donde es miembro o Líder. */
    List<ProyectoDeUsuario> proyectosDe(Long usuarioId);
}
