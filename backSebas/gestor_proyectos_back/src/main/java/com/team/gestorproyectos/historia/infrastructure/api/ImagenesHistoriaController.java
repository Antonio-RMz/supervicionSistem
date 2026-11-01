package com.team.gestorproyectos.historia.infrastructure.api;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.team.gestorproyectos.historia.domain.model.NuevaImagen;
import com.team.gestorproyectos.historia.domain.port.in.ImagenesHistoriaUseCase;
import com.team.gestorproyectos.historia.domain.port.in.ImagenesHistoriaUseCase.Imagen;
import com.team.gestorproyectos.shared.infrastructure.security.UsuarioSesion;

import lombok.RequiredArgsConstructor;

/** Las imágenes de la historia: capturas, bocetos o el diseño, junto con el enlace de Figma. */
@RestController
@RequestMapping("/api/equipo/proyectos/{proyectoId}/historias/{historiaId}/imagenes")
@RequiredArgsConstructor
public class ImagenesHistoriaController {

    private final ImagenesHistoriaUseCase imagenes;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public void subir(@PathVariable("proyectoId") Long proyectoId,
                      @PathVariable("historiaId") Long historiaId,
                      @RequestParam("archivo") MultipartFile archivo,
                      @AuthenticationPrincipal UsuarioSesion sesion) {
        imagenes.subir(proyectoId, historiaId, sesion.id(), sesion.esAdmin(),
                NuevaImagen.de(archivo.getOriginalFilename(), contenido(archivo)));
    }

    /** El archivo tal cual, con su tipo; el navegador no debe adivinar otro (nosniff). */
    @GetMapping("/{imagenId}")
    public ResponseEntity<byte[]> ver(@PathVariable("proyectoId") Long proyectoId,
                                      @PathVariable("historiaId") Long historiaId,
                                      @PathVariable("imagenId") Long imagenId,
                                      @AuthenticationPrincipal UsuarioSesion sesion) {
        Imagen imagen = imagenes.ver(proyectoId, historiaId, imagenId, sesion.id(), sesion.esAdmin());
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(imagen.tipoMime()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.inline().filename(imagen.nombre(), StandardCharsets.UTF_8)
                                .build().toString())
                .header("X-Content-Type-Options", "nosniff")
                .cacheControl(CacheControl.maxAge(Duration.ofHours(1)).cachePrivate())
                .body(imagen.contenido());
    }

    @DeleteMapping("/{imagenId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void quitar(@PathVariable("proyectoId") Long proyectoId,
                       @PathVariable("historiaId") Long historiaId,
                       @PathVariable("imagenId") Long imagenId,
                       @AuthenticationPrincipal UsuarioSesion sesion) {
        imagenes.quitar(proyectoId, historiaId, imagenId, sesion.id(), sesion.esAdmin());
    }

    private static byte[] contenido(MultipartFile archivo) {
        try {
            return archivo.getBytes();
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo leer la imagen", e);
        }
    }
}
