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

    @OneToMany(mappedBy = "tecnico", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Aviso> avisos;
}