package com.team.gestorproyectos.fase.domain.model;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.team.gestorproyectos.shared.domain.exception.ReglaNegocioException;

/**
 * Cómo está guardada la fase hoy, para llenar el formulario de Editar fase y validar los cambios.
 *
 * @param modulosEnUso los módulos que ya usan sus historias o tickets: esos no se pueden quitar
 */
public record FaseGuardada(
        Long id,
        int numero,
        String estado,
        String nombre,
        String objetivo,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        Long responsableId,
        String versionRelease,
        String prioridad,
        Set<Long> moduloIds,
        List<String> criteriosManuales,
        Set<Long> modulosEnUso) {

    public FaseGuardada {
        moduloIds = Set.copyOf(moduloIds);
        criteriosManuales = List.copyOf(criteriosManuales);
        modulosEnUso = Set.copyOf(modulosEnUso);
    }

    /**
     * Lo que no se puede cambiar con la fase ya trabajándose.
     *
     * @param otras         las demás fases del proyecto, para no repetir versión
     * @param nombresModulo id → "portal-api", para el mensaje
     */
    public void validarCambios(NuevaFase cambios, List<FaseExistente> otras, Map<Long, String> nombresModulo) {
        modulosEnUso.stream().filter(id -> moduloIds.contains(id) && !cambios.moduloIds().contains(id)).findFirst()
                .ifPresent(id -> {
                    throw new ReglaNegocioException("El repositorio " + nombresModulo.getOrDefault(id, "elegido")
                            + " ya tiene historias o tickets en esta fase: no se puede quitar");
                });
        if (cambios.versionRelease() != null && otras.stream()
                .anyMatch(f -> f.numero() != numero && cambios.versionRelease().equals(f.versionRelease()))) {
            throw new ReglaNegocioException("La versión " + cambios.versionRelease() + " ya es de otra fase");
        }
    }
}
