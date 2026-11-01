package com.team.gestorproyectos.usuario.infrastructure.persistence.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Permiso extra de una persona. proyectoId null = todos los proyectos; otorgado false = quitado. */
@Entity
@Table(name = "usuario_permiso")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioPermisoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long usuarioId;
    private Long permisoId;
    private Long proyectoId;
    private boolean otorgado;
}
