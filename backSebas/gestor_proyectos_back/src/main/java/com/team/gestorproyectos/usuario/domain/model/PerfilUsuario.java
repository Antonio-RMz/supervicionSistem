package com.team.gestorproyectos.usuario.domain.model;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import com.team.gestorproyectos.shared.domain.model.Movimiento;

/**
 * Detalle de una persona del equipo (PDF pág. 7).
 *
 * @param cerradosEsteMes        tickets suyos que se cerraron este mes
 * @param rechazadosEnQaEsteMes  veces que QA le regresó un ticket este mes
 * @param actividad              lo más reciente primero
 */
public record PerfilUsuario(
        Usuario usuario,
        TrabajoActual trabajo,
        long cerradosEsteMes,
        long rechazadosEnQaEsteMes,
        List<Hecho> actividad,
        List<ProyectoDeUsuario> proyectos,
        String usuarioGit) {

    /** Junta cambios de estado y comentarios en una sola línea de tiempo y deja los más recientes. */
    public static List<Hecho> lineaDeTiempo(List<Movimiento> movimientos, List<ComentarioHecho> comentarios, int limite) {
        return Stream.concat(
                        movimientos.stream().map(m -> new Hecho(m.queHizo(), m.clase().name(), m.fecha())),
                        comentarios.stream().map(ComentarioHecho::comoHecho))
                .sorted(Comparator.comparing(Hecho::fecha, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(limite)
                .toList();
    }
}
