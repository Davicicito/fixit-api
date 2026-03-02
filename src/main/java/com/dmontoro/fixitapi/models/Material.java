package com.dmontoro.fixitapi.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Entity
@Table(name = "materiales")
@Data
public class Material {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private Double precio;

    @OneToMany(mappedBy = "material")
    @JsonIgnore
    private List<AvisoMaterial> avisoMateriales;
}