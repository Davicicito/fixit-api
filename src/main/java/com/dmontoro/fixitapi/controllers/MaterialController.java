package com.dmontoro.fixitapi.controllers;

import com.dmontoro.fixitapi.models.Material;
import com.dmontoro.fixitapi.services.MaterialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * Controlador de la API para la gestion de los materiales e inventario.
 * Proporciona los puntos de entrada para que tanto el programa del jefe como la aplicacion movil
 * puedan consultar que piezas hay en el almacen o dar de alta nuevos productos.
 */
@RestController
@RequestMapping("/materiales")
@CrossOrigin
public class MaterialController {

    @Autowired
    private MaterialService materialService;

    /**
     * Solicita a la base de datos la lista de todos los productos y piezas guardados en el inventario.
     * Es la funcion que utiliza la aplicacion movil para que el tecnico pueda elegir que materiales ha gastado.
     *
     * @return Una respuesta con la lista completa de materiales y sus existencias.
     */
    @GetMapping
    public ResponseEntity<List<Material>> getAllMateriales() {
        return new ResponseEntity<>(materialService.getAllMateriales(), HttpStatus.OK);
    }

    /**
     * Busca un material concreto utilizando su numero de identificador unico.
     * Permite ver los detalles especificos de una pieza como su precio o su stock minimo.
     *
     * @param id El numero de identificacion del material.
     * @return Los datos del material si existe en el sistema.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Material> getMaterialById(@PathVariable Long id) {
        Optional<Material> material = materialService.getMaterialById(id);
        return material.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    /**
     * Recoge la informacion de un nuevo articulo y lo registra en el inventario de la empresa.
     *
     * @param material El objeto con el nombre y las propiedades del producto a crear.
     * @return El material guardado con su nuevo identificador asignado.
     */
    @PostMapping
    public ResponseEntity<Material> createMaterial(@RequestBody Material material) {
        return ResponseEntity.status(HttpStatus.CREATED).body(materialService.saveMaterial(material));
    }

    /**
     * Localiza un material por su numero identificador y lo borra definitivamente del registro.
     * Si no se encuentra el material el sistema avisa de que el borrado no ha sido posible.
     *
     * @param id El numero del material que se desea eliminar.
     * @return Una respuesta vacia confirmando que el articulo se ha borrado con exito.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMaterial(@PathVariable Long id) {
        if (materialService.getMaterialById(id).isPresent()) {
            materialService.deleteMaterial(id);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
}