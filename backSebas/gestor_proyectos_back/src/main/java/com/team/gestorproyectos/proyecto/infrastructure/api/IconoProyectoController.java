package com.team.gestorproyectos.proyecto.infrastructure.api;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Duration;

import org.springframework.http.CacheControl;
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
import com.team.gestorproyectos.proyecto.domain.port.in.IconoProyectoUseCase;
import com.team.gestorproyectos.shared.infrastructure.security.UsuarioSesion;

import lombok.RequiredArgsConstructor;

/** El ícono del proyecto. Sin ícono responde 204 y la app muestra las iniciales. */
@RestController
@RequestMapping("/api/equipo/proyectos/{proyectoId}/icono")
@RequiredArgsConstructor
public class IconoProyectoController {

    private final IconoProyectoUseCase iconos;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void subir(@PathVariable("proyectoId") Long proyectoId,
                      @RequestParam("archivo") MultipartFile archivo,
                      @AuthenticationPrincipal UsuarioSesion sesion) {
        iconos.subir(proyectoId, sesion.id(), sesion.esAdmin(),
                NuevaImagen.de(archivo.getOriginalFilename(), contenido(archivo)));
    }

    @GetMapping
    public ResponseEntity<byte[]> ver(@PathVariable("proyectoId") Long proyectoId,
                                      @AuthenticationPrincipal UsuarioSesion sesion) {
        return iconos.ver(proyectoId, sesion.id(), sesion.esAdmin())
                .map(i -> ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(i.tipoMime()))
                        .header("X-Content-Type-Options", "nosniff")
                        .cacheControl(CacheControl.maxAge(Duration.ZERO).cachePrivate())
                        .body(i.contenido()))
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void quitar(@PathVariable("proyectoId") Long proyectoId,
                       @AuthenticationPrincipal UsuarioSesion sesion) {
        iconos.quitar(proyectoId, sesion.id(), sesion.esAdmin());
    }

    private static byte[] contenido(MultipartFile archivo) {
        try {
            return archivo.getBytes();
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo leer la imagen", e);
        }
    }
}
