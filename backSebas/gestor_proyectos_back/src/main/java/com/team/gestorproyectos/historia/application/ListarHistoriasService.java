package com.team.gestorproyectos.historia.application;

import com.team.gestorproyectos.historia.domain.model.HistoriasDeFase;
import com.team.gestorproyectos.historia.domain.model.ListadoHistorias;
import com.team.gestorproyectos.historia.domain.port.in.ListarHistoriasUseCase;
import com.team.gestorproyectos.historia.domain.port.out.HistoriaListadoPort;
import com.team.gestorproyectos.historia.domain.port.out.HistoriaRepositoryPort;
import com.team.gestorproyectos.shared.application.ServicioAplicacion;
import com.team.gestorproyectos.shared.application.Transaccional;
import com.team.gestorproyectos.shared.domain.exception.NoEncontradoException;
import com.team.gestorproyectos.shared.domain.port.PermisosPort;

import lombok.RequiredArgsConstructor;

@ServicioAplicacion
@RequiredArgsConstructor
public class ListarHistoriasService implements ListarHistoriasUseCase {

    private final HistoriaRepositoryPort proyectos;
    private final HistoriaListadoPort historias;
    private final PermisosPort permisos;

    @Override
    @Transaccional(soloLectura = true)
    public ListadoHistorias listar(Long proyectoId, Long usuarioId, boolean esAdmin) {
        String nombre = proyectos.proyectoVisible(proyectoId, usuarioId, esAdmin)
                .orElseThrow(() -> new NoEncontradoException("No encontramos ese proyecto"));
        return new ListadoHistorias(proyectoId, nombre, historias.historias(proyectoId),
                permisos.puede(usuarioId, PermisosPort.CREAR_HISTORIAS));
    }

    @Override
    @Transaccional(soloLectura = true)
    public HistoriasDeFase deFase(Long proyectoId, Long faseId, Long usuarioId, boolean esAdmin) {
        proyectos.proyectoVisible(proyectoId, usuarioId, esAdmin)
                .orElseThrow(() -> new NoEncontradoException("No encontramos ese proyecto"));
        return historias.deFase(proyectoId, faseId)
                .orElseThrow(() -> new NoEncontradoException("No encontramos esa fase"));
    }
}
