package com.team.gestorproyectos.cliente.infrastructure.persistence.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "cliente")
@Getter
@Setter
@NoArgsConstructor
public class ClienteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String razonSocial;
    private String nombreCorto;
    private String rfc;
    private String giro;
    private String ciudad;
    private String contactoNombre;
    private String contactoCorreo;
    private String contactoTelefono;
    private String notas;
    private boolean activo;
}
