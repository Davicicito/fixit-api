package com.dmontoro.fixitapi.controllers;

import com.dmontoro.fixitapi.models.Categoria;
import com.dmontoro.fixitapi.services.CategoriaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador de la API encargado de gestionar las categorias.
 * Sirve como punto de conexion para leer o añadir nuevas especialidades de trabajo en el sistema.
 */
@RestController
@RequestMapping("/categorias")
@CrossOrigin
public class CategoriaController {

    @Autowired
    private CategoriaService categoriaService;

    /**
     * Pide a la base de datos la lista completa de todas las categorias guardadas en el sistema
     * y las devuelve al instante para que puedan mostrarse en los menus desplegables.
     *
     * @return La lista con todas las especialidades de trabajo disponibles.
     */
    @GetMapping
    public ResponseEntity<List<Categoria>> getAllCategorias() {
        return new ResponseEntity<>(categoriaService.getAllCategorias(), HttpStatus.OK);
    }

    /**
     * Recibe los datos de una nueva categoria de trabajo y la guarda de forma segura en la base de datos
     * para que los tecnicos o avisos puedan usarla a partir de ahora.
     *
     * @param categoria El objeto que contiene el nombre y los datos de la nueva especialidad.
     * @return La categoria recien creada confirmando que el proceso ha sido un exito.
     */
    @PostMapping
    public ResponseEntity<Categoria> createCategoria(@RequestBody Categoria categoria) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoriaService.saveCategoria(categoria));
    }
}