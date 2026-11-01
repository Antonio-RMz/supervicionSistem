package com.team.gestorproyectos.proyecto.infrastructure.persistence.entity;

import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "fase")
@Getter
@Setter
@NoArgsConstructor
public class FaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long proyectoId;
    private Integer numero;
    private String nombre;
    private String objetivo;
    private String estado;
    private String prioridad;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private Long responsableId;
    private String versionRelease;
    private boolean esMantenimiento;
    private String motivoEstado;
    private LocalDate reanudarEl;
}
