package com.dmontoro.fixitapi.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Entity
@Table(name = "clientes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    private String direccion;

    private String telefono;

    // --- NUEVOS CAMPOS PARA EL DISEÑO ---
    private String email;

    private String tipo = "EMPRESA"; // Puede ser EMPRESA o PARTICULAR

    @Column(length = 500) // Le damos más espacio por si escribes notas largas
    private String notas;
    // ------------------------------------

    // Relación 1:N: Un cliente puede solicitar muchos avisos
    // AÑADIDO: fetch = FetchType.EAGER para que se cuenten solos en la tarjeta
    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JsonIgnore
    private List<Aviso> avisos;
}