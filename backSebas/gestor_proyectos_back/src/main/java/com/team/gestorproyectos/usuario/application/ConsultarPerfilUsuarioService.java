package com.team.gestorproyectos.usuario.application;

import java.time.LocalDate;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.team.gestorproyectos.shared.application.ServicioAplicacion;
import com.team.gestorproyectos.shared.application.Transaccional;
import com.team.gestorproyectos.shared.domain.exception.NoEncontradoException;
import com.team.gestorproyectos.shared.domain.model.Persona;
import com.team.gestorproyectos.usuario.domain.model.PerfilUsuario;
import com.team.gestorproyectos.usuario.domain.model.TrabajoActual;
import com.team.gestorproyectos.usuario.domain.model.Usuario;
import com.team.gestorproyectos.usuario.domain.port.in.ConsultarPerfilUsuarioUseCase;
import com.team.gestorproyectos.usuario.domain.port.out.PerfilUsuarioConsultaPort;
import com.team.gestorproyectos.usuario.domain.port.out.UsuarioRepositoryPort;

import lombok.RequiredArgsConstructor;

@ServicioAplicacion
@RequiredArgsConstructor
public class ConsultarPerfilUsuarioService implements ConsultarPerfilUsuarioUseCase {

    private static final int HECHOS_RECIENTES = 8;

    private final UsuarioRepositoryPort usuarios;
    private final PerfilUsuarioConsultaPort consulta;

    @Override
    @Transaccional(soloLectura = true)
    public PerfilUsuario consultar(Long usuarioId) {
        Usuario usuario = usuarios.buscarDelEquipo(usuarioId)
                .orElseThrow(() -> new NoEncontradoException("No encontramos a esa persona"));

        Map<Long, Persona> personas = consulta.personasDelEquipo().stream()
                .collect(Collectors.toMap(Persona::id, Function.identity()));
        var inicioDeMes = LocalDate.now().withDayOfMonth(1).atStartOfDay();

        return new PerfilUsuario(
                usuario,
                TrabajoActual.armar(usuarioId, consulta.itemsAbiertos(usuarioId), personas),
                consulta.ticketsCerradosDesde(usuarioId, inicioDeMes),
                consulta.rechazosEnQaDesde(usuarioId, inicioDeMes),
                PerfilUsuario.lineaDeTiempo(
                        consulta.movimientosDe(usuarioId, HECHOS_RECIENTES),
                        consulta.comentariosDe(usuarioId, HECHOS_RECIENTES),
                        HECHOS_RECIENTES),
                consulta.proyectosDe(usuarioId));
    }
}
