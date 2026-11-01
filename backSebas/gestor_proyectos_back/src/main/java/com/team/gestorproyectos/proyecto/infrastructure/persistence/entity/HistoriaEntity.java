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
@Table(name = "historia")
@Getter
@Setter
@NoArgsConstructor
public class HistoriaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long proyectoId;
    private Long faseId;
    private Integer numero;
    private String titulo;
    private String como;
    private String quiero;
    private String para;
    private String estado;
    private String prioridad;
    private Integer puntos;
    private Long analistaId;
    private String figmaUrl;
    private boolean disenoAprobado;
    private Long disenoAprobadoPor;
    private LocalDateTime disenoAprobadoEn;
    private Long creadoPor;
}
