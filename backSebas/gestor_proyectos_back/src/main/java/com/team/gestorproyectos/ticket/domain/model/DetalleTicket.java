package com.team.gestorproyectos.ticket.domain.model;

import java.util.List;

import com.team.gestorproyectos.ticket.domain.model.PreparacionTicket.PersonaDeEquipo;
import com.team.gestorproyectos.ticket.domain.model.PreparacionTicket.TicketAbierto;
import com.team.gestorproyectos.ticket.domain.model.TicketParaMover.Opcion;

/**
 * El detalle del ticket (PDF pág. 29) con el panel de Acciones de quien lo ve (pág. 22).
 *
 * @param actor               quién tiene la acción principal: "Desarrollador", "Analista", "Líder" o "Nadie"
 * @param meToca              si quien lo ve tiene esa acción
 * @param puedeMarcarSubtareas las marca quien lo trabaja
 * @param puedeEditarSubtareas las agregan y quitan quien lo trabaja o el Líder
 * @param puedeEditar          sigue sin empezar y es de quien lo creó o del Líder (mismo formulario que Nuevo ticket)
 * @param bloqueadores        tickets abiertos del proyecto que pueden bloquearlo (Marcar bloqueada)
 * @param equipo              quién puede destrabarlo (y a quién asignar un bug al confirmarlo)
 * @param severidad           de un bug: CRITICA, ALTA, MEDIA o BAJA; null si no tiene
 * @param hotfix              bug de producción que sale en parche, sin esperar a una fase (PDF pág. 23)
 * @param requierePr          la Actividad termina con PR; false en un proyecto Normal sin PR (se termina)
 * @param parche              si es un hotfix aprobado: la versión y los bugs que saldrían con él
 */
public record DetalleTicket(
        InfoTicket info,
        String severidad,
        boolean hotfix,
        boolean requierePr,
        Parche parche,
        String actor,
        boolean meToca,
        List<Opcion> opciones,
        boolean puedeMarcarSubtareas,
        boolean puedeEditarSubtareas,
        boolean puedeEditar,
        List<TicketAbierto> bloqueadores,
        List<PersonaDeEquipo> equipo) {
}
