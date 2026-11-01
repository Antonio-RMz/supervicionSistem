package com.team.gestorproyectos.proyecto.infrastructure.persistence.entity;

import java.math.BigDecimal;
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
@Table(name = "ticket")
@Getter
@Setter
@NoArgsConstructor
public class TicketEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long proyectoId;
    private Long faseId;
    private Long historiaId;
    /** ACTV, BUGS, MEJR */
    private String tipo;
    private Integer numero;
    private String titulo;
    private String descripcion;
    private String estado;
    private String prioridad;
    private String severidad;
    private Long moduloId;
    private Long responsableId;
    private Long testerId;
    private Long creadoPor;
    private BigDecimal estimacionHoras;
    private BigDecimal horasRegistradas;
    private LocalDate fechaLimite;
    private String rama;
    private String capas;
    /** "Viene de" de una falla en producción (PDF pág. 39): la historia donde nació; no mueve su avance. */
    private Long historiaOrigenId;
    /** La versión en producción cuando se reportó la falla. */
    private String versionReportada;
}
