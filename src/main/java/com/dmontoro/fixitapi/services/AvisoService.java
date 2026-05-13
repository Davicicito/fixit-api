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

    // Métodos CRUD básicos
    public List<Aviso> getAllAvisos() {
        return avisoRepository.findAll();
    }

    public Optional<Aviso> getAvisoById(Long id) {
        return avisoRepository.findById(id);
    }

    public Aviso saveAviso(Aviso aviso) {
        if (aviso.getEstado() == null || aviso.getEstado().trim().isEmpty()) {
            aviso.setEstado("PENDIENTE");
        }

        return avisoRepository.save(aviso);
    }
    public void deleteAviso(Long id) {
        avisoRepository.deleteById(id);
    }


    public List<Aviso> getAvisosPorTecnico(Long tecnicoId) {
        return avisoRepository.findAvisosDetalladosPorTecnico(tecnicoId);
    }

    public List<Aviso> getAvisosPorMaterial(String material) {
        return avisoRepository.findAvisosPorUsoDeMaterial(material);
    }

    public List<Aviso> getAvisosPendientesPorCategoria(String categoria) {
        return avisoRepository.findAvisosPendientesPorCategoria(categoria);
    }

    @Autowired
    private com.dmontoro.fixitapi.repositories.AvisoMaterialRepository avisoMaterialRepository;

    // Método para añadir un material a un aviso indicando la CANTIDAD
    public AvisoMaterial añadirMaterialAAviso(Long idAviso, AvisoMaterial avisoMaterial) {
        Aviso aviso = avisoRepository.findById(idAviso)
                .orElseThrow(() -> new RuntimeException("Error: El aviso indicado no existe."));

        if (avisoMaterial.getMaterial() == null || avisoMaterial.getMaterial().getId() == null) {
            throw new RuntimeException("Error: Debes especificar un material válido.");
        }

        if (avisoMaterial.getCantidad() <= 0) {
            throw new RuntimeException("Error: La cantidad del material debe ser mayor que cero.");
        }

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
        avisoExistente.setPrioridad(avisoDetails.getPrioridad());
        avisoExistente.setFotoAveria(avisoDetails.getFotoAveria());
        avisoExistente.setFirmaCliente(avisoDetails.getFirmaCliente());
        avisoExistente.setTecnico(avisoDetails.getTecnico());
        avisoExistente.setCliente(avisoDetails.getCliente());
        avisoExistente.setCategoria(avisoDetails.getCategoria());

        return avisoRepository.save(avisoExistente);
    }
    @Autowired
    private com.dmontoro.fixitapi.repositories.MaterialRepository materialRepository;

    @org.springframework.transaction.annotation.Transactional
    public void finalizarTrabajo(Long id, com.dmontoro.fixitapi.dto.FinalizarAvisoRequest peticion) {

        // 1. Buscamos el aviso
        Aviso aviso = avisoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Aviso con ID " + id + " no encontrado"));

        // 2. Actualizamos los datos básicos
        aviso.setEstado(peticion.getEstado());
        aviso.setFotoAveria(peticion.getFotoBase64());
        aviso.setFirmaCliente(peticion.getFirmaBase64());

        // GUARDAMOS LA NOTA DEL CLIENTE
        aviso.setValoracionCliente(peticion.getValoracionCliente());

        if (peticion.getObservaciones() != null && !peticion.getObservaciones().isEmpty()) {
            String descActual = aviso.getDescripcion() != null ? aviso.getDescripcion() : "";
            aviso.setDescripcion(descActual + "\n\n--- OBSERVACIONES EXTRA ---\n" + peticion.getObservaciones());
        }

        avisoRepository.save(aviso);

        // 3. PROCESAMOS LOS MATERIALES Y RESTAMOS STOCK
        if (peticion.getMaterialesUsados() != null) {
            for (com.dmontoro.fixitapi.dto.MaterialGastado gastado : peticion.getMaterialesUsados()) {

                // Buscamos el material en la base de datos
                com.dmontoro.fixitapi.models.Material materialBD = materialRepository.findById(gastado.getIdMaterial())
                        .orElseThrow(() -> new RuntimeException("Material no encontrado"));


                double nuevoStock = materialBD.getStock() - gastado.getCantidad();
                materialBD.setStock((int) nuevoStock); // Lo guardamos en la base de datos de inventario
                materialRepository.save(materialBD);

                // Creamos el registro en la tabla intermedia (AvisoMaterial) para tener el historial
                AvisoMaterial relacion = new AvisoMaterial();
                relacion.setAviso(aviso);
                relacion.setMaterial(materialBD);
                relacion.setCantidad((int) gastado.getCantidad());
                avisoMaterialRepository.save(relacion);
            }
        }
    }
}