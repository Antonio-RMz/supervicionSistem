package com.team.gestorproyectos.proyecto.infrastructure.persistence.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "demo")
@Getter
@Setter
@NoArgsConstructor
public class DemoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long faseId;
    private String titulo;
    private String url;
    /** PENDIENTE, APROBADA, CON_CAMBIOS */
    private String estado;
    private String comentarioCliente;
    private Long revisadaPor;
    private LocalDateTime revisadaEn;
}
