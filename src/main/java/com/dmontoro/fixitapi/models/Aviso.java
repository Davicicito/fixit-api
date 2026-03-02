package com.dmontoro.fixitapi.models;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Entity
@Table(name = "avisos")
@Data
public class Aviso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String descripcion;
    private String estado;

    @Column(name = "foto_averia")
    private String fotoAveria;

    @Column(name = "firma_cliente")
    private String firmaCliente;

    @ManyToOne
    @JoinColumn(name = "tecnico_id")
    private Tecnico tecnico;

    @ManyToOne
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @ManyToOne
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    // Conexión con la tabla intermedia
    @OneToMany(mappedBy = "aviso")
    private List<AvisoMaterial> avisoMateriales;
}