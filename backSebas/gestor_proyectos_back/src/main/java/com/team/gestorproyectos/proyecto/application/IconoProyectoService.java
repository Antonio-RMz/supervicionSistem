package com.team.gestorproyectos.proyecto.application;

import java.util.Optional;

import com.team.gestorproyectos.historia.domain.model.NuevaImagen;
import com.team.gestorproyectos.proyecto.domain.port.in.IconoProyectoUseCase;
import com.team.gestorproyectos.proyecto.domain.port.out.IconoProyectoPort;
import com.team.gestorproyectos.proyecto.domain.port.out.IconoProyectoPort.ProyectoConIcono;
import com.team.gestorproyectos.shared.application.ServicioAplicacion;
import com.team.gestorproyectos.shared.application.Transaccional;
import com.team.gestorproyectos.shared.domain.exception.AccesoDenegadoException;
import com.team.gestorproyectos.shared.domain.exception.NoEncontradoException;
import com.team.gestorproyectos.shared.domain.exception.ReglaNegocioException;
import com.team.gestorproyectos.shared.domain.port.ArchivosPort;
import com.team.gestorproyectos.shared.domain.port.PermisosPort;

import lombok.RequiredArgsConstructor;

@ServicioAplicacion
@RequiredArgsConstructor
public class IconoProyectoService implements IconoProyectoUseCase {

    private final IconoProyectoPort proyectos;
    private final ArchivosPort archivos;
    private final PermisosPort permisos;

    @Override
    @Transaccional
    public void subir(Long proyectoId, Long usuarioId, boolean esAdmin, NuevaImagen imagen) {
        ProyectoConIcono proyecto = verificarEdicion(proyectoId, usuarioId, esAdmin);
        if (imagen.tamanoBytes() > TAMANO_MAXIMO) {
            throw new ReglaNegocioException("El ícono pesa más de 1 MB");
        }
        String ruta = archivos.guardar(imagen.contenido(), imagen.extension());
        try {
            proyectos.guardar(proyectoId, ruta, imagen.tipoMime());
        } catch (RuntimeException e) {
            archivos.borrar(ruta);
            throw e;
        }
        // El anterior ya no se usa.
        if (proyecto.ruta() != null) {
            archivos.borrar(proyecto.ruta());
        }
    }

    @Override
    @Transaccional(soloLectura = true)
    public Optional<Icono> ver(Long proyectoId, Long usuarioId, boolean esAdmin) {
        ProyectoConIcono proyecto = buscar(proyectoId, usuarioId, esAdmin);
        if (proyecto.ruta() == null) {
            return Optional.empty();
        }
        return Optional.of(new Icono(proyecto.tipoMime(), archivos.leer(proyecto.ruta())));
    }

    @Override
    @Transaccional
    public void quitar(Long proyectoId, Long usuarioId, boolean esAdmin) {
        ProyectoConIcono proyecto = verificarEdicion(proyectoId, usuarioId, esAdmin);
        if (proyecto.ruta() == null) {
            return;
        }
        proyectos.guardar(proyectoId, null, null);
        archivos.borrar(proyecto.ruta());
    }

    /** Igual que Editar proyecto: su Líder o un Admin, con el permiso de editar proyectos. */
    private ProyectoConIcono verificarEdicion(Long proyectoId, Long usuarioId, boolean esAdmin) {
        ProyectoConIcono proyecto = buscar(proyectoId, usuarioId, esAdmin);
        boolean puede = (esAdmin || usuarioId.equals(proyecto.liderId()))
                && permisos.puede(usuarioId, PermisosPort.EDITAR_PROYECTOS);
        if (!puede) {
            throw new AccesoDenegadoException("El ícono lo cambia el Líder del proyecto");
        }
        return proyecto;
    }

    private ProyectoConIcono buscar(Long proyectoId, Long usuarioId, boolean esAdmin) {
        return proyectos.visible(proyectoId, usuarioId, esAdmin)
                .orElseThrow(() -> new NoEncontradoException("No encontramos ese proyecto"));
    }
}
