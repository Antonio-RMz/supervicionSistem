package com.team.gestorproyectos.fase.domain.model;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/** Una fase que ya tiene el proyecto: sirve para numerar, sugerir fechas y versión, y "una fase a la vez". */
public record FaseExistente(int numero, String nombre, String estado, LocalDate fechaFin, String versionRelease,
                            boolean mantenimiento) {

    /** Estados en que una fase ocupa el turno: mientras exista una así, otra no puede iniciar. */
    private static final Set<String> ACTIVOS = Set.of("EN_CURSO", "PAUSADA", "LISTA_PARA_RELEASE", "EN_PUBLICACION");

    public boolean activa() {
        return !mantenimiento && ACTIVOS.contains(estado);
    }

    public static int siguienteNumero(List<FaseExistente> fases) {
        return fases.stream().filter(f -> !f.mantenimiento()).mapToInt(FaseExistente::numero).max().orElse(0) + 1;
    }

    /** El día después de que termina la última fase; si no hay, hoy. */
    public static LocalDate inicioSugerido(List<FaseExistente> fases, LocalDate hoy) {
        return fases.stream().filter(f -> !f.mantenimiento() && f.fechaFin() != null)
                .map(FaseExistente::fechaFin).max(LocalDate::compareTo)
                .map(fin -> fin.plusDays(1))
                .orElse(hoy);
    }
}
