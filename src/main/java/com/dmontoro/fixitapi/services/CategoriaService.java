package com.dmontoro.fixitapi.services;

import com.dmontoro.fixitapi.models.Categoria;
import com.dmontoro.fixitapi.repositories.CategoriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Clase de servicio que gestiona la logica de las categorias o especialidades tecnicas.
 * Sirve de intermediario entre el controlador y la base de datos para organizar los tipos
 * de trabajos que realiza la empresa como fontaneria o electricidad.
 */
@Service
public class CategoriaService {

    @Autowired
    private CategoriaRepository categoriaRepository;

    /**
     * Obtiene la lista completa de todas las ramas tecnicas registradas en el sistema.
     * @return Un listado con todas las categorias disponibles.
     */
    public List<Categoria> getAllCategorias() {
        return categoriaRepository.findAll();
    }

    /**
     * Busca una especialidad concreta utilizando su numero de identificador unico.
     * @param id El numero de identificacion de la categoria.
     * @return Un objeto opcional que contiene los datos de la especialidad si se encuentra.
     */
    public Optional<Categoria> getCategoriaById(Long id) {
        return categoriaRepository.findById(id);
    }

    /**
     * Registra una nueva categoria en la base de datos o actualiza una que ya existe.
     * @param categoria El objeto con el nombre y detalles de la especialidad.
     * @return La categoria guardada con su identificador oficial.
     */
    public Categoria saveCategoria(Categoria categoria) {
        return categoriaRepository.save(categoria);
    }

    /**
     * Elimina una especialidad tecnica del sistema de forma definitiva mediante su numero identificador.
     * @param id El numero de la categoria que se desea borrar.
     */
    public void deleteCategoria(Long id) {
        categoriaRepository.deleteById(id);
    }
}