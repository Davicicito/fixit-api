package com.dmontoro.fixitapi.services;

import com.dmontoro.fixitapi.models.Material;
import com.dmontoro.fixitapi.repositories.MaterialRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Clase de servicio encargada de la logica de negocio del inventario de la empresa.
 * Actua como el gestor del almacen, permitiendo controlar las existencias, añadir nuevas piezas
 * y asegurar que los datos de los productos esten siempre disponibles para los operarios.
 */
@Service
public class MaterialService {

    @Autowired
    private MaterialRepository materialRepository;

    /**
     * Recupera el listado completo de todos los materiales, herramientas y repuestos guardados en el sistema.
     * @return Una lista con todos los productos del inventario.
     */
    public List<Material> getAllMateriales() {
        return materialRepository.findAll();
    }

    /**
     * Busca la ficha detallada de un producto especifico utilizando su numero de identificacion.
     * @param id El numero identificador del material.
     * @return Un objeto opcional con la informacion del producto si existe en el registro.
     */
    public Optional<Material> getMaterialById(Long id) {
        return materialRepository.findById(id);
    }

    /**
     * Registra un nuevo material en el almacen o actualiza los datos de uno que ya estaba guardado.
     * @param material El objeto con el nombre, precio y stock del producto.
     * @return El material guardado con su identificador oficial.
     */
    public Material saveMaterial(Material material) {
        return materialRepository.save(material);
    }

    /**
     * Elimina definitivamente un producto del registro del inventario.
     * @param id El numero identificador del material que se desea borrar.
     */
    public void deleteMaterial(Long id) {
        materialRepository.deleteById(id);
    }
}