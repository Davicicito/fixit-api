package com.dmontoro.fixitapi.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "aviso_materiales")
@Data
public class AvisoMaterial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "aviso_id")
    @JsonIgnore
    private Aviso aviso;

    @ManyToOne
    @JoinColumn(name = "material_id")
    private Material material;

    private Integer cantidad; // Atributo específico de la relación
}