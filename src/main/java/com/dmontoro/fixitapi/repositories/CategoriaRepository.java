package com.dmontoro.fixitapi.repositories;

import com.dmontoro.fixitapi.models.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Interfaz que sirve para gestionar las categorias en la base de datos.
 * Al extender de JpaRepository el sistema nos regala todas las funciones necesarias
 * para guardar, borrar o buscar especialidades sin tener que escribir ni una sola linea de SQL.
 */
@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
}