package com.dmontoro.fixitapi.services;

import com.dmontoro.fixitapi.models.Aviso;
import com.dmontoro.fixitapi.models.AvisoMaterial;
import com.dmontoro.fixitapi.repositories.AvisoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AvisoService {

    @Autowired
    private AvisoRepository avisoRepository;

    // Métodos CRUD básicos (usando los que Spring crea solos)
    public List<Aviso> getAllAvisos() {
        return avisoRepository.findAll();
    }

    public Optional<Aviso> getAvisoById(Long id) {
        return avisoRepository.findById(id);
    }

    public Aviso saveAviso(Aviso aviso) {
        // LÓGICA DE NEGOCIO: Si no tiene estado, por defecto es PENDIENTE
        if (aviso.getEstado() == null || aviso.getEstado().trim().isEmpty()) {
            aviso.setEstado("PENDIENTE");
        }

        // Aquí en un futuro se añadirían las validaciones de si el técnico o cliente existen
        return avisoRepository.save(aviso);
    }
    public void deleteAviso(Long id) {
        avisoRepository.deleteById(id);
    }

    // --- MÉTODOS "PRO" USANDO TUS CONSULTAS JOIN ---

    public List<Aviso> getAvisosPorTecnico(Long tecnicoId) {
        return avisoRepository.findAvisosDetalladosPorTecnico(tecnicoId);
    }

    public List<Aviso> getAvisosPorMaterial(String material) {
        return avisoRepository.findAvisosPorUsoDeMaterial(material);
    }

    public List<Aviso> getAvisosPendientesPorCategoria(String categoria) {
        return avisoRepository.findAvisosPendientesPorCategoria(categoria);
    }

    // inyectamos el repositorio de la tabla intermedia aquí
    @Autowired
    private com.dmontoro.fixitapi.repositories.AvisoMaterialRepository avisoMaterialRepository;

    // Método para añadir un material a un aviso indicando la CANTIDAD
    public AvisoMaterial añadirMaterialAAviso(Long idAviso, AvisoMaterial avisoMaterial) {
        // VALIDACIÓN 1: ¿Existe el aviso?
        Aviso aviso = avisoRepository.findById(idAviso)
                .orElseThrow(() -> new RuntimeException("Error: El aviso indicado no existe."));

        // VALIDACIÓN 2: ¿Existe el material que intentan añadir?
        if (avisoMaterial.getMaterial() == null || avisoMaterial.getMaterial().getId() == null) {
            throw new RuntimeException("Error: Debes especificar un material válido.");
        }

        // Comprobamos en la base de datos si el material base existe realmente
        /* Nota: Asumiendo que inyectaste MaterialRepository en este Service */
        // materialRepository.findById(avisoMaterial.getMaterial().getId())
        //        .orElseThrow(() -> new RuntimeException("Error: El material no existe en el catálogo."));

        // VALIDACIÓN 3: ¿La cantidad es lógica?
        if (avisoMaterial.getCantidad() <= 0) {
            throw new RuntimeException("Error: La cantidad del material debe ser mayor que cero.");
        }

        // Si todo es correcto, asignamos y guardamos
        avisoMaterial.setAviso(aviso);
        return avisoMaterialRepository.save(avisoMaterial);
    }

    // Método para ver todos los materiales que se han gastado en un aviso concreto
    public List<com.dmontoro.fixitapi.models.AvisoMaterial> getMaterialesDeUnAviso(Long avisoId) {
        return avisoMaterialRepository.findByAvisoId(avisoId);
    }

    public Aviso actualizarAviso(Long id, Aviso avisoDetails) {
        Aviso avisoExistente = avisoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Aviso con ID " + id + " no encontrado"));

        avisoExistente.setDescripcion(avisoDetails.getDescripcion());
        avisoExistente.setEstado(avisoDetails.getEstado());
        avisoExistente.setPrioridad(avisoDetails.getPrioridad()); // AÑADIDO AQUI
        avisoExistente.setFotoAveria(avisoDetails.getFotoAveria());
        avisoExistente.setFirmaCliente(avisoDetails.getFirmaCliente());
        avisoExistente.setTecnico(avisoDetails.getTecnico());
        avisoExistente.setCliente(avisoDetails.getCliente());
        avisoExistente.setCategoria(avisoDetails.getCategoria());

        return avisoRepository.save(avisoExistente);
    }
}