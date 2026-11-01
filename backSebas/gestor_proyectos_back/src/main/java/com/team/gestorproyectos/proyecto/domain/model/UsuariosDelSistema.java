package com.team.gestorproyectos.proyecto.domain.model;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import com.team.gestorproyectos.shared.domain.exception.ReglaNegocioException;

/**
 * Pestaña Usuarios del sistema del proyecto.
 *
 * @param puedeEditar el Líder, un Admin o quien crea historias (Analistas)
 */
public record UsuariosDelSistema(List<UsuarioDelSistema> usuarios, boolean puedeEditar) {

    public static final int MAXIMO = 30;

    public UsuariosDelSistema {
        usuarios = List.copyOf(usuarios);
    }

    /** La lista completa que se guarda: sin nombres repetidos (sin importar mayúsculas) y no más de {@link #MAXIMO}. */
    public static List<UsuarioDelSistema> validar(List<UsuarioDelSistema> usuarios) {
        List<UsuarioDelSistema> lista = usuarios == null ? List.of() : List.copyOf(usuarios);
        if (lista.size() > MAXIMO) {
            throw new ReglaNegocioException("Puedes tener hasta " + MAXIMO + " usuarios del sistema");
        }
        Set<String> vistos = new HashSet<>();
        lista.stream().filter(u -> !vistos.add(u.nombre().toLowerCase(Locale.ROOT))).findFirst().ifPresent(u -> {
            throw new ReglaNegocioException("\"" + u.nombre() + "\" está repetido");
        });
        return lista;
    }
}
