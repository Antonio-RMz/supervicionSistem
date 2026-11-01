package com.team.gestorproyectos.usuario.infrastructure.api;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.team.gestorproyectos.usuario.domain.port.in.ListarUsuariosUseCase;
import com.team.gestorproyectos.usuario.infrastructure.api.dto.UsuariosDtos.UsuarioFilaResponse;
import com.team.gestorproyectos.usuario.infrastructure.api.mapper.UsuariosApiMapper;

import lombok.RequiredArgsConstructor;

/** Las personas del equipo, para elegir Líder y miembros de un proyecto. Lo ve todo el equipo. */
@RestController
@RequestMapping("/api/equipo/personas")
@RequiredArgsConstructor
public class PersonasController {

    private final ListarUsuariosUseCase listarUsuarios;
    private final UsuariosApiMapper mapper;

    @GetMapping
    public List<UsuarioFilaResponse> personas() {
        return mapper.aFilas(listarUsuarios.listar());
    }
}
