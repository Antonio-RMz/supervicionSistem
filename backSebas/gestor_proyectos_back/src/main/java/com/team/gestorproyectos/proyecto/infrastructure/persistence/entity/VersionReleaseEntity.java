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
@Table(name = "version_release")
@Getter
@Setter
@NoArgsConstructor
public class VersionReleaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long proyectoId;
    private Long faseId;
    private String version;
    private String tipo;
    private String estado;
    private String resumen;
    private String notas;
    private Long publicadoPor;
    private LocalDateTime publicadoEn;
}
