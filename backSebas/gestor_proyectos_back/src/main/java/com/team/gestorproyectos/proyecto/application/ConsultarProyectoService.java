package com.team.gestorproyectos.proyecto.application;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.team.gestorproyectos.proyecto.domain.model.ArbolProyecto;
import com.team.gestorproyectos.proyecto.domain.model.FaseArbol;
import com.team.gestorproyectos.proyecto.domain.model.FaseResumen;
import com.team.gestorproyectos.proyecto.domain.model.InfoProyecto;
import com.team.gestorproyectos.proyecto.domain.model.ResumenProyecto;
import com.team.gestorproyectos.proyecto.domain.model.Ritmo;
import com.team.gestorproyectos.proyecto.domain.port.in.ConsultarProyectoUseCase;
import com.team.gestorproyectos.proyecto.domain.port.out.ProyectoConsultaPort;
import com.team.gestorproyectos.proyecto.domain.port.out.ProyectoConsultaPort.MetricasTickets;
import com.team.gestorproyectos.shared.application.ServicioAplicacion;
import com.team.gestorproyectos.shared.application.Transaccional;
import com.team.gestorproyectos.shared.domain.exception.NoEncontradoException;
import com.team.gestorproyectos.shared.domain.model.ConteoTickets;
import com.team.gestorproyectos.shared.domain.model.Persona;
import com.team.gestorproyectos.shared.domain.model.Progreso;
import com.team.gestorproyectos.shared.domain.port.PermisosPort;

import lombok.RequiredArgsConstructor;

@ServicioAplicacion
@RequiredArgsConstructor
public class ConsultarProyectoService implements ConsultarProyectoUseCase {

    /** En QA más de esto ya cuenta como detenido. */
    private static final int DIAS_EN_QA_DETENIDO = 3;

    private final ProyectoConsultaPort consulta;
    private final PermisosPort permisos;

    @Override
    @Transaccional(soloLectura = true)
    public ResumenProyecto resumen(Long proyectoId, Long usuarioId, boolean esAdmin) {
        InfoProyecto info = verificar(proyectoId, usuarioId, esAdmin);
        List<ConteoTickets> conteos = consulta.conteos(List.of(proyectoId)).getOrDefault(proyectoId, List.of());
        List<FaseResumen> fases = fases(proyectoId, conteos);
        FaseResumen actual = ResumenProyecto.faseActual(fases).orElse(null);
        List<ConteoTickets> deLaActual = actual == null ? List.of()
                : conteos.stream().filter(c -> actual.id().equals(c.faseId())).toList();
        Progreso progresoActual = Progreso.de(deLaActual);

        LocalDateTime ahora = LocalDateTime.now();
        MetricasTickets metricas = consulta.metricas(proyectoId);
        return new ResumenProyecto(
                info,
                Progreso.de(conteos).porcentaje(),
                consulta.modulos(List.of(proyectoId)).getOrDefault(proyectoId, List.of()),
                consulta.equipos(List.of(proyectoId)).getOrDefault(proyectoId, List.of()),
                fases,
                actual,
                metricas.ticketsAbiertos(),
                metricas.bugsAbiertos(),
                metricas.bugsEnProduccion(),
                metricas.mantenimientoAbiertos(),
                progresoActual.terminados(),
                progresoActual.total(),
                actual == null ? Ritmo.SIN_FECHAS
                        : Ritmo.de(actual.fechaInicio(), actual.fechaFin(), actual.avance(), LocalDate.now()),
                consulta.salud(proyectoId, LocalDate.now().withDayOfMonth(1).atStartOfDay(), ahora.minusDays(30)),
                consulta.detenidos(proyectoId, ahora.minusDays(DIAS_EN_QA_DETENIDO)),
                puedeEditar(info, usuarioId, esAdmin),
                permisos.puede(usuarioId, PermisosPort.CREAR_FASES),
                permisos.puede(usuarioId, PermisosPort.CREAR_HISTORIAS),
                permisos.puede(usuarioId, PermisosPort.CREAR_TICKETS));
    }

    @Override
    @Transaccional(soloLectura = true)
    public List<FaseArbol> arbol(Long proyectoId, Long usuarioId, boolean esAdmin) {
        verificar(proyectoId, usuarioId, esAdmin);
        List<ConteoTickets> conteos = consulta.conteos(List.of(proyectoId)).getOrDefault(proyectoId, List.of());
        Map<Long, Persona> personas = consulta.personasDelEquipo().stream()
                .collect(Collectors.toMap(Persona::id, Function.identity()));
        return ArbolProyecto.armar(usuarioId, fases(proyectoId, conteos), consulta.historias(proyectoId),
                consulta.tickets(proyectoId), personas);
    }

    private InfoProyecto verificar(Long proyectoId, Long usuarioId, boolean esAdmin) {
        if (!consulta.visible(proyectoId, usuarioId, esAdmin)) {
            throw new NoEncontradoException("No encontramos ese proyecto");
        }
        return consulta.info(proyectoId).orElseThrow(() -> new NoEncontradoException("No encontramos ese proyecto"));
    }

    private List<FaseResumen> fases(Long proyectoId, List<ConteoTickets> conteos) {
        return consulta.fases(proyectoId).stream()
                .map(f -> new FaseResumen(f.id(), f.numero(), f.nombre(), f.estado(), f.fechaInicio(), f.fechaFin(),
                        f.versionRelease(), f.mantenimiento(), f.modulos(), f.historias(), f.tickets(),
                        f.mantenimiento() ? 0 : Progreso.deFase(f.id(), f.estado(), conteos)))
                .toList();
    }

    /** Editar pide el permiso y además ser el Líder de este proyecto (el Admin puede todos). */
    private boolean puedeEditar(InfoProyecto info, Long usuarioId, boolean esAdmin) {
        return (esAdmin || usuarioId.equals(info.liderId()))
                && permisos.puede(usuarioId, PermisosPort.EDITAR_PROYECTOS);
    }
}
