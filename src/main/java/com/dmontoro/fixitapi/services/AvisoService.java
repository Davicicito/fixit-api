package com.dmontoro.fixitapi.services;

import com.dmontoro.fixitapi.models.Aviso;
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
        // Regla de negocio profesional: Si el aviso es nuevo (no tiene ID), entra como PENDIENTE
        if (aviso.getId() == null && aviso.getEstado() == null) {
            aviso.setEstado("PENDIENTE");
        }
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
    public com.dmontoro.fixitapi.models.AvisoMaterial añadirMaterialAAviso(com.dmontoro.fixitapi.models.AvisoMaterial avisoMaterial) {
        return avisoMaterialRepository.save(avisoMaterial);
    }

    // Método para ver todos los materiales que se han gastado en un aviso concreto
    public List<com.dmontoro.fixitapi.models.AvisoMaterial> getMaterialesDeUnAviso(Long avisoId) {
        return avisoMaterialRepository.findByAvisoId(avisoId);
    }
}