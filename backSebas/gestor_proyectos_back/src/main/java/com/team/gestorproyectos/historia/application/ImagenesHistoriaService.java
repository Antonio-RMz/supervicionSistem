package com.team.gestorproyectos.historia.application;

import java.util.Set;

import com.team.gestorproyectos.historia.domain.model.HistoriaParaGestionar;
import com.team.gestorproyectos.historia.domain.model.ImagenGuardada;
import com.team.gestorproyectos.historia.domain.model.NuevaImagen;
import com.team.gestorproyectos.historia.domain.model.QuienGestiona;
import com.team.gestorproyectos.historia.domain.port.in.ImagenesHistoriaUseCase;
import com.team.gestorproyectos.historia.domain.port.out.HistoriaGestionPort;
import com.team.gestorproyectos.historia.domain.port.out.HistoriaRepositoryPort;
import com.team.gestorproyectos.shared.application.ServicioAplicacion;
import com.team.gestorproyectos.shared.application.Transaccional;
import com.team.gestorproyectos.shared.domain.exception.AccesoDenegadoException;
import com.team.gestorproyectos.shared.domain.exception.NoEncontradoException;
import com.team.gestorproyectos.shared.domain.exception.ReglaNegocioException;
import com.team.gestorproyectos.shared.domain.port.ArchivosPort;

import lombok.RequiredArgsConstructor;

@ServicioAplicacion
@RequiredArgsConstructor
public class ImagenesHistoriaService implements ImagenesHistoriaUseCase {

    private final HistoriaRepositoryPort proyectos;
    private final HistoriaGestionPort historias;
    private final ArchivosPort archivos;

    @Override
    @Transaccional
    public void subir(Long proyectoId, Long historiaId, Long usuarioId, boolean esAdmin, NuevaImagen imagen) {
        verificarGestion(proyectoId, historiaId, usuarioId, esAdmin);
        if (historias.imagenes(historiaId).size() >= NuevaImagen.MAXIMO_POR_HISTORIA) {
            throw new ReglaNegocioException("La historia ya tiene " + NuevaImagen.MAXIMO_POR_HISTORIA
                    + " imágenes: quita una para subir otra");
        }
        String ruta = archivos.guardar(imagen.contenido(), imagen.extension());
        try {
            historias.agregarImagen(historiaId, imagen, ruta, usuarioId);
        } catch (RuntimeException e) {
            archivos.borrar(ruta);
            throw e;
        }
    }

    @Override
    @Transaccional(soloLectura = true)
    public Imagen ver(Long proyectoId, Long historiaId, Long imagenId, Long usuarioId, boolean esAdmin) {
        buscar(proyectoId, historiaId, usuarioId, esAdmin);
        ImagenGuardada imagen = imagen(historiaId, imagenId);
        return new Imagen(imagen.nombre(), imagen.tipoMime(), archivos.leer(imagen.ruta()));
    }

    @Override
    @Transaccional
    public void quitar(Long proyectoId, Long historiaId, Long imagenId, Long usuarioId, boolean esAdmin) {
        verificarGestion(proyectoId, historiaId, usuarioId, esAdmin);
        ImagenGuardada imagen = imagen(historiaId, imagenId);
        historias.quitarImagen(imagen.id());
        archivos.borrar(imagen.ruta());
    }

    private ImagenGuardada imagen(Long historiaId, Long imagenId) {
        return historias.imagenes(historiaId).stream().filter(i -> i.id().equals(imagenId)).findFirst()
                .orElseThrow(() -> new NoEncontradoException("No encontramos esa imagen"));
    }

    private void verificarGestion(Long proyectoId, Long historiaId, Long usuarioId, boolean esAdmin) {
        HistoriaParaGestionar historia = buscar(proyectoId, historiaId, usuarioId, esAdmin);
        if (!historia.puedeAdjuntar(new QuienGestiona(usuarioId, esAdmin, Set.of()))) {
            throw new AccesoDenegadoException(historia.motivoSinImagenes());
        }
    }

    private HistoriaParaGestionar buscar(Long proyectoId, Long historiaId, Long usuarioId, boolean esAdmin) {
        proyectos.proyectoVisible(proyectoId, usuarioId, esAdmin)
                .orElseThrow(() -> new NoEncontradoException("No encontramos ese proyecto"));
        return historias.buscarParaGestionar(proyectoId, historiaId)
                .orElseThrow(() -> new NoEncontradoException("No encontramos esa historia"));
    }
}
