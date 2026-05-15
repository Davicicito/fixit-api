package com.dmontoro.fixitapi.services;

import com.dmontoro.fixitapi.models.Aviso;
import com.dmontoro.fixitapi.models.AvisoMaterial;
import com.dmontoro.fixitapi.repositories.AvisoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Clase de servicio que contiene el cerebro del sistema de avisos.
 * Se encarga de procesar las reglas de negocio mas importantes como el cierre de reparaciones,
 * el calculo del inventario restante y la gestion de los partes de trabajo.
 */
@Service
public class AvisoService {

    @Autowired
    private AvisoRepository avisoRepository;

    // Métodos CRUD básicos
    /**
     * Obtiene todos los partes de trabajo almacenados en el sistema sin ningun filtro.
     * @return La lista completa de avisos.
     */
    public List<Aviso> getAllAvisos() {
        return avisoRepository.findAll();
    }

    /**
     * Recupera un aviso concreto buscando por su numero de referencia unico.
     * @param id El numero identificador del aviso.
     * @return Un objeto opcional con los datos del aviso solicitado.
     */
    public Optional<Aviso> getAvisoById(Long id) {
        return avisoRepository.findById(id);
    }

    /**
     * Guarda un aviso en la base de datos asegurando que siempre tenga un estado asignado.
     * Si no se indica nada el sistema le asigna automaticamente el estado pendiente.
     * @param aviso El objeto con la informacion de la averia.
     * @return El aviso guardado con su identificador oficial.
     */
    public Aviso saveAviso(Aviso aviso) {
        if (aviso.getEstado() == null || aviso.getEstado().trim().isEmpty()) {
            aviso.setEstado("PENDIENTE");
        }

        return avisoRepository.save(aviso);
    }

    /**
     * Elimina un parte de trabajo del sistema de forma definitiva.
     * @param id El numero del aviso que se desea borrar.
     */
    public void deleteAviso(Long id) {
        avisoRepository.deleteById(id);
    }

    /**
     * Busca todos los trabajos que tiene asignados un operario especifico.
     * @param tecnicoId El numero de identificacion del tecnico.
     * @return El listado de sus tareas pendientes y realizadas.
     */
    public List<Aviso> getAvisosPorTecnico(Long tecnicoId) {
        return avisoRepository.findAvisosDetalladosPorTecnico(tecnicoId);
    }

    /**
     * Filtra los avisos buscando por el nombre de un material utilizado.
     * @param material El nombre de la pieza o repuesto.
     * @return Los avisos donde se ha usado ese componente.
     */
    public List<Aviso> getAvisosPorMaterial(String material) {
        return avisoRepository.findAvisosPorUsoDeMaterial(material);
    }

    /**
     * Obtiene los avisos que todavia no se han iniciado dentro de una rama profesional concreta.
     * @param categoria El nombre de la especialidad tecnica.
     * @return La lista de avisos sin empezar.
     */
    public List<Aviso> getAvisosPendientesPorCategoria(String categoria) {
        return avisoRepository.findAvisosPendientesPorCategoria(categoria);
    }

    @Autowired
    private com.dmontoro.fixitapi.repositories.AvisoMaterialRepository avisoMaterialRepository;

    // Método para añadir un material a un aviso indicando la CANTIDAD
    /**
     * Vincula un material a un aviso concreto especificando cuantas unidades se han consumido.
     * Verifica que tanto el trabajo como el material existan antes de realizar el registro.
     * @param idAviso El numero del aviso al que se añade el material.
     * @param avisoMaterial El objeto con los datos del consumo.
     * @return El registro de la union entre el material y el aviso.
     */
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
    /**
     * Consulta el historial de materiales consumidos en una reparacion especifica.
     * @param avisoId El numero identificador del parte de trabajo.
     * @return La lista de todas las piezas utilizadas en esa tarea.
     */
    public List<com.dmontoro.fixitapi.models.AvisoMaterial> getMaterialesDeUnAviso(Long avisoId) {
        return avisoMaterialRepository.findByAvisoId(avisoId);
    }

    /**
     * Modifica los datos principales de un aviso existente actualizando toda su informacion.
     * @param id El numero del aviso a modificar.
     * @param avisoDetails Los nuevos datos que se van a guardar.
     * @return El aviso con la informacion actualizada.
     */
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

    /**
     * Realiza todo el proceso de cierre de un trabajo de forma segura y coordinada.
     * Se encarga de guardar las fotos finales y las firmas pero tambien de restar automaticamente
     * las piezas usadas del inventario general para que el stock este siempre al dia.
     * @param id El identificador del aviso que se termina.
     * @param peticion El paquete de datos enviado desde el movil con toda la informacion final.
     */
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