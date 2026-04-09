package com.dmontoro.fixitapi.models;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "materiales")
@Data
public class Material {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;

    private Double precio;

    // --- NUEVOS CAMPOS AÑADIDOS ---
    private Integer stock;

    @Column(name = "stock_minimo")
    private Integer stockMinimo;

    private String unidad;

    // Relación N:1 con Categoría (Muchos materiales pertenecen a una categoría)
    @ManyToOne
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;
    // ------------------------------

    // Evitamos bucles infinitos en el JSON al pedir materiales
    @JsonIgnore
    @OneToMany(mappedBy = "material")
    private List<AvisoMaterial> avisoMateriales;
}