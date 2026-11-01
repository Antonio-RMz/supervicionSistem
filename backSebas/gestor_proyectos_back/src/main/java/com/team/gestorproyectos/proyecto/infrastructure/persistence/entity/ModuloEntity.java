package com.team.gestorproyectos.proyecto.infrastructure.persistence.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "modulo")
@Getter
@Setter
@NoArgsConstructor
public class ModuloEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long proyectoId;
    private String tipo;
    private String nombre;
    private String tecnologia;
    private String repositorio;
    private String ramaBase;
    /** Responsable técnico del módulo (PDF pág. 35); null si no se ha elegido. */
    private Long responsableId;
}
