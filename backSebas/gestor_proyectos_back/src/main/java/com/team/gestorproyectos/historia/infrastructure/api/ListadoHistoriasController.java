package com.team.gestorproyectos.historia.infrastructure.api;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.team.gestorproyectos.historia.domain.port.in.ListarHistoriasUseCase;
import com.team.gestorproyectos.historia.infrastructure.api.dto.ListadoHistoriasDtos.HistoriasDeFaseResponse;
import com.team.gestorproyectos.historia.infrastructure.api.dto.ListadoHistoriasDtos.ListadoHistoriasResponse;
import com.team.gestorproyectos.historia.infrastructure.api.mapper.ListadoHistoriasApiMapper;
import com.team.gestorproyectos.shared.infrastructure.security.UsuarioSesion;

import lombok.RequiredArgsConstructor;

/** Menú Historias: todas las historias de un proyecto, para buscarlas y abrir cada una. */
@RestController
@RequestMapping("/api/equipo/proyectos/{proyectoId}/historias")
@RequiredArgsConstructor
public class ListadoHistoriasController {

    private final ListarHistoriasUseCase listarHistorias;
    private final ListadoHistoriasApiMapper mapper;

    @GetMapping
    public ListadoHistoriasResponse listar(@PathVariable("proyectoId") Long proyectoId,
                                           @AuthenticationPrincipal UsuarioSesion sesion) {
        return mapper.aListado(listarHistorias.listar(proyectoId, sesion.id(), sesion.esAdmin()));
    }

    /** Las historias de una fase completas, para copiarlas como JSON desde Importar historias. */
    @GetMapping(params = "fase")
    public HistoriasDeFaseResponse deFase(@PathVariable("proyectoId") Long proyectoId,
                                          @RequestParam("fase") Long faseId,
                                          @AuthenticationPrincipal UsuarioSesion sesion) {
        return mapper.aHistoriasDeFase(listarHistorias.deFase(proyectoId, faseId, sesion.id(), sesion.esAdmin()));
    }
}
