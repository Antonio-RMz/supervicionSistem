package com.team.gestorproyectos.dashboard.domain.port.out;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.team.gestorproyectos.dashboard.domain.model.ConteoCierres;
import com.team.gestorproyectos.shared.domain.model.ItemTrabajo;
import com.team.gestorproyectos.shared.domain.model.Movimiento;
import com.team.gestorproyectos.shared.domain.model.Persona;
import com.team.gestorproyectos.dashboard.domain.model.ProyectoDashboard;
import com.team.gestorproyectos.shared.domain.model.ConteoTickets;

/** Lo que el Dashboard necesita leer de la base de datos. */
public interface DashboardConsultaPort {

    /** Vacío si no existe o si el usuario no es miembro (y no es Admin). */
    Optional<ProyectoDashboard> proyectoVisible(Long proyectoId, Long usuarioId, boolean esAdmin);

    /** Tickets e historias que no están cerrados, cancelados ni descartados. */
    List<ItemTrabajo> itemsAbiertos(Long proyectoId);

    /** Miembros del proyecto y su Líder. */
    List<Persona> personasDelProyecto(Long proyectoId);

    /** Fases en orden, sin Mantenimiento ni canceladas. Con id, número, nombre y estado. */
    List<FaseBasica> fases(Long proyectoId);

    List<ConteoTickets> conteoDeTickets(Long proyectoId);

    ConteoCierres cierresDesde(Long proyectoId, LocalDateTime desde);

    List<Movimiento> ultimosMovimientos(Long proyectoId, int limite);

    record FaseBasica(Long id, int numero, String nombre, String estado) {
    }
}
