package com.dmontoro.fixitapi.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;

@Entity
@Table(name = "tecnicos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Tecnico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    private String especialidad;

    @Column(nullable = false)
    private String rol; // ADMIN o TECNICO

    // --- NUEVOS CAMPOS AÑADIDOS ---
    private String telefono;

    private Boolean activo = true; // Por defecto un técnico está activo

    private Double calificacion = 0.0;
    // ------------------------------

    @OneToMany(mappedBy = "tecnico", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JsonIgnore
    private List<Aviso> avisos;
}