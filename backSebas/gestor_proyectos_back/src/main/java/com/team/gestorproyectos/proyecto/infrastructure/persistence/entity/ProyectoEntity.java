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
@Table(name = "proyecto")
@Getter
@Setter
@NoArgsConstructor
public class ProyectoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String codigo;
    private String nombre;
    private String descripcion;
    /** null = proyecto interno */
    private Long clienteId;

    private String prioridad;
    private String estado;
    private LocalDate fechaInicio;
    private LocalDate fechaEntrega;
    private Long liderId;
    private boolean permitirAutovalidacion;
    private boolean comentarioAlRechazar;
    private boolean ramaAlIniciar;
    private boolean notificarClienteFase;
}
