package com.team.gestorproyectos.usuario.domain.model;

import java.time.LocalDateTime;

import com.team.gestorproyectos.shared.domain.model.Movimiento;

/** Un comentario que dejó la persona. Se cuenta como "Comentó en BUGS-31". */
public record ComentarioHecho(String entidad, String tipo, int numero, String titulo, LocalDateTime fecha) {

    public Hecho comoHecho() {
        return new Hecho("Comentó en " + Movimiento.nombrar(entidad, tipo, numero, titulo), "COMENTARIO", fecha);
    }
}
