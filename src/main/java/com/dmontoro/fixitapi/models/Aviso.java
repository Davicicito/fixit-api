package com.dmontoro.fixitapi.models;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;
import java.time.LocalDate;

@Entity
@Table(name = "avisos")
@Data
public class Aviso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String descripcion;
    private String estado;

    // NUEVO CAMPO AÑADIDO A LA BASE DE DATOS
    @Column(columnDefinition = "VARCHAR(20) DEFAULT 'MEDIA'")
    private String prioridad = "MEDIA";

    @Column(name = "foto_averia")
    private String fotoAveria;

    @Column(name = "firma_cliente")
    private String firmaCliente;

    @Column(name = "fecha_creacion")
    private LocalDate fechaCreacion = LocalDate.now();

    @ManyToOne
    @JoinColumn(name = "tecnico_id")
    private Tecnico tecnico;

    @ManyToOne
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @ManyToOne
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    @OneToMany(mappedBy = "aviso")
    private List<AvisoMaterial> avisoMateriales;
}